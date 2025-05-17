import { useState } from "react";
import { postLoginForm } from "../api/ApiService";
import * as yup from "yup";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";

function LoginForm() {
    const schema = yup.object({
        username: yup.string().min(1).max(20).required("아이디를 입력해주세요"),
        password: yup
            .string()
            .min(6)
            .max(20)
            .required("비밀번호를 입력해주세요"),
    });

    const {
        register,
        setError,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm({ resolver: yupResolver(schema) });

    const onSubmit = (data) => {
        postLoginForm(data)
            .then(() => {
                console.log("로그인완료");
            })
            .catch((e) => {
                const msg = e.response?.data?.errors?.login;
                if (msg) {
                    setError("login", { message: msg });
                } else {
                    console.error("알수없는 오류", e);
                }
            });
    };
    return (
        <div className="flex flex-col items-center justify-center h-screen">
            <form
                onSubmit={handleSubmit(onSubmit)}
                className="w-full max-w-sm p-6 bg-orange-200 rounded-lg shadow"
            >
                <div className="mb-4">
                    <input
                        type="text"
                        {...register("username")}
                        placeholder="아이디"
                        className="w-full px-3 py-2 border rounded focus:outline-none focus:ring"
                    />
                    <p>{errors.username?.message}</p>
                </div>
                <div>
                    <input type="password" {...register("password")} />
                    <p>{errors.password?.message}</p>
                </div>
                {errors.login && (
                    <p style={{ color: "red" }}>{errors.login.message}</p>
                )}

                <button type="submit" disabled={isSubmitting}>
                    로그인
                </button>
            </form>
        </div>
    );
}

export default LoginForm;
