import axios from "axios";
import Product from "../page/product/Product";

axios.defaults.withCredentials = true;

const API_URL = process.env.REACT_APP_API_URL;

if (!API_URL) {
    console.warn("API_URL이 설정되지 않음");
}

const api = axios.create({
    baseURL: API_URL,
    withCredentials: true, // 쿠키 허용
});

// 응답을 가로채서 401에러 발생시 토큰 재발급
api.interceptors.response.use(
    (res) => res,
    async (error) => {
        const { config, response } = error;
        if (!response || response.status !== 401) return Promise.reject(error);
        if (config._retry || config.url === "/auth/reissue") return Promise.reject(error);

        config._retry = true;

        try {
            await api.post("/auth/reissue"); // ① refresh → access 재발급

            /* ②  FormData 인 경우 다시 복사해서 넣어준다 */
            if (config.data instanceof FormData) {
                const newData = new FormData();
                config.data.forEach((v, k) => newData.append(k, v));
                config.data = newData;
            }

            return api(config); // ③ 원‑요청 재시도
        } catch (e) {
            console.error("토큰 재발급 실패", e);
            return Promise.reject(e);
        }
    }
);

export default api;

export const getMessage = async () => {
    return await api.get(`/test`);
};

export const postLoginForm = async (user) => {
    return await api.post(`/auth/login`, user);
}; // 쿠키를 포함할수 있게된다 물론 cors의 allowCredentials 설정을 해줘야함

export const signupPost = async (user) => {
    return await api.post(`/auth/signup`, user);
};
export const logout = async () => {
    return await api.post(`/auth/logout`);
};
export const getProducts = async () => {
    return await api.get(`/products`);
};

export const getProductById = async (id) => {
    return await api.get(`/products/${id}`);
};

export const postAddItemInCart = async (id, quantity) => {
    return await api.post(`/carts/items`, {
        productId: id,
        quantity: quantity,
    });
};

export const getCartItems = async () => {
    return await api.get(`/carts`);
};

export const postOrder = async () => {
    return await api.post(`/orders`);
};

export const postRegistationProduct = async () => {
    return await api.post(`/products`);
};

export const getCategories = async () => {
    return await api.get(`/categories`);
};

export const deleteCartItem = async (cartItemId) => {
    return await api.delete(`/carts/items/${cartItemId}`);
};

export const patchCartItemQuantity = async (cartItemId, quantity) => {
    return await api.patch(`/carts/items/${cartItemId}`, {
        quantity: quantity,
    });
};

export const getUserProfile = async () => {
    return await api.get(`/users/me`);
};

export const patchUserProfile = async (userProfile) => {
    return await api.patch(`/users/me`, {
        email: userProfile.email,
        phoneNumber: userProfile.phoneNumber,
        address: userProfile.address,
    });
};
export const patchChangePassword = async (userPasswords) => {
    return await api.patch(`/users/me/password`, {
        originalPassword: userPasswords.originalPassword,
        newPassword: userPasswords.newPassword,
        confirmNewPassword: userPasswords.confirmNewPassword,
    });
};

export const deleteUser = async () => {
    return await api.delete(`users/me`);
};

export const createCategotyPost = async ({ name, parentId }) => {
    return await api.post("categories", { name, parentId });
};

export const createProductPost = async (fd) => {
    return await api.post("/products", fd);
};

export const getProductBySmallCategoryId = async (smallCategoryId) => {
    return await api.get(`/products/category/${smallCategoryId}`);
};

export const getOrders = async () => {
    return await api.get(`/orders`);
};

export const patchCancelOrder = async (orderId) => {
    return await api.patch(`/orders/${orderId}`);
};

export const getSerchProduct = async (productName) => {
    return await api.get(`/products/search`, { params: { query: productName } });
};

export const postCreateReview = async ({ productId, recommend, content }) => {
    return await api.post(`reviews`, {
        productId: productId,
        recommend: recommend,
        content: content,
    });
};

export const getProductReviews = async (productId) => {
    return await api.get(`reviews/products/${productId}`);
};
