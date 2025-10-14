import { useEffect, useState } from "react";
import api, { createProductPost, getCategories } from "../../api/ApiService";
import { Controller, useForm } from "react-hook-form";

import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";
import { useNavigate } from "react-router-dom";

const MAX_MB = 15;

const schema = yup.object().shape({
    big: yup.number().typeError("대분류를 선택해주세요").required("대분류를 선택해주세요"),
    mid: yup.number().typeError("중분류를 선택해주세요").required("중분류를 선택해주세요"),
    small: yup.number().typeError("소분류를 선택해주세요").required("소분류를 선택해주세요"),
    name: yup.string().required("상품명은 필수입니다"),
    desc: yup.string().required("설명은 필수입니다"),
    price: yup.number().typeError("숫자만 입력해주세요").min(0, "0 이상의 값을 입력해주세요").required("가격을 입력해주세요"),
    qty: yup.number().typeError("숫자만 입력해주세요").min(0, "0 이상의 값을 입력해주세요").required("수량을 입력해주세요"),
    files: yup
        .mixed()
        .test("required", "이미지를 선택해주세요", (value) => value && value.length > 0)
        .test(
            "fileSize",
            `파일당 최대 ${MAX_MB}MB 이하여야 합니다`,
            (value) => value && Array.from(value).every((file) => file.size <= MAX_MB * 1024 * 1024)
        ),
});

export default function ProductForm() {
    const {
        register,
        handleSubmit,
        control,
        reset,
        watch,
        formState: { errors, isSubmitting },
    } = useForm({ resolver: yupResolver(schema) });

    const [tree, setTree] = useState([]);

    useEffect(() => {
        getCategories()
            .then((res) => setTree(res.data))
            .catch((e) => {
                console.error(e);
            });
    }, []);

    const bigValue = watch("big"); // 대분류가 선택됐는지 확인하는 변수
    const midValue = watch("mid"); // 중분류가 선택됐는지 확인하는 변수
    const smallValue = watch("small"); // 소분류가 선택됐는지 확인하는 변수
    const midCategory = bigValue ? tree.find((category) => category.id === +bigValue)?.children || [] : []; // 대분류가 선택되면 중분류를 선택할수있게 옵션을 보여준다
    const smallCategory = midValue ? midCategory.find((category) => category.id === +midValue)?.children || [] : []; // 중분류가 선택되면 소분류를 선택할수있게 옵션을 보여준다

    const onSubmit = async (data) => {
        try {
            //JSON
            const productRequest = {
                name: data.name,
                description: data.desc,
                price: Number(data.price),
                quantity: Number(data.qty),
                categoryId: data.small,
            };

            //FormData
            const fd = new FormData();
            fd.append(
                "data",
                new Blob([JSON.stringify(productRequest)], {
                    type: "application/json",
                })
            );
            Array.from(data.files).forEach((file) => fd.append("files", file));

            await createProductPost(fd); // Content‑Type 자동으로 multipart/form‑data + boundary
            reset();
            alert("등록 완료");
        } catch (e) {
            console.log("상품을 등록할수 없습니다", e);
            alert("상품 등록에 실패했습니다.");
        }
    };

    return (
        <div className="flex justify-center items-start py-10 bg-[#1d253d] min-h-screen px-4">
            <form onSubmit={handleSubmit(onSubmit)} className="w-full max-w-2xl bg-white rounded-lg shadow p-8 space-y-6">
                <div className="flex flex-col md:flex-row md:space-x-4 space-y-4 md:space-y-0">
                    <div className="flex flex-col w-full">
                        <select {...register("big")}>
                            <option value="">대분류</option>
                            {tree.map((category) => (
                                <option key={category.id} value={category.id}>
                                    {category.name}
                                </option>
                            ))}
                        </select>
                        {errors.big && <p className="text-red-600">{errors.big.message}</p>}
                    </div>
                    <div className="flex flex-col w-full">
                        <select {...register("mid")}>
                            <option value="">중분류</option>
                            {midCategory.map((category) => (
                                <option key={category.id} value={category.id}>
                                    {category.name}
                                </option>
                            ))}
                        </select>
                        {errors.mid && <p className="text-red-600">{errors.mid.message}</p>}
                    </div>
                    <div className="flex flex-col w-full">
                        <select {...register("small")}>
                            <option value="">소분류</option>
                            {smallCategory.map((category) => (
                                <option key={category.id} value={category.id}>
                                    {category.name}
                                </option>
                            ))}
                        </select>
                        {errors.small && <p className="text-red-600">{errors.small.message}</p>}
                    </div>
                </div>

                <div className="flex justify-between border-b pb-4">
                    <span className="block mb-2 font-medium">상품명</span>
                    <div className="w-2/3">
                        <input {...register("name")} placeholder="상품명" className="w-full" />
                        {errors.price && <p className="text-red-600">{errors.name?.message}</p>}
                    </div>
                </div>

                <div className="flex justify-between border-b pb-4">
                    <span className="block mb-2 font-medium">설명</span>
                    <div className="w-2/3">
                        <textarea {...register("desc")} placeholder="설명" className="w-full" />
                        {errors.desc && <p className="text-red-600">{errors.desc.message}</p>}
                    </div>
                </div>

                <div className="flex justify-between border-b pb-4">
                    <span className="block mb-2 font-medium">가격</span>
                    <div className="w-2/3">
                        <input {...register("price")} type="number" min="0" placeholder="가격" className="w-full" />
                        {errors.price && <p className="text-red-600">{errors.price.message}</p>}
                    </div>
                </div>

                <div className="flex justify-between border-b pb-4">
                    <span className="block mb-2 font-medium">수량</span>
                    <div className="w-2/3">
                        <input {...register("qty")} type="number" min="0" placeholder="수량" className="w-full" />
                        {errors.qty && <p className="text-red-600">{errors.qty.message}</p>}
                    </div>
                </div>

                <div>
                    <Controller
                        name="files"
                        control={control}
                        defaultValue={[]}
                        render={({ field }) => (
                            <>
                                <input type="file" multiple accept="image/*" onChange={(e) => field.onChange(e.target.files)} />
                                <p className="text-red-600">{errors.files?.message}</p>
                            </>
                        )}
                    />
                </div>
                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full bg-blue-600 text-white font-medium rounded-md px-4 py-2 hover:bg-blue-700 transition"
                >
                    상품 등록
                </button>
            </form>
        </div>
    );
}
