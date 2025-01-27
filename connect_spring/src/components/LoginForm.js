import { useState } from "react";
import { postLoginForm } from "../api/ApiService";

function LoginForm() {
    const [user, setUser] = useState({
        username: "",
        password: "",
    });

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            console.log(user);
            const data = await postLoginForm(user);
            console.log("로그인 성공:", data);
        } catch (error) {
            console.error("로그인 실패:", error);
        }
    };

    const handleChange = (e) => {
        setUser({
            ...user,
            [e.target.name]: e.target.value,
        });
    };
    return (
        <form onSubmit={handleSubmit}>
            <div>
                <label>아이디:</label>
                <input type="text" name="username" value={user.username} onChange={handleChange} />
            </div>
            <div>
                <label>비밀번호:</label>
                <input type="password" name="password" value={user.password} onChange={handleChange} />
            </div>
            {/* <div>
                <label>이메일:</label>
                <input type="email" name="email" value={user.email} onChange={handleChange} />
            </div>
            <div>
                <label>전화번호:</label>
                <input type="number" name="phoneNumber" value={user.phoneNumber} onChange={handleChange} />
            </div>
            <div>
                <label>주소:</label>
                <input type="text" name="address" value={user.address} onChange={handleChange} />
            </div> */}

            <button type="submit">로그인</button>
        </form>
    );
}

export default LoginForm;
