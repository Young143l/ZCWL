import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import Main_layout from "./layout/Main_layout.tsx";
import NotFound from "./pages/NotFound.tsx";
import Home from "./pages/Home.tsx";
import Login from "./pages/Login.tsx";
import DocumentList from "./pages/DocumentList.tsx";
import DocumentContent from "./pages/DocumentContent.tsx";
import "./index.css";
import DocumentDirectory from "./pages/DocumentDirectory.tsx";
const router = createBrowserRouter([
    {
        element: <Main_layout />,
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
        <RouterProvider router={router} />
    </StrictMode>,
);
