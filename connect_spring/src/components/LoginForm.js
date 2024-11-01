import axios from "axios";
import { useState } from "react";
import { postLoginForm } from "../api/ApiService";

function LoginForm() {
    const [user, setUser] = useState({
        username: "",
        password: "",
    });

    const handleChange = (e) => {
        setUser({
            ...user,
            [e.target.name]: e.target.value,
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            console.log(user);
            const response = await postLoginForm(user);
            console.log("로그인 성공:", response.data);
        } catch (error) {
            console.error("로그인 실패:", error);
        }
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
            <button type="submit">로그인</button>
        </form>
    );
}

export default LoginForm;
