import axios from "axios";

axios.defaults.withCredentials = true;

const API_URL = "https://localhost:8443/api";

const api = axios.create({
    baseURL: API_URL,
    withCredentials: true, // 쿠키 허용
});

// 응답을 가로채서 401에러 발생시 토큰 재발급
api.interceptors.response.use(
    (response) => response, // 응답이 성공적이면 그대로 반환
    async (error) => {
        // config에는 요청 정보가 들어있음 url, method data 등
        const originalRequest = error.config;

        // 첫 에러 발생시 커스텀 _retry를 초기화 해줌
        if (originalRequest._retry === undefined) {
            originalRequest._retry = false;
        }

        // 에러 응답이 존재하며 코드가 401일때 + 재요청이 아닐때
        if (error.response && error.response.status === 401 && !originalRequest._retry) {
            // 동일 요청이 반복되지 않게 ture
            originalRequest._retry = true;
            try {
                // 리프레시 토큰으로 액세스 토큰 재발급 요청
                await api.post("/reissue");
                // 재요청
                return api(originalRequest);
            } catch (refreshError) {
                // 리프레시 토큰도 만료된 경우 로그아웃 처리
                console.error("토큰 재발급 실패:", refreshError);
                // 로그인 페이지로 리다이렉트
                window.location.href = "/login";
                return Promise.reject(refreshError);
            }
        }
        // 401이 아니거나 이미 재시도된 요청일 경우 에러 반환
        return Promise.reject(error);
    }
);

export default api;

export const getMessage = async () => {
    return await api.get(`/test`);
};

export const postLoginForm = async (user) => {
    return (await api.post(`/login`, user)).data;
}; // 쿠키를 포함할수 있게된다 물론 cors의 allowCredentials 설정을 해줘야함

export const signupPost = async (user) => {
    return await api.post(`/signup`, user);
};
export const logout = async () => {
    return await api.post(`/logout`);
};
