import { useEffect, useState } from "react";
import { getOrders, patchCancelOrder } from "../../api/ApiService";
import { Link, useNavigate } from "react-router-dom";
import { RootUrl } from "../../components/path";
import { set } from "react-hook-form";

export default function PurchaseList() {
    const navigate = useNavigate();
    const [orderList, setOrderList] = useState([]);
    useEffect(() => {
        fetchOrders();
    }, []);
    // 주문 목록 가져오기기
    const fetchOrders = async () => {
        try {
            const response = await getOrders();
            const orderList = response.data;
            setOrderList(orderList);
        } catch (e) {
            console.error("주문 목록을 불러오는 도중 에러 발생", e);
        }
    };
    // 주문 취소하기  삭제가 아닌 취소임임
    const cancelOrder = async (orderId) => {
        try {
            await patchCancelOrder(orderId);
            navigate(0);
        } catch (e) {
            console.error("주문 취소 중 에러 발생", e);
        }
    };

    return (
        <div className="bg-[#1d253d] ">
            {/* 뒤로가기 */}
            <div
                className="text-4xl flex mt-10 ml-10 font-bold cursor-pointer text-[#f6f8ff] hover:text-[#d4d4d4]"
                onClick={() => {
                    navigate(-1);
                }}
            >{`<`}</div>
            {/* 주문이 없으면 구매 목록이 없다고 뜨고 있으면 구매 목록을 보여줌줌 */}
            {orderList.length === 0 ? (
                <>
                    <div className="flex flex-col items-center justify-center min-h-screen">
                        <h1 className="text-2xl font-bold mb-4">구매 목록</h1>
                        <p className="text-gray-600">구매한 상품이 없습니다.</p>
                    </div>
                </>
            ) : (
                <>
                    <div className="flex flex-col items-center min-h-screen py-12">
                        <h1 className="text-2xl font-bold mb-8 text-[#f6f8ff]">구매 목록</h1>

                        <ul className="w-full max-w-4xl space-y-6">
                            {/* 주문 목록을 보여준다 */}
                            {orderList.map((order) => (
                                <li key={order.orderId} className="border flex justify-between rounded-lg shadow-sm p-6 bg-white">
                                    <div className="flex-1 flex flex-col gap-4">
                                        {/* 주문 번호, 날짜 */}
                                        <div>
                                            <div>주문 번호 : {order.id}</div>
                                            <div className="text-xl font-bold">{new Date(order.orderDate).toLocaleDateString()} 주문</div>
                                        </div>

                                        <ul className="space-y-4">
                                            {/* 주문 목록의 상품을 나열한다 */}
                                            {order.itemList.map((item) => (
                                                <li
                                                    key={item.productId}
                                                    className="flex items-start gap-4 border-b-2 border-gray-200 w-full pb-4"
                                                >
                                                    {/* 상품 이미지 */}
                                                    <img
                                                        className="bg-gray-500 w-36 h-36 rounded-md object-cover"
                                                        key={item.productImageResponse.id}
                                                        src={`${RootUrl()}${item.productImageResponse.url}`}
                                                        onClick={() => {
                                                            navigate(`/product/${item.productId}`);
                                                        }}
                                                    />

                                                    {/* 상품의 정보 */}
                                                    <div className="">
                                                        <div className="text-lg font-bold">{item.productName}</div>
                                                        <div className="text-gray-600">수량: {item.quantity}</div>
                                                        <div className="text-gray-800">가격: {item.price}원</div>
                                                        {order.orderStatus === "ORDERED" ? (
                                                            <Link
                                                                to={`/product/${item.productId}/review`}
                                                                className="border-2 items-center bg-[#bebebe] rounded-lg p-1"
                                                            >
                                                                리뷰작성
                                                            </Link>
                                                        ) : null}
                                                    </div>
                                                </li>
                                            ))}
                                        </ul>
                                    </div>

                                    <div className="flex flex-col justify-between items-end space-y-4">
                                        {/* 주문 상태*/}
                                        <div className={`${order.orderStatus === "ORDERED" ? "text-green-700" : "text-red-700"} font-bold`}>
                                            {order.orderStatus}
                                        </div>
                                        {/* 주문 취소를 할경우 주문취소 버튼을 없앤다 */}
                                        {order.orderStatus === "ORDERED" ? (
                                            <button
                                                className="text-sm text-gray-500 hover:underline"
                                                onClick={() => {
                                                    cancelOrder(order.id);
                                                }}
                                            >
                                                주문 취소
                                            </button>
                                        ) : (
                                            <div></div>
                                        )}
                                    </div>
                                </li>
                            ))}
                        </ul>
                    </div>
                </>
            )}
        </div>
    );
}
