import { useEffect, useState } from "react";
import {
    deleteCartItem,
    getCartItems,
    patchCartItemQuantity,
    postOrder,
    updateCartItem,
    updateCartItemQuantity,
} from "../../api/ApiService";
import * as yup from "yup";
import { set, useFieldArray, useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { RootUrl } from "../../components/path";
import { useNavigate } from "react-router-dom";

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

export default function Cart() {
    const [totalPrice, setTotalPrice] = useState(0);
    const [cartItems, setCartItems] = useState([]);
    const navigator = useNavigate();

    // useForm
    const {
        register,
        handleSubmit,
        reset,
        setValue,
        formState: { errors, isSubmitting },
    } = useForm({
        resolver: yupResolver(schema),
        defaultValues: { items: [] },
    });

    // useEffect
    useEffect(() => {
        fetchCartItems();
    }, [reset]);

    // 장바구니 가져오기
    const fetchCartItems = async () => {
        try {
            const { data } = await getCartItems();
            const cartItemResponses = data.cartItemResponses;

            let sum = 0;
            const items = cartItemResponses.map((c) => {
                sum += c.productPrice * c.quantity;
                return {
                    id: c.id,
                    productName: c.productName,
                    quantity: c.quantity,
                    imageId: c.productImageResponse.id,
                    imageUrl: c.productImageResponse.url,
                };
            });

            console.log("합계 가격:", sum);
            setTotalPrice(sum);
            setCartItems(items);
            reset({ items });
        } catch (err) {
            console.error("장바구니를 불러올 수 없음", err);
        }
    };

    const changeCartItemQuantity = async (id, value) => {
        await patchCartItemQuantity(id, value);
        navigator(0);
    };

    const calculateCartItemQuantity = async (item, value) => {
        const newQuantity = item.quantity + value;
        if (newQuantity < 1) {
            console.error("수량은 최소 1개 이상이어야 합니다");
            alert("수량은 최소 1개 이상이어야 합니다");
            return;
        }
        try {
            setCartItems((items) => items.map((cartItem) => (cartItem.id === item.id ? { ...cartItem, quantity: newQuantity } : cartItem)));
            setTotalPrice((prev) => prev + item.productPrice * value);
            await changeCartItemQuantity(item.id, newQuantity);
        } catch (e) {
            console.error("장바구니 아이템 수량을 업데이트할 수 없음", e);
            alert("장바구니 아이템 수량을 업데이트할 수 없습니다");
        }
    };

    const onSubmit = async (data) => {
        try {
            if (data.items.length != 0) {
                await Promise.all(data.items.map((item) => patchCartItemQuantity(item.id, item.quantity)));
                await postOrder();
                console.log("주문 성공");
                navigator(0);
            } else {
                alert("주문가능한 상품이 없습니다");
            }
        } catch (e) {
            console.error("주문 실패", e);
        }
    };

    // 개별 상품 삭제
    const handleDeleteCartItem = async (id) => {
        try {
            await deleteCartItem(id);
            setCartItems((items) => items.filter((item) => item.id !== id));
            navigator(0);
        } catch (e) {
            console.error("카트아이템을 삭제할 수 없음", e);
        }
    };

    return (
        <div>
            <form onSubmit={handleSubmit(onSubmit)} className="flex justify-between">
                <div className="mx-32 mt-10  border-2 rounded-3xl px-10 pt-5 pb-10 shadow-lg w-2/3 ">
                    <div
                        className="text-7xl mb-5 cursor-pointer"
                        onClick={() => {
                            navigator(-1);
                        }}
                    >
                        {"<"}
                    </div>
                    <ul>
                        {cartItems.map((cartItem, idx) => (
                            <li key={cartItem.id} className="justify-start mb-5 flex space-x-4 pb-5 border-b-4">
                                <div>
                                    <img key={cartItem.imageId} src={`${RootUrl()}${cartItem.imageUrl}`} className="w-56 h-40"></img>
                                </div>
                                <div className="flex flex-col justify-between">
                                    <div className="text-xl max-w-[30ch] truncate">{cartItem.productName} </div>
                                    <div className="flex  ">
                                        <div
                                            onClick={() => {
                                                calculateCartItemQuantity(cartItem, -1);
                                            }}
                                            className="cursor-pointer bg-gray-300 px-4 py-3"
                                        >
                                            -
                                        </div>
                                        <input
                                            min={1}
                                            defaultValue={cartItem.quantity}
                                            className="w-16 text-center border-2"
                                            {...register(`items.${idx}.quantity`)}
                                            onInput={(e) => (e.target.value = e.target.value.replace(/\D/g, ""))}
                                            onBlur={(e) => {
                                                const qty = parseInt(e.target.value);
                                                if (isNaN(qty) || qty < 1) {
                                                    alert("수량은 최소 1개 이상이어야 합니다");
                                                    navigator(0);
                                                }
                                                setValue(`items.${idx}.quantity`, qty);
                                                setCartItems((items) =>
                                                    items.map((ci) => (ci.id === cartItem.id ? { ...ci, quantity: qty } : ci))
                                                );
                                                setTotalPrice(
                                                    (prev) => prev - cartItem.productPrice * cartItem.quantity + cartItem.productPrice * qty
                                                );
                                                changeCartItemQuantity(cartItem.id, qty);
                                            }}
                                        />
                                        <div
                                            onClick={() => {
                                                calculateCartItemQuantity(cartItem, 1);
                                            }}
                                            className="cursor-pointer bg-gray-300 px-4 py-3"
                                        >
                                            +
                                        </div>
                                        {errors.items?.[idx]?.quantity && (
                                            <p className="error-message">{errors.items[idx].quantity.message}</p>
                                        )}
                                    </div>
                                </div>
                                <button
                                    type="button"
                                    onClick={() => handleDeleteCartItem(cartItem.id)}
                                    className="mt-32  rounded-lg text-bold text-lg px-4 py-1 h-2/3   focus:outline-none focus:ring border-b-2 border-black"
                                >
                                    삭제
                                </button>
                            </li>
                        ))}
                    </ul>
                </div>
                <div className="w-2/12 sticky mt-32 self-start  mr-16">
                    <h2 className="text-xl font-bold mb-4">합계 가격 {totalPrice}원</h2>
                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="w-full bg-blue-500 text-white rounded px-4 py-2 hover:bg-blu e-600 focus:outline-none focus:ring"
                    >
                        {isSubmitting ? "주문 처리 중" : "주문하기"}
                    </button>
                </div>
            </form>
        </div>
    );
}
