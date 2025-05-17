import { useParams } from "react-router-dom";
import api, { getProductById, postAddItemInCart } from "../api/ApiService";
import { useEffect, useState } from "react";
import { RootUrl } from "../components/path";
import * as yup from "yup";
import { useFieldArray, useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";

function ProductDetail() {
    const { id } = useParams();
    const [product, setProduct] = useState([]);

    const schema = yup.object({
        cartItemQuantity: yup.number().min(1, "최소 수량은 1개입니다").required("수량을 1개 이상 입력해주세요"),
    });

    const {
        register,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm({ resolver: yupResolver(schema), defaultValues: { cartItemQuantity: 1 } });

    useEffect(() => {
        (async () => {
            try {
                const res = await getProductById(id);
                console.log(res.data);
                setProduct(res.data);
            } catch (e) {
                console.log("상품 불러오기 실패", e);
            }
        })();
    }, [id]);

    const onSubmit = async (data) => {
        try {
            await postAddItemInCart(product.id, data.cartItemQuantity);
            alert("카트에 담겼습니다");
        } catch (e) {
            const msg = e.response?.data?.errors?.product;
            alert(msg);
        }
    };

    return (
        <div>
            <form onSubmit={handleSubmit(onSubmit)}>
                <div>제품명 : {product.name}</div>
                <div>가격 : {product.price}</div>
                <div>재고 : {product.quantity}</div>
                <div>
                    <label>주문 수량 : </label>
                    <input type="number" {...register("cartItemQuantity")} />
                </div>
                {errors.orderQuantity && <p>{errors.cartItemQuantity.message}</p>}
                <div>{product.description}</div>
                <div>
                    {product.productImageList?.map((img) => (
                        <img
                            key={img.id}
                            src={`${RootUrl()}${img.url}`}
                            style={{ width: "150px", height: "auto" }}
                        ></img>
                    ))}
                </div>
                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? "장바구니 제출 중" : "장바구니에 담기"}
                </button>
            </form>
        </div>
    );
}

export default ProductDetail;
