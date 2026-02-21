import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import {
    createBrowserRouter,
    RouterProvider,
    ScrollRestoration,
} from "react-router-dom";
import Main_layout from "./layout/Main_layout.tsx";
import NotFound from "./pages/NotFound.tsx";
import Home from "./pages/Home.tsx";
import Login from "./pages/Login.tsx";
import DocumentList from "./pages/Document/DocumentList.tsx";
import DocumentContent from "./pages/Document/DocumentContent.tsx";
import "./index.css";
import DocumentDirectory from "./pages/Document/DocumentDirectory.tsx";
import ProjectList from "./pages/Project/ProjectList.tsx";
import { ConfigProvider } from "antd";
import CodeSF from "./pages/Code/CodeSF.tsx";
const router = createBrowserRouter([
    {
        element: (
            <>
                <Main_layout />
                <ScrollRestoration />
            </>
        ),
        children: [
            {
                path: "/",
                element: <Home />,
            },
            {
                path: "/document/:d_id/:c_id",
                element: <DocumentContent />,
            },
            {
                path: "/document/:d_id",
                element: <DocumentDirectory />,
            },
            {
                path: "/document",
                element: <DocumentList />,
            },
            {
                path: "/project",
                element: <ProjectList />,
            },
            {
                path: "/code/sf",
                element: <CodeSF />,
            },
            {
                path: "*",
                element: <NotFound />,
            },
        ],
    },
    {
        path: "/login",
        element: <Login />,
    },
]);

createRoot(document.getElementById("root")!).render(
    <StrictMode>
        <ConfigProvider
            theme={{
                token: {
                    colorPrimary: "#13c2c2",
                    colorPrimaryBorder: "#87e8de",
                },
            }}
        >
            <RouterProvider router={router} />
        </ConfigProvider>
    </StrictMode>,
);
