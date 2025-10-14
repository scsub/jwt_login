import { lazy, Suspense } from "react";
import { createBrowserRouter } from "react-router-dom";

const Loading = <div>Loading....</div>;
const LoginForm = lazy(() => import("../page/login/LoginForm.js"));
const Signup = lazy(() => import("../page/signup/SignupForm.js"));
const HomePage = lazy(() => import("../page/Home.js"));
const Product = lazy(() => import("../page/product/Product.js"));
const Layout = lazy(() => import("../components/Layout.js"));
const ProductDetail = lazy(() => import("../page/product/ProductDetail.js"));
const Cart = lazy(() => import("../page/cart/Cart.js"));
const ProductRegistraion = lazy(() => import("../page/product/ProductRegistration.js"));
const MyPage = lazy(() => import("../page/user/MyPage.js"));
const ChangePassword = lazy(() => import("../page/user/ChangePassword.js"));
const CreateCategory = lazy(() => import("../page/category/CreateCategory.js"));
const PurchaseList = lazy(() => import("../page/order/PurchaseList.js"));
const CreateReview = lazy(() => import("../page/review/CreateReview.js"));
const router = createBrowserRouter([
    {
        path: "/",
        element: (
            <Suspense fallback={Loading}>
                <Layout />
            </Suspense>
        ),
        children: [
            {
                path: "",
                element: (
                    <Suspense fallback={Loading}>
                        <HomePage />
                    </Suspense>
                ),
            },
            {
                path: "signup",
                element: (
                    <Suspense fallback={Loading}>
                        <Signup />
                    </Suspense>
                ),
            },
            {
                path: "login",
                element: (
                    <Suspense fallback={Loading}>
                        <LoginForm />
                    </Suspense>
                ),
            },
            {
                path: "product",
                element: (
                    <Suspense fallback={Loading}>
                        <Product />
                    </Suspense>
                ),
            },
            {
                path: "product/:id",
                element: (
                    <Suspense fallback={Loading}>
                        <ProductDetail />
                    </Suspense>
                ),
            },
            {
                path: "/product/:id/review",
                element: (
                    <Suspense fallback={Loading}>
                        <CreateReview />
                    </Suspense>
                ),
            },
            {
                path: "cart",
                element: (
                    <Suspense fallback={Loading}>
                        <Cart />
                    </Suspense>
                ),
            },
            {
                path: "product/registraion",
                element: (
                    <Suspense fallback={Loading}>
                        <ProductRegistraion />
                    </Suspense>
                ),
            },
            {
                path: "myPage",
                element: (
                    <Suspense fallback={Loading}>
                        <MyPage />
                    </Suspense>
                ),
            },
            {
                path: "myPage/ChangePassword",
                element: (
                    <Suspense fallback={Loading}>
                        <ChangePassword />
                    </Suspense>
                ),
            },
            {
                path: "/admin/categories",
                element: (
                    <Suspense fallback={Loading}>
                        <CreateCategory />
                    </Suspense>
                ),
            },
            {
                path: "/myPage/purchaseList",
                element: (
                    <Suspense fallback={Loading}>
                        <PurchaseList />
                    </Suspense>
                ),
            },
        ],
    },
]);

export default router;
