import { useEffect, useState } from "react";
import { deleteCartItem, getCartItems, postOrder, updateCartItem } from "../api/ApiService";
import * as yup from "yup";
import { useFieldArray, useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { RootUrl } from "../components/path";

function Cart() {
    const [cartItems, setCartItems] = useState([]);

    const schema = yup.object({
        items: yup.array().of(
            yup.object({
                id: yup.number().required(),
                productName: yup.string().required(),
                quantity: yup
                    .number()
                    .typeError("수량을 숫자로 입력해주세요")
                    .min(1, "최소 1개 이상이어야 합니다")
                    .required("수량을 입력해주세요"),
                imageId: yup.number().required(),
                imageUrl: yup.string().required(),
            })
        ),
    });

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors, isSubmitting },
    } = useForm({ resolver: yupResolver(schema), defaultValues: { items: [] } });

    // 장바구니 가져오기
    useEffect(() => {
        (async () => {
            try {
                const res = await getCartItems();
                const items = res.data.cartItemResponses.map((cartItem) => ({
                    id: cartItem.id,
                    productName: cartItem.productName,
                    quantity: cartItem.quantity,
                    imageId: cartItem.productImageResponse.id,
                    imageUrl: cartItem.productImageResponse.url,
                }));
                setCartItems(items);

                reset({ items });
            } catch (e) {
                console.error("장바구니를 불러올 수 없음", e);
            }
        })();
    }, [reset]);

    //제출
    const onSubmit = async (data) => {
        try {
            await Promise.all(data.items.map((item) => updateCartItem(item.id, item.quantity)));
            await postOrder();
            console.log("주문 성공");
        } catch (e) {
            console.error("주문 실패", e);
        }
    };

    // 개별 상품 삭제
    const handleDeleteCartItem = async (id) => {
        try {
            await deleteCartItem(id);
            setCartItems((items) => items.filter((item) => item.id !== id));
        } catch (e) {
            console.error("카트아이템을 삭제할 수 없음", e);
        }
    };

    return (
        <div>
            <form onSubmit={handleSubmit(onSubmit)}>
                <ul>
                    {cartItems.map((cartItem, idx) => (
                        <li key={cartItem.id}>
                            <div>상품: {cartItem.productName}</div>
                            <div>
                                <img
                                    key={cartItem.imageId}
                                    src={`${RootUrl()}${cartItem.imageUrl}`}
                                    style={{ width: "150px", height: "auto" }}
                                >
                                    {}
                                </img>
                            </div>

                            <label>갯수 : </label>
                            <input
                                type="number"
                                min={1}
                                defaultValue={cartItem.quantity}
                                {...register(`items.${idx}.quantity`)}
                            />
                            {errors.items?.[idx]?.quantity && (
                                <p className="error-message">{errors.items[idx].quantity.message}</p>
                            )}
                            <button type="button" onClick={() => handleDeleteCartItem(cartItem.id)}>
                                상품삭제
                            </button>
                        </li>
                    ))}
                </ul>
                <button type="submit" disabled={isSubmitting}>
                    {isSubmitting ? "주문 처리 중" : "주문하기"}
                </button>
            </form>
        </div>
    );
}

export default Cart;
