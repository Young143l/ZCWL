import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import svgr from "vite-plugin-svgr";

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        react({
            babel: {
                plugins: [["babel-plugin-react-compiler"]],
            },
        }),
        tailwindcss(),
        svgr(),
    ],
    server: {
        proxy: {
            "/api": {
                target: "http://localhost:8080", 
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, ""),
            },
            "/mcp": {
                target: "http://localhost:8001", 
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/mcp/, ""),
            },
        },
    },
});
