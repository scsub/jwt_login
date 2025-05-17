import { Outlet, Link } from "react-router-dom";

function NavigationBar() {
    return (
        <nav style={{ background: "#ccc", padding: "10px" }}>
            <Link to="/" style={{ marginRight: "10px" }}>
                홈
            </Link>
            <Link to="/product" style={{ marginRight: "10px" }}>
                상품
            </Link>
            <Link to="/login" style={{ marginRight: "10px" }}>
                로그인
            </Link>
            <Link to="/signup" style={{ marginRight: "10px" }}>
                회원가입
            </Link>
            <Link to="/cart" style={{ marginRight: "10px" }}>
                카트
            </Link>
            <Link to="/product/registraion" style={{ marginRight: "10px" }}>
                상품등록
            </Link>
            <Link to="/myPage" style={{ marginRight: "10px" }}>
                마이페이지
            </Link>
            <Link to="/myPage/changepassword" style={{ marginRight: "10px" }}>
                마이페이지/비밀번호
            </Link>
            <Link to="/admin/categories" style={{ marginRight: "10px" }}>
                카테고리 작성
            </Link>
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
