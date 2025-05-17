import { useState } from "react";
import { signupPost } from "../api/ApiService";
import * as yup from "yup";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
const schema = yup.object({
    username: yup
        .string()
        .min(4, "아이디는 최소4자 이상")
        .max(15, "아이디는 최대 15자 이하")
        .required("아이디는 4~20자"),
    password: yup
        .string()
        .min(6, "비밀번호는 최대 6자리 이상입니다")
        .max(20, "비밀번호는 최대 20자리 이하입니다")
        .required("비밀번호는 6~20자"),
    passwordCheck: yup
        .string()
        .min(6, "비밀번호는 최대 6자리 이상입니다")
        .max(20, "비밀번호는 최대 20자리 이하입니다")
        .required("비밀번호는 6~20자"),
    email: yup.string().email("이메일 형식을 지켜주세요").required(),
    phoneNumber: yup.string().matches(/^\d{10,25}$/, "전화번호는 10~25자리입니다"),
    address: yup.string().min(2, "주소는 최대 2자리 이상입니다").max(40).required(), // 이거를 지도에서 선택하는 것처럼 하고싶은데 일단 보류
});

function SignupForm() {
    const {
        register,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm({
        resolver: yupResolver(schema),
    });

    const onSubmit = async (data) => {
        try {
            await signupPost(data);
            alert("회원 가입 완료");
        } catch (e) {
            if (e.response?.data) {
                Object.entries(e.response.data).forEach(([field, msg]) => setError(field, { message: msg }));
            } else {
                console.error(e);
            }
        }
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)}>
            <div>
                <label>아이디</label>
                <input {...register("username")} />
                <p className="err">{errors.username?.message}</p>
            </div>

            <div>
                <label>비밀번호</label>
                <input type="password" {...register("password")} />
                <p className="err">{errors.password?.message}</p>
            </div>
            <div>
                <label>비밀번호 확인</label>
                <input type="password" {...register("passwordCheck")} />
                <p className="err">{errors.passwordCheck?.message}</p>
            </div>
            <div>
                <label>이메일</label>
                <input type="email" {...register("email")} />
                <p className="err">{errors.email?.message}</p>
            </div>

            <div>
                <label>전화번호</label>
                <input type="tel" {...register("phoneNumber")} />
                <p className="err">{errors.phoneNumber?.message}</p>
            </div>

            <div>
                <label>주소</label>
                <input {...register("address")} />
                <p className="err">{errors.address?.message}</p>
            </div>

            <button disabled={isSubmitting}>회원가입</button>
        </form>
    );
}

export default SignupForm;
