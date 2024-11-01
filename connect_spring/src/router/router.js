import { lazy, Suspense } from "react";
import { createBrowserRouter } from "react-router-dom";

const LoginForm = lazy(() => import("../components/LoginForm.js"));
const Mess = lazy(() => import("../page/Message.js"));
const Loading = <div>Loading....</div>;

const router = createBrowserRouter([
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
]);

export default router;
