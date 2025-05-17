import { useEffect, useState } from "react";
import { deleteUser, getUserProfile, patchUserProfile } from "../api/ApiService";
import { Link, useNavigate } from "react-router-dom";

function MyPage() {
    const [userProfile, setUserProfile] = useState([]);
    const navigate = useNavigate();
    useEffect(() => {
        getUserProfile()
            .then((res) => setUserProfile(res.data))
            .catch((e) => {
                console.error(e);
            });
    }, []);

    const handleChangeProfile = async (e) => {
        setUserProfile({ ...userProfile, [e.target.name]: e.target.value });
    };

    const handleProfileSubmit = async (e) => {
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
        <div>
            <form onSubmit={handleProfileSubmit}>
                <div>{userProfile.username} 페이지</div>
                <div>
                    <label>비밀번호 교체 : </label>
                    <Link to={`changePassword`}>
                        <button>비밀번호 교체</button>
                    </Link>
                </div>
                <div>
                    <label>이메일 : </label>
                    <input name="email" type="email" value={userProfile.email} onChange={handleChangeProfile}></input>
                </div>
                <div>
                    <label>전화번호 : </label>
                    <input name="phoneNumber" value={userProfile.phoneNumber} onChange={handleChangeProfile}></input>
                </div>
                <div>
                    <label>주소 : </label>
                    <input name="address" value={userProfile.address} onChange={handleChangeProfile}></input>
                </div>
                <button type="submit">제출하기</button>
            </form>

            <div>
                <button onClick={handleUserDelete}>회원탈퇴</button>
            </div>
        </div>
    );
}

export default MyPage;
