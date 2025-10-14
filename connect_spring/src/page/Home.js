import { Link, useNavigate } from "react-router-dom";
import { getCategories, logout } from "../api/ApiService";
import { RootUrl } from "../components/path";
import { useEffect, useState } from "react";

export default function Home() {
    return (
        <div className="flex flex-col items-center justify-center min-h-screen bg-[#1d253d] text-gray-100 px-4">
            {/* 메인 */}
            <h1 className="text-3xl lg:text-4xl font-extrabold mb-3">ㅁㅁㅁ몰 프로젝트</h1>

            {/* 딱히 이렇다할게 없다 */}
            <nav className="flex flex-col items-center gap-4 text-lg">
                <Link to="/product" className="hover:text-indigo-400 transition underline-offset-4 hover:underline">
                    ▶ 상품 둘러보기
                </Link>
            </nav>
        </div>
    );
}
