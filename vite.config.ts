import { defineConfig } from "vite";
import tailwindcss from "@tailwindcss/vite";
import svgr from "vite-plugin-svgr";
import babel from '@rolldown/plugin-babel'
import react, { reactCompilerPreset } from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
    plugins: [
        react(),
        babel({ presets: [reactCompilerPreset()] }),
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
            "/ws": {
                target: "ws://localhost:8080",
                changeOrigin: true,
                ws: true,
            },
        },
        // 开发服务器优化
        host: 'localhost',
        port: 3000,
        open: true,
        // 启用 HMR 严格模式，提高热更新准确性
        hmr: {
            overlay: true,
        },
    },
    build: {
        // 构建目标设为 es2022，与 tsconfig.app.json 保持一致，充分利用现代浏览器特性
        target: "es2022",
        // 启用 CSS 代码分割
        cssCodeSplit: true,
        // CSS 压缩使用 lightningcss（Vite 内置支持，速度更快、压缩率更高）
        cssMinify: "lightningcss",
        // 启用源码映射（生产环境建议关闭以提升加载速度）
        sourcemap: false,
        // 压缩配置（使用 oxc，速度更快）
        minify: "oxc",
        // 小于 4KB 的资源内联为 base64，减少 HTTP 请求
        assetsInlineLimit: 4096,
        // 模块预加载 polyfill，确保动态导入兼容性
        modulePreload: {
            polyfill: true,
        },
        // 代码分割配置
        rollupOptions: {
            output: {
                // 手动代码分割策略 - 优化分组，减少重复依赖
                manualChunks(id: string) {
                    if (id.includes("node_modules")) {
                        // Ant Design 相关
                        if (id.includes("antd") && !id.includes("@ant-design/icons")) {
                            return "ui-vendor";
                        }
                        if (id.includes("@ant-design/icons")) {
                            return "icon-vendor";
                        }
                        // Monaco Editor 相关 - 进一步细分
                        if (id.includes("monaco-editor/esm/vs/language")) {
                            // 语言相关功能单独分包
                            if (id.includes("typescript")) {
                                return "monaco-ts";
                            }
                            if (id.includes("javascript")) {
                                return "monaco-js";
                            }
                            if (id.includes("json")) {
                                return "monaco-json";
                            }
                            if (id.includes("html")) {
                                return "monaco-html";
                            }
                            if (id.includes("css")) {
                                return "monaco-css";
                            }
                            return "monaco-languages";
                        }
                        if (id.includes("monaco-editor/esm/vs/editor")) {
                            // 编辑器核心功能
                            return "monaco-editor-core";
                        }
                        if (id.includes("monaco-editor")) {
                            // 其他 monaco 相关
                            return "monaco-base";
                        }
                        // 路由和状态管理
                        if (id.includes("react-router-dom") || id.includes("zustand")) {
                            return "router-state";
                        }
                        // Markdown 渲染
                        if (id.includes("react-markdown") || id.includes("remark-gfm") || id.includes("github-markdown-css")) {
                            return "markdown-vendor";
                        }
                        // 工具库 - console-feed 单独分包以处理 eval 警告
                        if (id.includes("console-feed")) {
                            return "console-feed-vendor";
                        }
                        if (id.includes("ts-md5") || id.includes("nprogress") || id.includes("react-device-detect")) {
                            return "util-vendor";
                        }
                        // 其他大型依赖可以考虑单独分包
                        if (id.includes("jsmind")) {
                            return "mindmap-vendor";
                        }
                    }
                },
                // 资源文件命名规则
                chunkFileNames: "assets/js/[name]-[hash].js",
                entryFileNames: "assets/js/[name]-[hash].js",
                assetFileNames: (assetInfo) => {
                    const name = assetInfo.name ?? "";
                    if (/\.(png|jpe?g|gif|svg|webp|ico)$/i.test(name)) {
                        return "assets/images/[name]-[hash][extname]";
                    }
                    if (/\.(css)$/i.test(name)) {
                        return "assets/css/[name]-[hash][extname]";
                    }
                    if (/\.(woff2?|ttf|otf|eot)$/i.test(name)) {
                        return "assets/fonts/[name]-[hash][extname]";
                    }
                    return "assets/[name]-[hash][extname]";
                },
            },
        },
        // 设置 chunk 大小警告阈值（monaco-editor 基础包 ~4.1MB，调高避免无意义误报）
        chunkSizeWarningLimit: 5000,
        // 启用压缩后大小报告
        reportCompressedSize: true,
    },
    // 优化依赖预构建
    optimizeDeps: {
        // 需要预构建的依赖
        include: [
            "react",
            "react-dom",
            "antd",
            "@ant-design/icons",
            "@monaco-editor/react",
            "monaco-editor",
            "react-router-dom",
            "zustand",
            "react-markdown",
            "remark-gfm",
            "console-feed",
            "react-device-detect",
            "github-markdown-css",
            "nprogress",
            "ts-md5",
            "jsmind",
        ],
        // 排除某些依赖（如果有问题可以取消注释）
        // exclude: [],
        // 启用 esbuild 预构建缓存，加快后续启动速度
        force: false,
    },
    // 静态资源处理优化
    assetsInclude: ['**/*.md', '**/*.json'],
    // 环境变量前缀，确保只有指定前缀的环境变量被暴露给客户端
    envPrefix: ['VITE_', 'npm_'],
});
