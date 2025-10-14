export default function ProductReviews({ reviews }) {
    if (!reviews || reviews.length === 0) {
        return (
            <div className="max-w-4xl mx-auto mt-12">
                <h2 className="text-2xl font-bold mb-6 pl-1 border-l-4 border-[#3182b8]  ">리뷰</h2>
                <div className="text-center text-gray-500">등록된 리뷰가 없습니다.</div>
            </div>
        );
    }
    return (
        <div className="max-w-4xl mx-auto mt-12">
            <ul>
                <h2 className="text-2xl font-bold mb-6 pl-1 border-l-4 border-[#3182b8]  ">리뷰</h2>
                {reviews.map((review) => (
                    <li key={review.id} className="bg-[#fffafa] rounded-2xl shadow-md p-6 border border-[#e3e3e9]">
                        <div className="flex items-center gap-2 mb-2">
                            {review.recommend ? (
                                <div className="px-2 py-1 bg-blue-100 text-blue-700 font-bold rounded-full">추천</div>
                            ) : (
                                <div className="px-2 py-1 bg-red-100 text-red-600 font-bold rounded-full">비추천</div>
                            )}
                            <div className="ml-auto text-xs text-gray-400">{review.createdAt?.slice(0, 10)}</div>
                        </div>
                        <div className="flex items-center gap-2 mb-3">
                            <div className="font-bold text-gray-700">{review.username?.slice(0, 3) + "*****"}</div>
                        </div>
                        <p className="text-base text-gray-900">{review.content}</p>
                    </li>
                ))}
            </ul>
        </div>
    );
}
