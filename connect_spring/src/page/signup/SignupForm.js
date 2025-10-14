import { useState } from "react";
import { signupPost } from "../../api/ApiService";
import * as yup from "yup";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { useNavigate } from "react-router-dom";
import { setErrorMessage } from "../../utill/setErrorMessage";
const schema = yup.object({
    username: yup
        .string()
        .min(4, "아이디는 최소4자 이상입니다다")
        .max(15, "아이디는 최대 15자 이하입니다")
        .required("아이디는 4~20자 이내입니다"),
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
    email: yup.string().email("이메일 형식을 지켜주세요").required("이메일을 입력해주세요"),
    phoneNumber: yup.string().matches(/^\d{10,25}$/, "전화번호는 10~25자리입니다"),
    address: yup.string().min(2, "주소는 최대 2자리 이상입니다").max(40).required(), // 이거를 지도에서 선택하는 것처럼 하고싶은데 일단 보류
});

function SignupForm() {
    const navigate = useNavigate();
    const {
        register,
        handleSubmit,
        setError,
        formState: { errors, isSubmitting },
    } = useForm({
        resolver: yupResolver(schema),
    });

    const onSubmit = async (request) => {
        try {
            await signupPost(request);
            alert("회원 가입 완료");
            navigate("/login");
        } catch (e) {
            const errors = e.response?.data?.errors;
            if (errors) {
                setErrorMessage(errors, setError);
            } else {
                alert("회원 가입 중 오류가 발생했습니다");
            }
        }
    };

    return (
        <div className="flex flex-col items-center justify-center h-screen bg-[#1d253d] ">
            <form onSubmit={handleSubmit(onSubmit)} className="w-6/12 p-6 bg-gray-900 rounded-lg shadow ">
                <div>
                    <input
                        {...register("username")}
                        placeholder="아이디"
                        className="w-full px-3 py-2 mb-2 border rounded focus:outline-none focus:ring focus:ring-[#d4d4d4] bg-[#28314b] text-white"
                    />
                    <p className="text-red-600 mb-2">{errors.username?.message}</p>
                </div>

                <div>
                    <input
                        type="password"
                        {...register("password")}
                        placeholder="비밀번호"
                        className="w-full px-3 py-2 mb-2 border rounded focus:outline-none focus:ring focus:ring-[#d4d4d4] bg-[#28314b] text-white"
                    />
                    <p className="text-red-600 mb-2">{errors.password?.message}</p>
                </div>
                <div>
                    <input
                        type="password"
                        {...register("passwordCheck")}
                        placeholder="비밀번호 확인"
                        className="w-full px-3 py-2 mb-2 border rounded focus:outline-none focus:ring focus:ring-[#d4d4d4] bg-[#28314b] text-white"
                    />
                    <p className="text-red-600 mb-2">{errors.passwordCheck?.message}</p>
                </div>
                <div>
                    <input
                        type="email"
                        {...register("email")}
                        placeholder="이메일"
                        className="w-full px-3 py-2 mb-2 border rounded focus:outline-none focus:ring focus:ring-[#d4d4d4] bg-[#28314b] text-white"
                    />
                    <p className="text-red-600 mb-2">{errors.email?.message}</p>
                </div>
                <div>
                    <input
                        type="tel"
                        {...register("phoneNumber")}
                        placeholder="전화번호"
                        className="w-full px-3 py-2 mb-2 border rounded focus:outline-none focus:ring focus:ring-[#d4d4d4] bg-[#28314b] text-white"
                    />
                    <p className="text-red-600 mb-2">{errors.phoneNumber?.message}</p>
                </div>
                <div>
                    <input
                        {...register("address")}
                        placeholder="주소"
                        className="w-full px-3 py-2 mb-2 border rounded focus:outline-none focus:ring focus:ring-[#d4d4d4] bg-[#28314b] text-white"
                    />
                    <p className="text-red-600 mb-2">{errors.address?.message}</p>
                </div>

                <button
                    disabled={isSubmitting}
                    className="w-full px-4 py-2 mt-4 text-white bg-blue-500 rounded hover:bg-blue-700 focus:outline-none focus:ring"
                    type="submit"
                >
                    회원가입
                </button>
            </form>
        </div>
    );
}

export default SignupForm;
