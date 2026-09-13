import { useState, type FC } from "react";
import { Card, List, Tag, theme } from "antd";
import {
    FireOutlined,
    RightOutlined,
} from "@ant-design/icons";
import useIsDark from "../status/IsDark_status";

// 新闻数据接口
interface NewsItem {
    id: number;
    title: string;
    summary: string;
    tag: string;
    tagColor: string;
    date: string;
    hot: boolean;
    url: string;
}

// 模拟编程新闻数据
const newsData: NewsItem[] = [
    {
        id: 1,
        title: "React 19 正式发布，带来全新编译器",
        summary: "React 团队宣布 React 19 正式版发布，包含 React Compiler 自动优化、Server Components 稳定版、Actions 等重要更新...",
        tag: "React",
        tagColor: "blue",
        date: "2026-04-10",
        hot: true,
        url: "https://react.dev/blog/2024/04/25/react-19",
    },
    {
        id: 2,
        title: "TypeScript 5.5 新特性：类型推断再升级",
        summary: "TypeScript 5.5 带来了更智能的类型推断、性能优化以及对 JSDoc 的改进，让类型检查更加精准高效...",
        tag: "TypeScript",
        tagColor: "cyan",
        date: "2026-04-09",
        hot: true,
        url: "https://devblogs.microsoft.com/typescript/announcing-typescript-5-5/",
    },
    {
        id: 3,
        title: "Node.js 22 发布，原生支持 WebSocket",
        summary: "Node.js 22 版本正式发布，带来原生 WebSocket 客户端支持、V8 引擎升级、ESM 改进等多项新特性...",
        tag: "Node.js",
        tagColor: "green",
        date: "2026-04-08",
        hot: false,
        url: "https://nodejs.org/en/blog/announcements/v22-release-announce",
    },
    {
        id: 4,
        title: "Vue 3.5 即将发布，响应式系统优化",
        summary: "Vue 核心团队透露 3.5 版本计划，将进一步优化响应式系统性能，改进 defineModel 和泛型组件支持...",
        tag: "Vue",
        tagColor: "green",
        date: "2026-04-07",
        hot: true,
        url: "https://blog.vuejs.org/posts/vue-3-5",
    },
    {
        id: 5,
        title: "Rust 1.78 发布，提升编译速度",
        summary: "Rust 团队发布 1.78 版本，编译器性能提升显著，新增多项 lint 规则，改进错误信息提示...",
        tag: "Rust",
        tagColor: "orange",
        date: "2026-04-06",
        hot: false,
        url: "https://blog.rust-lang.org/2024/05/02/Rust-1.78.0.html",
    },
    {
        id: 6,
        title: "GitHub Copilot X 新增代码解释功能",
        summary: "GitHub 宣布 Copilot X 新功能上线，AI 可以解释任意代码片段的工作原理，帮助开发者更好理解代码...",
        tag: "AI",
        tagColor: "purple",
        date: "2026-04-05",
        hot: true,
        url: "https://github.blog/2024-04-29-github-copilot-workspace/",
    },
    {
        id: 7,
        title: "Docker Desktop 4.30 优化开发者体验",
        summary: "Docker 发布 Desktop 4.30 版本，新增资源监控面板、改进容器调试工具、优化 Mac 平台性能...",
        tag: "Docker",
        tagColor: "blue",
        date: "2026-04-04",
        hot: false,
        url: "https://www.docker.com/blog/docker-desktop-4-30/",
    },
    {
        id: 8,
        title: "CSS Container Queries 全面支持",
        summary: "主流浏览器已全面支持 CSS Container Queries，开发者可以根据容器大小而非视口进行响应式设计...",
        tag: "CSS",
        tagColor: "pink",
        date: "2026-04-03",
        hot: false,
        url: "https://developer.mozilla.org/zh-CN/docs/Web/CSS/CSS_containment/Container_queries",
    },
    {
        id: 9,
        title: "Vite 5.3 发布，预热功能增强",
        summary: "Vite 发布 5.3 版本，优化了预构建和 HMR 性能，新增实验性功能，改进对 SSR 的支持...",
        tag: "工具",
        tagColor: "volcano",
        date: "2026-04-02",
        hot: true,
        url: "https://vitejs.dev/blog/announcing-vite5-3",
    },
    {
        id: 10,
        title: "PostgreSQL 17 测试版性能大幅提升",
        summary: "PostgreSQL 17 首个测试版发布，查询性能提升显著，新增 JSON 函数，改进并发控制机制...",
        tag: "数据库",
        tagColor: "geekblue",
        date: "2026-04-01",
        hot: false,
        url: "https://www.postgresql.org/about/news/postgresql-17-beta-1-released-2865/",
    },
];

const News_components: FC = () => {
    const { isDark } = useIsDark();
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const [hoveredId, setHoveredId] = useState<number | null>(null);

    return (
        <Card
            className={`w-full ${isDark ? "bg-[#1E1E1E] border-gray-700" : "bg-white border-gray-200"}`}
            styles={{
                body: {
                    padding: 0,
                },
            }}
        >
            {/* Header 部分 */}
            <div
                className={`flex items-center justify-between px-5 py-4 border-b ${isDark ? "border-gray-700" : "border-gray-200"}`}
            >
                <div
                    className="flex flex-col border-l-4 pl-2.5"
                    style={{ borderColor: pColor }}
                >
                    <span
                        className={`text-lg font-bold ${isDark ? "text-white" : "text-gray-800"}`}
                    >
                        编程资讯
                    </span>
                    <span
                        className="text-base font-medium tracking-wider opacity-60"
                        style={{ color: pColor }}
                    >
                        Tech News
                    </span>
                </div>
            </div>

            {/* 新闻列表 */}
            <div className="px-5 py-4">
                <List
                    dataSource={newsData}
                    renderItem={(item) => (
                        <List.Item
                            className={`px-3! py-3! rounded-lg transition-all duration-200 cursor-pointer ${
                                hoveredId === item.id
                                    ? isDark
                                        ? "bg-gray-800"
                                        : "bg-gray-50"
                                    : ""
                            }`}
                            onMouseEnter={() => setHoveredId(item.id)}
                            onMouseLeave={() => setHoveredId(null)}
                        >
                            <a
                                href={item.url}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="flex items-start gap-4 w-full no-underline"
                            >
                                {/* 左侧：标签和日期 */}
                                <div className="flex flex-col items-center gap-2 min-w-20">
                                    <Tag color={item.tagColor} className="m-0">
                                        {item.tag}
                                    </Tag>
                                    <span
                                        className={`text-xs ${isDark ? "text-gray-500" : "text-gray-400"}`}
                                    >
                                        {item.date.slice(5)}
                                    </span>
                                </div>

                                {/* 右侧：标题和摘要 */}
                                <div className="flex-1 min-w-0">
                                    <div className="flex items-center gap-2 mb-1">
                                        <h4
                                            className={`text-base font-semibold m-0 truncate ${isDark ? "text-gray-100" : "text-gray-800"}`}
                                        >
                                            {item.title}
                                        </h4>
                                        {item.hot && (
                                            <FireOutlined
                                                className="text-red-500 shrink-0"
                                            />
                                        )}
                                    </div>
                                    <p
                                        className={`text-sm m-0 line-clamp-2 ${isDark ? "text-gray-400" : "text-gray-500"}`}
                                    >
                                        {item.summary}
                                    </p>
                                </div>

                                {/* 箭头图标 */}
                                <RightOutlined
                                    className={`shrink-0 transition-all duration-200 ${
                                        hoveredId === item.id
                                            ? isDark
                                                ? "text-gray-300 translate-x-1"
                                                : "text-gray-600 translate-x-1"
                                            : isDark
                                                ? "text-gray-600"
                                                : "text-gray-300"
                                    }`}
                                />
                            </a>
                        </List.Item>
                    )}
                />
            </div>
        </Card>
    );
};

export default News_components;
