import { useEffect } from "react";
import { useForm } from "react-hook-form";
import { createCartegotyPost, createCategotyPost, getCategories } from "../../api/ApiService";
import { useState } from "react";
import * as yup from "yup";
import { yupResolver } from "@hookform/resolvers/yup";

export default function CreateCategory() {
    const [tree, setTree] = useState([]);

    const {
        register,
        handleSubmit,
        watch,
        reset,
        formState: { errors, isSubmitting },
    } = useForm({
        defaultValues: { bigId: "", midId: "", name: "" },
    });

    useEffect(() => {
        (async () => {
            try {
                const { data } = await getCategories();
                setTree(data);
            } catch (e) {
                console.error("카테고리 로드 실패", e);
            }
        })();
    }, []);

    const bigId = watch("bigId");
    const midId = watch("midId");
    const big = tree.find((c) => c.id === +bigId);
    const mids = big?.children || [];

    useEffect(() => {
        reset({ bigId, midId: "", name: watch("name") });
    }, [bigId]);

    const onSubmit = async (data) => {
        try {
            const parentId = data.midId ? +data.midId : data.bigId ? +data.bigId : null; // 깊이 자동 판정

            await createCategotyPost({ name: data.name, parentId });
            alert("생성 완료");

            // 카테고리 다시 불러오기
            const { data: refresh } = await getCategories();
            setTree(refresh);

            reset(); //입력 초기화
        } catch (e) {
            console.error("생성 실패", e);
            alert("카테고리 생성에 실패했습니다.");
        }
    };

    return (
        <div className="flex justify-center items-center h-screen bg-[#1d253d] px-4">
            <form
                onSubmit={handleSubmit(onSubmit)}
                className="flex flex-col md:flex-row gap-4 items-center bg-white p-8 rounded-xl shadow-lg"
            >
                <select
                    {...register("bigId")}
                    className="w-48 px-4 py-2 border border-gray-300 rounded-md bg-white focus:outline-none focus:ring-2 focus:ring-blue-400"
                >
                    <option value="">대분류 선택</option>
                    {tree.map((c) => (
                        <option key={c.id} value={c.id}>
                            {c.name}
                        </option>
                    ))}
                </select>

                <select
                    {...register("midId")}
                    disabled={!bigId}
                    className="w-48 px-4 py-2 border border-gray-300 rounded-md bg-white disabled:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-blue-400"
                >
                    <option value="">중분류 선택</option>
                    {mids.map((c) => (
                        <option key={c.id} value={c.id}>
                            {c.name}
                        </option>
                    ))}
                </select>

                <input
                    placeholder="새 카테고리 이름"
                    {...register("name", { required: true })}
                    className="w-64 px-4 py-2 border border-gray-300 rounded-md bg-white focus:outline-none focus:ring-2 focus:ring-blue-400"
                />

                <button type="submit" className="px-6 py-2 bg-blue-600 text-white font-medium rounded-md hover:bg-blue-700 transition">
                    카테고리 생성
                </button>
            </form>
        </div>
    );
}
