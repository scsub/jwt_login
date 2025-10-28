import axios from "axios";
import Product from "../page/product/Product";

axios.defaults.withCredentials = true;

const API_URL = process.env.REACT_APP_API_URL;

if (!API_URL) {
    console.warn("API_URL이 설정되지 않음");
}

const api = axios.create({
    baseURL: API_URL,
    withCredentials: true, // 리프레시 토큰을 사용하기 위해 토큰을 허용시킴
});

// 401예외 발생시 리프레시 토큰으로 엑세스 토큰을 재발급 받은 후 다시 사용자의 요청을 처리
api.interceptors.response.use(
    (res) => res, // 성공시 그냥 통과시킴
    async (error) => { // 실패시 아래 코드 실행
        const { config, response } = error; // config는 요청한 설정 정보, response는 상태코드 등이 담겨온다

        // 검증
        if (!response || response.status !== 401) return Promise.reject(error); // 401일때만 토큰 재발급해주는데 그게 아닐경우 걸러냄
        // 무한 루프 방지 _retry 커스텀을 사용해  이미 시도를 했는지 체크하고 재발급 요청을 실패하면 다시 이곳으로 오는데 그 요청을
        // 또 실패하면 또 이곳으로 오게되서 무한루프를 방지하기 위해 재발급 요청이 실패하면 예외를 던진다
        const url = config.url;
        const reissueURL = url.includes("/auth/reissue");
        if (config._retry || reissueURL) return Promise.reject(error);

        // 재발급을 위한 코드가 실행되면 _retry를 true로 만들어 루프시 위에서 걸림
        config._retry = true;

        try {
            // 토큰 재발급 이때 토큰은 httponly 쿠키로 이미 브라우저에 저장되어있어서 굳이 요청에 넣지않아도 된다
            // 물론 프론트와 백에 쿠키설정 withCredentials와 allowCredentials등 을 설정해놔야한다
            await api.post("/auth/reissue"); //

            // 토큰을 재발급받은 뒤
            // 폼데이터를 사용하는 경우 재시도할때 원본 폼데이터가 비어있을수있으니 복제하여 사용함
            if (config.data instanceof FormData) { // 폼데이터 인지 확인 JSON이면 복제하지 않음
                const newData = new FormData();
                config.data.forEach((v, k) => newData.append(k, v));
                config.data = newData;
            }

            // 재시도
            return api(config);
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
