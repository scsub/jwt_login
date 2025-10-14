import { Outlet, Link, useNavigate, useLocation } from "react-router-dom";
import { getUserProfile, logout } from "../api/ApiService";
import { useEffect, useState } from "react";
import { set } from "react-hook-form";
import { getRole, hasRole } from "../utill/auth";

function NavigationBar() {
    const navigator = useNavigate();
    const location = useLocation(); // URL에 대한 정보를 담고있음음
    const [isLoggedIn, setIsLoggedIn] = useState(false); // 로그인 상태 관리

    const role = getRole();
    const isAdmin = hasRole("ROLE_ADMIN");
    useEffect(() => {
        // 로그인 상태 확인 로직
        (async () => {
            try {
                await getUserProfile();
                setIsLoggedIn(true);
            } catch (e) {
                console.error("로그인 상태 확인 실패", e);
                setIsLoggedIn(false);
            }
        })();
    }, [location.pathname]); // 현재경로를 나타내기 때문에 경로가 바뀌면 로그인상태를 확인함
    const handleLogout = async (e) => {
        e.preventDefault();
        try {
            console.log("로그아웃 실행");
            await logout();

            console.log("로그아웃 성공");
            setIsLoggedIn(false);
            navigator("/");
        } catch (e) {
            console.error("로그아웃 실패");
        }
    };
    return (
        <nav className="flex items-center justify-between p-4 bg-[#2992d8] text-white">
            <Link to="/" className="font-bold">
                홈
            </Link>
            <Link to="/product" className="font-bold">
                상품
            </Link>

            <Link to="/cart" className="font-bold">
                카트
            </Link>

            <Link to="/myPage" className="font-bold">
                마이페이지
            </Link>
            <Link to="/myPage/purchaseList" className="font-bold">
                구매목록
            </Link>
            {role === "ADMIN" && (
                <>
                    <Link to="admin/product/registraion" className="font-bold">
                        상품등록
                    </Link>
                    <Link to="/admin/categories" className="font-bold">
                        카테고리 작성
                    </Link>
                </>
            )}

            <div className="flex items-center space-x-4">
                {isLoggedIn ? (
                    <>
                        <div>
                            <button onClick={handleLogout} className="font-bold">
                                로그아웃
                            </button>
                        </div>
                    </>
                ) : (
                    <>
                        <Link to="/login" className=" font-bold">
                            로그인
                        </Link>
                        <Link to="/signup" className=" font-bold">
                            회원가입
                        </Link>
                    </>
                )}
            </div>
        </nav>
    );
}

function Layout() {
    return (
        <div>
            <NavigationBar />
            <Outlet />
        </div>
    );
}

export default Layout;
