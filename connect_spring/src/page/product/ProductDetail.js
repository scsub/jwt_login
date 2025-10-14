import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import { getProductById, getProductReviews, postAddItemInCart } from "../../api/ApiService";
import { RootUrl } from "../../components/path";
import * as yup from "yup";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import ProductReviews from "../../components/ProductReviews";

const schema = yup.object({
    qty: yup.number().typeError("숫자를 입력해주세요").min(1, "최소 1개 이상").required(),
});

export default function ProductDetail() {
    const { id } = useParams();
    const [product, setProduct] = useState(null);
    const [mainIdx, setMainIdx] = useState(0);
    const [reviews, setReviews] = useState(null);

    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm({
        resolver: yupResolver(schema),
        defaultValues: { qty: 1 },
    });

    useEffect(() => {
        fetchProduct(id);
    }, [id]);

    useEffect(() => {
        if (!product) return;
        fetchProductReviews(product.id);
    }, [product]);

    const fetchProduct = async (id) => {
        try {
            const { data } = await getProductById(id);
            setProduct(data);
        } catch (e) {
            console.error("불러오기 실패", e);
        }
    };

    const fetchProductReviews = async (productId) => {
        try {
            const response = await getProductReviews(productId);
            setReviews(response.data);
            console.log("리뷰 불러오기 성공", response.data);
        } catch (e) {
            console.error("상품 리뷰 불러오는 도중 에러 발생");
        }
    };

    const onSubmit = async ({ qty }) => {
        try {
            await postAddItemInCart(product.id, qty);
            alert("장바구니에 담겼습니다!");
        } catch (e) {
            alert(e.response?.data?.errors?.product ?? "담기 실패");
        }
    };

    if (!product) {
        return <div className="flex items-center justify-center h-screen text-gray-500">로딩 중…</div>;
    }

    const imgs = product.productImageList || [];

    return (
        <>
            <div className="mx-auto w-full max-w-7xl px-4 lg:px-6 py-10 lg:grid lg:grid-cols-12 lg:gap-8">
                {/*메인 이미지 */}
                <section className="lg:col-span-7">
                    <div className="w-full aspect-square border rounded-2xl overflow-hidden">
                        {imgs && imgs.length > 0 ? (
                            <img src={`${RootUrl()}${imgs[mainIdx].url}`} alt={product.name} className="w-full h-full object-cover" />
                        ) : (
                            <div className="flex items-center justify-center h-full text-gray-400">이미지 없음</div>
                        )}
                    </div>
                </section>

                <aside className="lg:col-span-5 mt-8 lg:mt-0 lg:pl-6">
                    <div className="sticky top-24 bg-white rounded-2xl shadow-xl p-8 space-y-6">
                        {/* 상품명, 가격, 재고*/}
                        <header>
                            <h1 className="text-2xl lg:text-3xl font-bold mb-3">{product.name}</h1>
                            <p className="text-3xl lg:text-4xl font-extrabold text-[#111111]">{product.price.toLocaleString()}원</p>
                            <p className="text-sm mt-2 text-gray-500">
                                재고&nbsp;:&nbsp;
                                {product.quantity > 0 ? product.quantity : "일시 품절"}
                            </p>
                        </header>

                        {/* 수량 입력 + 장바구니 버튼 */}
                        <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                            <div className="flex items-center gap-4">
                                <label className="font-medium whitespace-nowrap">구매 수량</label>
                                <input
                                    type="text"
                                    inputMode="numeric"
                                    className="w-24 text-center border rounded-lg py-2"
                                    {...register("qty")}
                                    onInput={(e) => (e.target.value = e.target.value.replace(/\D/g, ""))}
                                />
                            </div>
                            {errors.qty && <p className="text-sm text-red-500">{errors.qty.message}</p>}

                            <button
                                type="submit"
                                disabled={isSubmitting || product.quantity === 0}
                                className="w-full py-3 rounded-xl text-lg font-semibold text-white transition
                                bg-[#333333] "
                            >
                                {isSubmitting ? "담는 중…" : "장바구니에 담기"}
                            </button>
                        </form>
                    </div>
                </aside>
            </div>

            {/* 상품 상세 설명 */}
            <section className="max-w-7xl mx-auto px-4 lg:px-6 pb-16">
                <h2 className="text-xl font-semibold mb-4">상품 설명</h2>
                <div className="prose max-w-none text-gray-800 whitespace-pre-line">{product.description}</div>
                {imgs.length > 0 && <div className="mt-8 grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4"></div>}
            </section>

            {/* 리뷰 목록 */}
            <ProductReviews reviews={reviews} />
        </>
    );
}
