import { useEffect, useState } from "react";
import {
    getCategories,
    getProductBySmallCategoryId,
    getProductReviews,
    getProducts,
    getSerchProduct,
    postAddItemInCart,
} from "../../api/ApiService";
import { Link } from "react-router-dom";
import { RootUrl } from "../../components/path";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";
import { useForm } from "react-hook-form";
const schema = yup.object({
    keyword: yup.string().trim().min(2, "두 글자 이상 입력해주세요").required("검색어를 입력해주세요"),
});

function Product() {
    const [products, setProducts] = useState([]);
    const [categories, setCategories] = useState([]);

    useEffect(() => {
        fetchProducts();
        fetchCategories();
    }, []);

    const fetchProducts = async () => {
        try {
            const response = await getProducts();
            setProducts(response.data);
        } catch (e) {
            console.error("상품 불러오는 도중 에러 발생");
        }
    };

    const fetchCategories = async () => {
        try {
            const response = await getCategories();
            setCategories(response.data);
        } catch (e) {
            console.error("카테고리 불러오는 도중 에러 발생");
        }
    };

    const fetchProductBySmallCategory = async (smallCategoryId) => {
        try {
            const response = await getProductBySmallCategoryId(smallCategoryId);
            setProducts(response.data);
        } catch (e) {
            console.error("소분류 상품 불러오는 도중 에러 발생");
        }
    };

    const onSearch = async ({ keyword }) => {
        try {
            const { data } = await getSerchProduct(keyword);
            setProducts(data);
        } catch (e) {
            alert("검색 실패");
            console.error(e);
        }
    };

    const {
        register,
        handleSubmit,
        control,
        reset,
        watch,
        setValue,
        formState: { errors, isSubmitting },
    } = useForm({ resolver: yupResolver(schema), defaultValues: { keyword: "" } });

    const bigValue = watch("big"); // 대분류가 선택됐는지 확인하는 변수
    const midValue = watch("mid"); // 중분류가 선택됐는지 확인하는 변수
    const smallValue = watch("small"); // 소분류가 선택됐는지 확인하는 변수

    const midCategory = bigValue ? categories.find((category) => category.id === +bigValue)?.children || [] : []; // 대분류가 선택되면 중분류를 선택할수있게 옵션을 보여준다
    const smallCategory = midValue ? midCategory.find((category) => category.id === +midValue)?.children || [] : []; // 중분류가 선택되면 소분류를 선택할수있게 옵션을 보여준다

    return (
        <div>
            {/* 검색창 */}
            <form onSubmit={handleSubmit(onSearch)} className="border-b-2 pb-2">
                <div className="flex justify-center items-center mt-5 pb-5  mx-2">
                    <input
                        placeholder="상품명을 입력하세요"
                        className="w-full max-w-md border-2 rounded-lg px-4 py-2
                        focus:outline-none focus:ring-2 focus:ring-[#525050]"
                        {...register("keyword")}
                    />
                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="bg-[#0e0d0d] text-white rounded-lg
                        px-4 py-2 flex items-center"
                    >
                        검색
                    </button>
                </div>
                {errors.keyword && <p className="text-center text-red-500 text-sm -mt-4">{errors.keyword.message}</p>}
            </form>
            {/* 에러 메시지 */}

            {/*상품 카테고리 */}
            <div className="flex flex-col mx-10">
                {/*대분류  */}
                <div className="flex text-2xl mt-4 mb-1">
                    {categories.map((category) => (
                        <button
                            key={category.id}
                            type="button"
                            onClick={() => {
                                setValue("big", category.id);
                                setValue("mid", null);
                                setValue("small", null);
                            }}
                            className={`px-4 py-2 rounded ${
                                +bigValue === category.id ? " border-b-2 border-[#1b2f70] text-blue-700" : " text-gray-700"
                            }`}
                        >
                            {category.name}
                        </button>
                    ))}
                </div>
                {/*중분류  */}
                {bigValue && (
                    <div className="flex ">
                        {midCategory.map((category) => (
                            <button
                                key={category.id}
                                type="button"
                                onClick={() => {
                                    setValue("mid", category.id);
                                    setValue("small", null);
                                }}
                                className={`px-4 py-2 rounded ${
                                    +midValue === category.id ? " border-b-2  border-[#1b2f70] text-blue-700" : " text-gray-700"
                                }`}
                            >
                                {category.name}
                            </button>
                        ))}
                    </div>
                )}
                {/*소분류  */}
                {midValue && (
                    <div className="flex">
                        {smallCategory.map((category) => (
                            <button
                                key={category.id}
                                type="button"
                                onClick={() => {
                                    setValue("small", category.id);
                                    fetchProductBySmallCategory(category.id);
                                }}
                                className={`px-4 py-2 rounded ${
                                    +smallValue === category.id ? "border-b-2  border-[#1b2f70] text-blue-700" : " text-gray-700"
                                }`}
                            >
                                {category.name}
                            </button>
                        ))}
                    </div>
                )}
            </div>

            {/* 상품 목록 */}
            <div>
                <ul className="flex ">
                    {products.map((product) => (
                        <li className="flex flex-col border-2 rounded-2xl  mx-3">
                            <Link to={`/product/${product.id}`} key={product.id}>
                                <div>
                                    {product.productImageList.map((img) => (
                                        <img key={img.id} src={`${RootUrl()}${img.url}`} className="w-60 h-60  m-2" />
                                    ))}
                                </div>
                            </Link>
                            <div className="flex justify-between mx-4">
                                <div>{product.name}</div>
                                <div
                                    onClick={() => {
                                        postAddItemInCart(product.id, 1)
                                            .then(() => {
                                                alert("장바구니에 추가되었습니다.");
                                            })
                                            .catch((error) => {
                                                console.error("장바구니 추가 실패:", error);
                                                alert("장바구니 추가에 실패했습니다.");
                                            });
                                    }}
                                >
                                    장바구니
                                </div>
                            </div>
                        </li>
                    ))}
                </ul>
            </div>
            {/*리뷰 목록 */}
            <div>{}</div>
        </div>
    );
}

export default Product;
