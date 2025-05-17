import { useNavigate } from "react-router-dom";
import { logout } from "../api/ApiService";

function Home() {
    const navigator = useNavigate();

    const handleLogout = async (e) => {
        e.preventDefault();
        try {
            console.log("로그아웃 실행");
            await logout();
            console.log("로그아웃 성공");
        } catch (e) {
            console.error("로그아웃 실패");
        }
    };
    const goToProduct = (e) => {
        e.preventDefault();
        navigator("/product");
    };

    return (
        <div>
            <div>
                <button onClick={handleLogout} className="">
                    로그아웃
                </button>
            </div>
            <div>
                <button onClick={goToProduct} className="text-red-500">
                    상품
                </button>
            </div>
        </div>
    );
}

export default Home;
