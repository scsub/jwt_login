import { lazy, Suspense } from "react";
import { createBrowserRouter } from "react-router-dom";

const Loading = <div>Loading....</div>;
const LoginForm = lazy(() => import("../components/LoginForm.js"));
const Mess = lazy(() => import("../page/Message.js"));
const UserEdit = lazy(() => import("../components/UserEdit.js"));
const Signup = lazy(() => import("../components/SignupForm.js"));
const router = createBrowserRouter([
    {
        path: "/signup",
        element: (
            <Suspense fallback={Loading}>
                <Signup />
            </Suspense>
        ),
    },
    {
        path: "/login",
        element: (
            <Suspense fallback={Loading}>
                <LoginForm />
            </Suspense>
        ),
    },
    {
        path: "/test",
        element: (
            <Suspense fallback={Loading}>
                <Mess />
            </Suspense>
        ),
    },
    {
        path: "/user/edit",
        element: (
            <Suspense fallback={Loading}>
                <UserEdit />
            </Suspense>
        ),
    },
]);

export default router;
