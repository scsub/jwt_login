import axios from "axios";
import { useEffect, useState } from "react";
import api from "../api/ApiService";

function Message() {
    const [message, setMessage] = useState("11");

    useEffect(() => {
        api.get("/test")
            .then((response) => {
                setMessage(response.data);
            })
            .catch((error) => {
                console.error("에러 발생", error);
            });
    }, []);
    return (
        <div>
            <h1>스프링 부트 연동</h1>
            <h1>{message}</h1>
        </div>
    );
}
export default Message;
