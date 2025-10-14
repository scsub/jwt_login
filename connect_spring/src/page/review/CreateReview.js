import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import * as yup from "yup";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { getProductById, postCreateReview } from "../../api/ApiService";

const schema = yup.object({
    recommend: yup.boolean().nullable().required("추천 또는 비추천을 선택해주세요"),
    comment: yup.string().trim().min(5, "5자 이상 작성해주세요").max(500, "500자 이하로 작성해주세요").required("리뷰를 입력해주세요"),
});

export default function CreateReview() {
    const { id: productId } = useParams();
    const [product, setProduct] = useState([]);
    const navigate = useNavigate();

    const {
        register,
        handleSubmit,
        setValue,
        watch,
        formState: { errors, isSubmitting },
    } = useForm({
        resolver: yupResolver(schema),
        defaultValues: { recommend: null, comment: "" },
    });
    useEffect(() => {
        fetchProduct();
    }, []);

    const fetchProduct = async () => {
        try {
            const { data } = await getProductById(productId);
            setProduct(data);
        } catch (e) {
            console.error("상품 정보 로드 실패", e);
        }
    };

    const handleSelect = (value) => {
        setValue("recommend", value, { shouldValidate: true });
    };

    const onSubmit = async ({ recommend, comment }) => {
        try {
            await postCreateReview({
                productId,
                recommend,
                content: comment,
            });
            alert("리뷰가 등록되었습니다!");
            navigate(-1);
        } catch (e) {
            alert("리뷰 등록 실패");
            console.error(e);
        }
    };

    return (
        <div className="max-w-xl mx-auto px-4 py-12">
            <h1 className="text-2xl font-bold mb-8 text-center">리뷰 작성</h1>
            <h2 className="text-2xl font-bold mb-8 text-center">{product.name}</h2>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
                {/* 추천 여부 */}
                <section>
                    <label className="block text-lg font-medium mb-3">이 상품을 추천하시나요?</label>
                    <div className="flex gap-4">
                        {/* 추천 버튼 */}
                        <button
                            type="button"
                            className={`flex-1 py-3 rounded-xl border text-center font-semibold
                ${(errors.recommend && "border-red-500") || ""} ${
                                watch("recommend") === true ? "bg-[#2235dd] text-white" : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                            }`}
                            onClick={() => handleSelect(true)}
                        >
                            추천
                        </button>

                        {/* 비추천 버튼 */}
                        <button
                            type="button"
                            className={`flex-1 py-3 rounded-xl border text-center font-semibold
                ${(errors.recommend && "border-red-500") || ""} ${
                                watch("recommend") === false ? "bg-red-600 text-white" : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                            }`}
                            onClick={() => handleSelect(false)}
                        >
                            비추천
                        </button>
                    </div>
                    {errors.recommend && <p className="text-sm text-red-500 mt-1">{errors.recommend.message}</p>}
                    <input type="hidden" {...register("recommend")} />
                </section>

                {/* 코멘트 */}
                <section>
                    <label className="block text-lg font-medium mb-3">리뷰 내용</label>
                    <textarea
                        rows={6}
                        maxLength={500}
                        placeholder="리뷰 내용을 작성해주세요"
                        className="w-full border rounded-lg p-3 focus:ring-2"
                        {...register("comment")}
                    />
                    {errors.comment && <p className="text-sm text-red-500 mt-1">{errors.comment.message}</p>}
                </section>

                {/* 제출 버튼 */}
                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full py-3 rounded-xl bg-[#111111]
                    text-white transition"
                >
                    {isSubmitting ? "등록 중…" : "리뷰 등록"}
                </button>
            </form>
        </div>
    );
}
