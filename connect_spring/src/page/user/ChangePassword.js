import { useEffect, useState } from "react";
import { patchChangePassword } from "../../api/ApiService";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";

const schema = yup.object({
    originalPassword: yup
        .string()
        .min(6, "비밀번호는 최대 6자리 이상입니다")
        .max(20, "비밀번호는 최대 20자리 이하입니다")
        .required("비밀번호는 6~20자"),
    newPassword: yup
        .string()
        .min(6, "비밀번호는 최대 6자리 이상입니다")
        .max(20, "비밀번호는 최대 20자리 이하입니다")
        .required("비밀번호는 6~20자"),
    confirmNewPassword: yup
        .string()
        .min(6, "비밀번호는 최대 6자리 이상입니다")
        .max(20, "비밀번호는 최대 20자리 이하입니다")
        .required("비밀번호는 6~20자"),
});

function ChangePassword() {
    const [passwords, setPasswords] = useState([]);
    const {
        register,
        setError,
        handleSubmit,
        formState: { errors, isSubmitting },
    } = useForm({
        resolver: yupResolver(schema),
    });

    useEffect(() => {}, []);

    const onSubmit = async (e) => {
        e.preventDefault();
        await patchChangePassword(passwords)
            .then(console.log("비밀번호 변경완료"))
            .catch((e) => console.log(e));
    };

    return (
        <div className="flex flex-col items-center justify-center h-screen bg-[#1d253d] ">
            <form onSubmit={handleSubmit(onSubmit)} className="w-3/5  bg-white rounded-lg shadow p-6">
                <div className="flex justify-center text-3xl font-bold mb-4">비밀번호 변경</div>

                <div className="flex justify-between items-center border-b py-2">
                    <span className="font-medium w-1/3">현재 비밀번호 </span>
                    <div className="flex flex-col w-2/3">
                        <input
                            name="originalPassword"
                            {...register("originalPassword")}
                            className="bg-gray-200 rounded px-2 py-1 "
                            placeholder="현재 비밀번호"
                        ></input>
                        <p className="text-red-600 ">{errors.originalPassword?.message}</p>
                    </div>
                </div>
                <div className="flex justify-between items-center border-b py-2">
                    <span>새 비밀번호 </span>
                    <div className="flex flex-col w-2/3">
                        <input
                            name="newPassword"
                            {...register("newPassword")}
                            className="bg-gray-200 rounded px-2 py-1 "
                            placeholder="새 비밀번호"
                        ></input>
                        <p className="text-red-600">{errors.newPassword?.message}</p>
                    </div>
                </div>
                <div className="flex justify-between items-center border-b py-2">
                    <span>새 비밀번호 확인 </span>
                    <div className="flex flex-col w-2/3">
                        <input
                            name="confirmNewPassword"
                            {...register("confirmNewPassword")}
                            className="bg-gray-200 rounded px-2 py-1 "
                            placeholder="새 비밀번호 확인"
                        ></input>
                        <p className="text-red-600">{errors.confirmNewPassword?.message}</p>
                    </div>
                </div>
                <button type="submit" className="block mt-4 ml-auto bg-blue-600 text-white rounded px-4 py-2">
                    비밀번호 변경
                </button>
            </form>
        </div>
    );
}

export default ChangePassword;
