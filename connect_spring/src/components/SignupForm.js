import { useState } from "react";
import { signupPost } from "../api/ApiService";

function SignupFrom() {
    const [user, setUser] = useState({
        username: "",
        password: "",
        email: "",
        phoneNumber: "",
        address: "",
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            console.log(user);
            const data = await signupPost(user);
            console.log("회원가입 성공:", data);
        } catch (error) {
            console.error("회원가입 실패:", error);
        }
    };

    const handleChange = async (e) => {
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
            <div>
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
            </div>

            <button type="submit">회원가입</button>
        </form>
    );
}

export default SignupFrom;
