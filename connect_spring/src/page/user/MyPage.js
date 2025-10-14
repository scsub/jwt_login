import { useEffect, useState } from "react";
import { deleteUser, getUserProfile, patchUserProfile } from "../../api/ApiService";
import { Link, useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";

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
function MyPage() {
    const {
        register,
        setError,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm({ resolver: yupResolver(schema) });

    const [userProfile, setUserProfile] = useState([]);
    const navigate = useNavigate();
    useEffect(() => {
        getUserProfile()
            .then((res) => setUserProfile(res.data))
            .catch((e) => {
                console.error(e);
            });
    }, []);

    const onSubmit = async (e) => {
        e.preventDefault();
        patchUserProfile(userProfile).catch((e) => {
            console.error(e);
        });
    };

    const handleUserDelete = async (e) => {
        const deleteCheck = window.confirm("회원 탈퇴 하시겠습니까?");
        if (!deleteCheck) {
            return;
        }
        deleteUser()
            .then(() => {
                alert("회원탈퇴 완료");
                navigate("/");
            })
            .catch((e) => {
                console.log(e);
                alert("회원탈퇴 할수없음");
            });
    };

    return (
        <div className="flex flex-col items-center justify-center h-screen bg-[#1d253d] ">
            <form className="w-3/5  bg-white rounded-lg shadow p-6">
                <div className="text-center text-2xl font-bold mb-4 ">{userProfile.username}님의 프로필</div>

                <div className="flex justify-between items-center border-b py-2">
                    <span className="font-medium">비밀번호 교체</span>
                    <button type="button" onClick={() => navigate("changePassword")} className="text-sm text-blue-600 hover:underline">
                        변경하기
                    </button>
                </div>

                <div className="flex justify-between items-center border-b py-2">
                    <label className="font-medium w-1/3">이메일</label>
                    <div className="flex flex-col w-2/3">
                        <input
                            type="email"
                            {...register("email")}
                            placeholder="이메일"
                            className="bg-gray-200 rounded px-2 py-1 focus:outline-none"
                        />
                        {errors.email && <p className="text-red-600">{errors.email.message}</p>}
                    </div>
                </div>

                <div className="flex justify-between items-center border-b py-2">
                    <label className="font-medium">전화번호</label>
                    <div className="flex flex-col w-2/3">
                        <input
                            {...register("phoneNumber")}
                            placeholder="전화번호"
                            className=" bg-gray-200 rounded px-2 py-1 focus:outline-none"
                        />
                        {errors.email && <p className="text-red-600">{errors.phoneNumber.message}</p>}
                    </div>
                </div>

                <div className="flex justify-between items-center border-b py-2">
                    <label className="font-medium">주소</label>
                    <div className="flex flex-col w-2/3">
                        <input {...register("address")} placeholder="주소" className=" bg-gray-200 rounded px-2 py-1 focus:outline-none" />
                        {errors.email && <p className="text-red-600">{errors.address.message}</p>}
                    </div>
                </div>

                <div className="mt-4 text-right">
                    <button
                        type="submit"
                        className="bg-blue-600 text-white rounded px-4 py-2 hover:bg-blue-700"
                        onClick={handleSubmit(onSubmit)}
                    >
                        제출하기
                    </button>
                </div>
            </form>
        </div>
    );
}

export default MyPage;
