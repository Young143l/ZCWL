import { StrictMode} from "react";
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
import CodeSF from "./components/CodeSF_mobile.tsx";
import Code from "./pages/Code/Code.tsx";
import CodeCP from "./components/CodeCP_mobile.tsx";
import Project from "./pages/Project/Project.tsx";
import Signin from "./pages/Signin.tsx";
import User from "./pages/User.tsx";
import { loader } from "@monaco-editor/react";
import * as monaco from "monaco-editor";


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
                path: "/project/:p_id",
                element: <Project />,
            },
            {
                path: "/project",
                element: <ProjectList />,
            },
            {
                path: "/code/sf/:sf_id",
                element: <CodeSF />,
            },
            {
                path: "/code/cp/:cp_id",
                element: <CodeCP />,
            },
            {
                path: "/code",
                element: <Code />,
            },
            {
                path: "/user",
                element: <User />,
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
    {
        path: "/signin",
        element: <Signin />,
    },
]);

loader.config({
    monaco,
});


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
