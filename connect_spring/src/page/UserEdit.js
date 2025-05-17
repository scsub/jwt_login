import { useState } from "react";
import { postLoginForm } from "../api/ApiService";
import * as yup from "yup";

function UserEdit() {
    const schema = yup.object({
        username: yup.string().min().max().required("아이디를 입력해주세요"),
        password: yup.string().min().max().required("비밀번호를 입력해주세요"),
    });
    const [user, setUser] = useState({
        username: "",
        password: "",
    });

    const handleSubmit = async (e) => {
        e.preventDefault();
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
                <label>아이디</label>
                <input type="text" name="username" value={user.username} onChange={handleChange} />
            </div>
            <div>
                <label>비밀번호</label>
                <input type="password" name="password" value={user.password} onChange={handleChange} />
            </div>
        </form>
    );
}

export default UserEdit;
