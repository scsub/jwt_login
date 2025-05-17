import { lazy, Suspense } from "react";
import { createBrowserRouter } from "react-router-dom";
const Loading = <div>Loading....</div>;
const LoginForm = lazy(() => import("../page/LoginForm.js"));
const UserEdit = lazy(() => import("../page/UserEdit.js"));
const Signup = lazy(() => import("../page/SignupForm.js"));
const HomePage = lazy(() => import("../page/Home.js"));
const Product = lazy(() => import("../page/Product.js"));
const Layout = lazy(() => import("../components/Layout.js"));
const ProductDetail = lazy(() => import("../page/ProductDetail.js"));
const Cart = lazy(() => import("../page/Cart.js"));
const ProductRegistraion = lazy(() => import("../page/ProductRegistration.js"));
const MyPage = lazy(() => import("../page/MyPage.js"));
const ChangePassword = lazy(() => import("../page/ChangePassword.js"));
const CreateCategory = lazy(() => import("../page/CreateCategory.js"));
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
                path: "edit",
                element: (
                    <Suspense fallback={Loading}>
                        <UserEdit />
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
        ],
    },
]);

export default router;
