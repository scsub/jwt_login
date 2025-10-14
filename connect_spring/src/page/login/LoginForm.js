import { useState } from "react";
import { postLoginForm } from "../../api/ApiService";
import * as yup from "yup";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { useNavigate } from "react-router-dom";
import { setErrorMessage } from "../../utill/setErrorMessage";
import { getRole, saveRole } from "../../utill/auth";

function LoginForm() {
    const navigate = useNavigate();
    const schema = yup.object({
        username: yup.string().required("아이디를 입력해주세요"),
        password: yup.string().min(6, "비밀번호는 6자리 이상입니다").required("비밀번호를 입력해주세요"),
    });

    const {
        register,
        setError,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm({ resolver: yupResolver(schema) });

    const onSubmit = async (request) => {
        try {
            const req = await postLoginForm(request);
            saveRole(req.data.role);
            console.log(getRole());
            navigate("/");
        } catch (e) {
            const errors = e.response?.data?.errors;
            if (errors) {
                setErrorMessage(errors, setError);
            } else {
                console.error("알수없는 오류");
            }
        }
    };

    return (
        <div className="flex flex-col items-center justify-center h-screen bg-[#1d253d] ">
            <form onSubmit={handleSubmit(onSubmit)} className="w-full max-w-sm p-6 bg-gray-900 rounded-lg shadow">
                <div className="flex justify-center mb-5 text-3xl text-[#f6f8ff]">로그인</div>
                <div className="mb-4">
                    <input
                        type="text"
                        {...register("username")}
                        placeholder="아이디"
                        className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:ring-[#d4d4d4] bg-[#28314b] text-white"
                    />
                    <p className="text-red-600 mt-3">{errors.username?.message}</p>
                </div>
                <div>
                    <input
                        type="password"
                        {...register("password")}
                        placeholder="비밀번호"
                        className="w-full px-3 py-2 border rounded focus:outline-none focus:ring focus:ring-[#d4d4d4] bg-[#28314b] text-white"
                    />
                    <p className="text-red-600 mt-3">{errors.password?.message}</p>
                </div>
                {errors.login && <p style={{ color: "red" }}>{errors.login.message}</p>}

                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="w-full px-4 py-2 mt-4 text-white bg-blue-500 rounded hover:bg-blue-700 focus:outline-none focus:ring"
                >
                    로그인
                </button>
                <p className="w-full text-white text-center mt-4">아이디가 없다면?</p>
                <button
                    onClick={() => navigate("/signup")}
                    className="w-full px-4 py-2 mt-4 text-white bg-blue-500 rounded hover:bg-blue-700 focus:outline-none focus:ring"
                >
                    회원가입
                </button>
            </form>
        </div>
    );
}

export default LoginForm;
