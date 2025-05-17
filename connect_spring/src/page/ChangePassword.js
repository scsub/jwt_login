import { useState } from "react";
import { patchChangePassword } from "../api/ApiService";

function ChangePassword() {
    const [passwords, setPasswords] = useState([]);

    const submitForm = async (e) => {
        e.preventDefault();
        await patchChangePassword(passwords)
            .then(console.log("비밀번호 변경완료"))
            .catch((e) => console.log(e));
    };

    const changePassword = (e) => {
        setPasswords({ ...passwords, [e.target.name]: e.target.value });
    };
    return (
        <div>
            <form onSubmit={submitForm}>
                <div>비밀 번호 변경 페이지</div>
                <div>
                    <label>현재 비밀번호 :</label>
                    <input name="originalPassword" onChange={changePassword} value={passwords.originalPassword}></input>
                </div>

                <div>
                    <label>새 비밀번호 : </label>
                    <input name="newPassword" onChange={changePassword} value={passwords.newPassword}></input>
                </div>

                <div>
                    <label>새 비밀번호 확인 : </label>
                    <input
                        name="confirmNewPassword"
                        onChange={changePassword}
                        value={passwords.confirmNewPassword}
                    ></input>
                </div>
                <button type="submit">제출</button>
            </form>
        </div>
    );
}

export default ChangePassword;
