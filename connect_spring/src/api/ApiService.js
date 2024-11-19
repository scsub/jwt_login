import axios from "axios";

axios.defaults.withCredentials = true;

const API_URL = "http://localhost:8080/api";

const api = axios.create({
    baseURL: API_URL,
    withCredentials: true,
});

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;

        if (originalRequest._retry === undefined) {
            originalRequest._retry = false;
        }

        if (error.response && error.response.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;
            try {
                // 리프레시 토큰으로 액세스 토큰 재발급 요청
                await api.post("/reissue");
                // 재요청
                return api(originalRequest);
            } catch (refreshError) {
                // 리프레시 토큰도 만료된 경우 로그아웃 처리
                console.error("토큰 재발급 실패:", refreshError);
                // 로그아웃 로직 추가 (예: 로그인 페이지로 리다이렉트)
                window.location.href = "/login";
                return Promise.reject(refreshError);
            }
        }
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

export const logout = async () => {
    return await api.post(`/logout`);
};
