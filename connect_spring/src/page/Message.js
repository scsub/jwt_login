import axios from "axios";
import { useEffect, useState } from "react";
import api, { getMessage, logout } from "../api/ApiService";

function Message() {
    const [message, setMessage] = useState("11");

    useEffect(() => {
        getMessage()
            .then((response) => {
                setMessage(response.data);
            })
            .catch((error) => {
                console.error("에러 발생", error);
            });
    }, []);

    const handleLogout = () => {
        logout()
            .then((response) => {
                console.log("로그아웃", response.data);
            })
            .catch((error) => {
                console.error(error);
            });
    };
    return (
        <div>
            <h1>스프링 부트 연동</h1>
            <h1>{message}</h1>
            <button onClick={handleLogout}>로그아웃</button>
        </div>
    );
}
export default Message;
