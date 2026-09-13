import { type FC, useEffect, useState } from "react";
import {
    Card,
    Spin,
    Tag,
    Progress,
    Empty,
    Button,
    Tooltip,
    theme,
} from "antd";
import {
    BookOutlined,
    CheckCircleFilled,
    ClockCircleFilled,
    PlayCircleFilled,
    RightOutlined,
    LeftOutlined,
    MenuFoldOutlined,
    MenuUnfoldOutlined,
} from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import useLogin from "../status/Login_status";
import useIsDark from "../status/IsDark_status";
import {
    getDocsProgress,
    getRecentLearning,
    type LearningRecord,
} from "../api/Learning_api";
import { getDoc, getDocList, type DocInfo, type DocDir } from "../api/Doc_api";

// ==================== 类型定义 ====================

interface ChapterNode {
    id: string;
    name: string;
    progress: number;
    status: "learning" | "completed" | "not_started";
    lastAccessTime?: string;
}

interface DocNode {
    id: string;
    name: string;
    summary: string;
    img: string;
    avgProgress: number;
    status: "learning" | "completed" | "not_started";
    chapters: ChapterNode[];
    lastAccessTime?: string;
}

interface TimelineMilestone {
    date: string;
    label: string;
    docs: DocNode[];
}

// ==================== 工具函数 ====================

const getDocStatus = (
    avgProgress: number,
): "learning" | "completed" | "not_started" => {
    if (avgProgress >= 100) return "completed";
    if (avgProgress > 0) return "learning";
    return "not_started";
};

const formatDate = (dateStr?: string): string => {
    if (!dateStr) return "";
    const date = new Date(dateStr);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));
    if (days === 0) return "今天";
    if (days === 1) return "昨天";
    if (days < 7) return `${days}天前`;
    return date.toLocaleDateString("zh-CN", {
        month: "short",
        day: "numeric",
    });
};

// 构建时间线里程碑
const buildTimelineMilestones = (
    docs: DocNode[],
): TimelineMilestone[] => {
    const milestoneMap = new Map<string, TimelineMilestone>();

    // 按学习时间分组
    for (const doc of docs) {
        if (doc.lastAccessTime) {
            const date = new Date(doc.lastAccessTime);
            const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;

            if (!milestoneMap.has(monthKey)) {
                milestoneMap.set(monthKey, {
                    date: monthKey,
                    label: `${date.getFullYear()}年${date.getMonth() + 1}月`,
                    docs: [],
                });
            }
            milestoneMap.get(monthKey)!.docs.push(doc);
        } else {
            // 未开始的文档归入"未开始"
            if (!milestoneMap.has("not_started")) {
                milestoneMap.set("not_started", {
                    date: "not_started",
                    label: "未开始",
                    docs: [],
                });
            }
            milestoneMap.get("not_started")!.docs.push(doc);
        }
    }

    // 按月份排序（倒序）
    return Array.from(milestoneMap.values())
        .sort((a, b) => {
            if (a.date === "not_started") return 1;
            if (b.date === "not_started") return -1;
            return b.date.localeCompare(a.date);
        });
};

// ==================== 主组件 ====================

const LearningPath_components: FC = () => {
    const { token, isLogin } = useLogin();
    const { isDark } = useIsDark();
    const nav = useNavigate();
    const { token: antdToken } = theme.useToken();
    const pColor = antdToken.colorPrimary;

    const [loading, setLoading] = useState(true);
    const [docNodes, setDocNodes] = useState<DocNode[]>([]);
    const [timelineMilestones, setTimelineMilestones] = useState<
        TimelineMilestone[]
    >([]);
    const [viewMode, setViewMode] = useState<"tree" | "timeline" | "combined">(
        "combined",
    );
    const [collapsed, setCollapsed] = useState(false);

    // 获取所有文档和进度数据
    useEffect(() => {
        let cancelled = false;

        if (!isLogin || !token) {
            const timer = setTimeout(() => {
                setLoading(false);
            }, 0);
            return () => {
                clearTimeout(timer);
                cancelled = true;
            };
        }

        const fetchData = async () => {
            try {
                // 1. 获取文档列表
                const docListRes = await getDocList();
                if (!docListRes.ok || cancelled) {
                    if (!cancelled) {
                        setTimeout(() => setLoading(false), 0);
                    }
                    return;
                }
                const docs = (docListRes as { ok: boolean; docList: DocInfo[] })
                    .docList;

                // 2. 获取所有文档的进度
                const progressRes = await getDocsProgress(token);
                const progressMap = progressRes.success
                    ? (progressRes.data as Record<string, number>)
                    : {};

                // 3. 获取最近学习记录（用于时间线）
                const recentRes = await getRecentLearning(token);
                const recentRecords = recentRes.success
                    ? (recentRes.data as LearningRecord[])
                    : [];

                // 4. 构建每个文档的详细数据
                const docNodesData: DocNode[] = [];
                for (const doc of docs) {
                    if (cancelled) return;
                    const docRes = await getDoc(doc.id);
                    if (!docRes.ok) continue;
                    const docInfo = (
                        docRes as {
                            ok: boolean;
                            docInfo: DocInfo;
                            docDir: DocDir[];
                        }
                    ).docInfo;
                    const docDir = (
                        docRes as {
                            ok: boolean;
                            docInfo: DocInfo;
                            docDir: DocDir[];
                        }
                    ).docDir;

                    const avgProgress = progressMap[doc.id] || 0;

                    // 获取该文档的最近学习时间
                    const docRecords = recentRecords.filter(
                        (r) => r.docId.toString() === doc.id,
                    );
                    const lastAccessTime =
                        docRecords.length > 0
                            ? docRecords.sort(
                                  (a, b) =>
                                      new Date(b.lastAccessTime).getTime() -
                                      new Date(a.lastAccessTime).getTime(),
                              )[0].lastAccessTime
                            : undefined;

                    // 构建章节节点
                    const chapters: ChapterNode[] = docDir.map((chapter) => {
                        const chapterRecord = docRecords.find(
                            (r) =>
                                r.chapterId?.toString() === chapter.id ||
                                r.chapterId === null,
                        );
                        return {
                            id: chapter.id,
                            name: chapter.name,
                            progress: chapterRecord?.progress || 0,
                            status: chapterRecord
                                ? chapterRecord.status === "completed"
                                    ? "completed"
                                    : "learning"
                                : "not_started",
                            lastAccessTime: chapterRecord?.lastAccessTime,
                        };
                    });

                    docNodesData.push({
                        id: doc.id,
                        name: docInfo.name,
                        summary: docInfo.summary,
                        img: docInfo.img,
                        avgProgress,
                        status: getDocStatus(avgProgress),
                        chapters,
                        lastAccessTime,
                    });
                }

                if (cancelled) return;
                setTimeout(() => {
                    if (!cancelled) {
                        setDocNodes(docNodesData);
                        // 5. 构建时间线数据
                        const milestones = buildTimelineMilestones(docNodesData);
                        setTimelineMilestones(milestones);
                        setLoading(false);
                    }
                }, 0);
            } catch (error) {
                console.error("获取学习路径数据失败:", error);
                if (!cancelled) {
                    setTimeout(() => setLoading(false), 0);
                }
            }
        };

        fetchData();

        return () => {
            cancelled = true;
        };
    }, [token, isLogin]);

    // ==================== 渲染函数 ====================

    // 渲染进度标签
    const renderStatusTag = (
        status: "learning" | "completed" | "not_started",
    ) => {
        switch (status) {
            case "completed":
                return (
                    <Tag
                        color="success"
                        icon={<CheckCircleFilled />}
                        className="text-xs"
                    >
                        已完成
                    </Tag>
                );
            case "learning":
                return (
                    <Tag
                        color="processing"
                        icon={<PlayCircleFilled />}
                        className="text-xs"
                    >
                        学习中
                    </Tag>
                );
            default:
                return (
                    <Tag
                        color="default"
                        icon={<ClockCircleFilled />}
                        className="text-xs"
                    >
                        未开始
                    </Tag>
                );
        }
    };

    // 渲染文档节点（树状视图）
    const renderDocNode = (doc: DocNode) => (
        <div
            key={doc.id}
            className={`rounded-xl border overflow-hidden transition-all duration-200 hover:shadow-md ${
                isDark
                    ? "bg-[#1a1a1a] border-gray-700"
                    : "bg-white border-gray-200"
            }`}
        >
            {/* 文档头部 */}
            <div
                className="p-4 flex items-center justify-between cursor-pointer hover:opacity-80 transition-opacity"
                onClick={() => nav(`/document/${doc.id}`)}
            >
                <div className="flex items-center gap-3 flex-1 min-w-0">
                    <div
                        className="w-10 h-10 rounded-lg flex items-center justify-center shrink-0 text-white text-lg"
                        style={{
                            backgroundColor:
                                doc.status === "completed"
                                    ? "#52c41a"
                                    : doc.status === "learning"
                                      ? pColor
                                      : "#d9d9d9",
                        }}
                    >
                        <BookOutlined />
                    </div>
                    <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-2">
                            <span
                                className={`font-medium truncate ${
                                    isDark ? "text-gray-100" : "text-gray-800"
                                }`}
                            >
                                {doc.name}
                            </span>
                            {renderStatusTag(doc.status)}
                        </div>
                        <div className="flex items-center gap-2 mt-1">
                            <Progress
                                percent={Math.round(doc.avgProgress)}
                                size="small"
                                status={
                                    doc.status === "completed"
                                        ? "success"
                                        : "active"
                                }
                                strokeColor={{
                                    "0%": "#108ee9",
                                    "100%": "#87d068",
                                }}
                                className="flex-1 max-w-40"
                                showInfo={false}
                            />
                            <span className="text-xs text-gray-400">
                                {Math.round(doc.avgProgress)}%
                            </span>
                        </div>
                    </div>
                </div>
                <div className="flex items-center gap-2 shrink-0">
                    {doc.lastAccessTime && (
                        <span className="text-xs text-gray-400 hidden sm:block">
                            {formatDate(doc.lastAccessTime)}
                        </span>
                    )}
                    <Button
                        type="text"
                        size="small"
                        icon={<RightOutlined />}
                        className="text-gray-400"
                    />
                </div>
            </div>

            {/* 章节列表（展开状态） */}
            {!collapsed && doc.chapters.length > 0 && (
                <div
                    className={`border-t ${
                        isDark ? "border-gray-700" : "border-gray-100"
                    }`}
                >
                    {doc.chapters.map((chapter, index) => (
                        <div
                            key={chapter.id}
                            className={`flex items-center gap-3 px-4 py-2.5 cursor-pointer transition-colors ${
                                isDark
                                    ? "hover:bg-gray-800"
                                    : "hover:bg-gray-50"
                            } ${
                                index < doc.chapters.length - 1
                                    ? `border-b ${
                                          isDark
                                              ? "border-gray-800"
                                              : "border-gray-50"
                                      }`
                                    : ""
                            }`}
                            onClick={() =>
                                nav(`/document/${doc.id}/${chapter.id}`)
                            }
                        >
                            {/* 状态图标 */}
                            <div className="relative flex items-center justify-center w-6 shrink-0">
                                {chapter.status === "completed" ? (
                                    <CheckCircleFilled className="text-green-500 text-sm" />
                                ) : chapter.status === "learning" ? (
                                    <PlayCircleFilled
                                        className="text-blue-500 text-sm"
                                        style={{ color: pColor }}
                                    />
                                ) : (
                                    <div
                                        className={`w-2 h-2 rounded-full ${
                                            isDark
                                                ? "bg-gray-600"
                                                : "bg-gray-300"
                                        }`}
                                    />
                                )}
                            </div>

                            {/* 章节名称 */}
                            <span
                                className={`flex-1 text-sm truncate ${
                                    chapter.status === "completed"
                                        ? isDark
                                            ? "text-gray-400"
                                            : "text-gray-500"
                                        : isDark
                                          ? "text-gray-200"
                                          : "text-gray-700"
                                } ${
                                    chapter.status === "completed"
                                        ? "line-through decoration-1 decoration-gray-400"
                                        : ""
                                }`}
                            >
                                {chapter.name}
                            </span>

                            {/* 进度条 */}
                            {chapter.progress > 0 && chapter.progress < 100 && (
                                <Progress
                                    percent={chapter.progress}
                                    size="small"
                                    className="w-16"
                                    showInfo={false}
                                    strokeColor={{
                                        "0%": "#108ee9",
                                        "100%": "#87d068",
                                    }}
                                />
                            )}

                            {/* 完成标签 */}
                            {chapter.status === "completed" && (
                                <Tag color="success" className="text-xs mr-0">
                                    完成
                                </Tag>
                            )}

                            {/* 最近访问 */}
                            {chapter.lastAccessTime && (
                                <span className="text-xs text-gray-400 hidden md:block shrink-0">
                                    {formatDate(chapter.lastAccessTime)}
                                </span>
                            )}

                            {/* 箭头 */}
                            <RightOutlined className="text-gray-300 text-xs shrink-0" />
                        </div>
                    ))}
                </div>
            )}
        </div>
    );

    // 渲染时间线视图
    const renderTimeline = () => (
        <div className="relative">
            {/* 时间线竖线 */}
            <div
                className={`absolute left-5 top-0 bottom-0 w-0.5 ${
                    isDark ? "bg-gray-700" : "bg-gray-200"
                }`}
            />

            {timelineMilestones.map((milestone, mi) => (
                <div key={mi} className="relative mb-8 last:mb-0">
                    {/* 时间线节点 */}
                    <div className="flex items-start gap-4">
                        {/* 时间标记点 */}
                        <div className="relative z-10 flex items-center justify-center w-10 shrink-0">
                            <div
                                className={`w-4 h-4 rounded-full border-2 ${
                                    milestone.date === "not_started"
                                        ? isDark
                                            ? "border-gray-500 bg-gray-800"
                                            : "border-gray-400 bg-white"
                                        : "border-transparent"
                                }`}
                                style={{
                                    backgroundColor:
                                        milestone.date === "not_started"
                                            ? undefined
                                            : pColor,
                                    borderColor:
                                        milestone.date === "not_started"
                                            ? undefined
                                            : pColor,
                                }}
                            />
                        </div>

                        {/* 内容区域 */}
                        <div className="flex-1 min-w-0">
                            {/* 时间标签 */}
                            <div className="flex items-center gap-2 mb-3">
                                <span
                                    className={`text-base font-bold ${
                                        isDark
                                            ? "text-gray-200"
                                            : "text-gray-700"
                                    }`}
                                >
                                    {milestone.label}
                                </span>
                                <span className="text-xs text-gray-400">
                                    {milestone.docs.length} 个文档
                                </span>
                            </div>

                            {/* 文档列表 */}
                            <div className="flex flex-col gap-3">
                                {milestone.docs.map((doc) =>
                                    renderDocNode(doc),
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            ))}

            {timelineMilestones.length === 0 && (
                <div className="py-12">
                    <Empty description="暂无学习记录，开始你的学习之旅吧！">
                        <Button
                            type="primary"
                            onClick={() => nav("/document")}
                        >
                            浏览文档
                        </Button>
                    </Empty>
                </div>
            )}
        </div>
    );

    // 渲染树状视图（仅文档列表）
    const renderTreeView = () => (
        <div className="flex flex-col gap-3">
            {docNodes.length > 0 ? (
                docNodes.map((doc) => renderDocNode(doc))
            ) : (
                <div className="py-12">
                    <Empty description="暂无文档数据" />
                </div>
            )}
        </div>
    );

    // 渲染统计概览
    const renderStats = () => {
        const totalDocs = docNodes.length;
        const completedDocs = docNodes.filter(
            (d) => d.status === "completed",
        ).length;
        const learningDocs = docNodes.filter(
            (d) => d.status === "learning",
        ).length;
        const totalChapters = docNodes.reduce(
            (sum, d) => sum + d.chapters.length,
            0,
        );
        const completedChapters = docNodes.reduce(
            (sum, d) =>
                sum +
                d.chapters.filter((c) => c.status === "completed").length,
            0,
        );

        return (
            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
                <div
                    className={`rounded-xl p-3 ${
                        isDark ? "bg-[#1a1a1a]" : "bg-white"
                    } border ${isDark ? "border-gray-700" : "border-gray-200"}`}
                >
                    <div className="text-xs text-gray-400 mb-1">总文档数</div>
                    <div
                        className={`text-2xl font-bold ${
                            isDark ? "text-gray-100" : "text-gray-800"
                        }`}
                    >
                        {totalDocs}
                    </div>
                </div>
                <div
                    className={`rounded-xl p-3 ${
                        isDark ? "bg-[#1a1a1a]" : "bg-white"
                    } border ${isDark ? "border-gray-700" : "border-gray-200"}`}
                >
                    <div className="text-xs text-gray-400 mb-1">已完成</div>
                    <div className="text-2xl font-bold text-green-500">
                        {completedDocs}
                    </div>
                </div>
                <div
                    className={`rounded-xl p-3 ${
                        isDark ? "bg-[#1a1a1a]" : "bg-white"
                    } border ${isDark ? "border-gray-700" : "border-gray-200"}`}
                >
                    <div className="text-xs text-gray-400 mb-1">学习中</div>
                    <div
                        className="text-2xl font-bold"
                        style={{ color: pColor }}
                    >
                        {learningDocs}
                    </div>
                </div>
                <div
                    className={`rounded-xl p-3 ${
                        isDark ? "bg-[#1a1a1a]" : "bg-white"
                    } border ${isDark ? "border-gray-700" : "border-gray-200"}`}
                >
                    <div className="text-xs text-gray-400 mb-1">
                        章节完成率
                    </div>
                    <div
                        className={`text-2xl font-bold ${
                            isDark ? "text-gray-100" : "text-gray-800"
                        }`}
                    >
                        {totalChapters > 0
                            ? Math.round(
                                  (completedChapters / totalChapters) * 100,
                              )
                            : 0}
                        %
                    </div>
                </div>
            </div>
        );
    };

    // ==================== 主渲染 ====================

    if (loading) {
        return (
            <Card title="学习路径图" className="w-full">
                <div className="flex justify-center items-center h-64">
                    <Spin size="large" />
                </div>
            </Card>
        );
    }

    return (
        <Card
            title={
                <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <span
                            className={`text-lg font-bold ${
                                isDark ? "text-gray-100" : "text-gray-800"
                            }`}
                        >
                            学习路径图
                        </span>
                        <span className="text-sm text-gray-400 font-normal">
                            追踪你的学习旅程
                        </span>
                    </div>
                    <div className="flex items-center gap-2">
                        {/* 视图切换按钮 */}
                        <Tooltip title="树状视图">
                            <Button
                                type={
                                    viewMode === "tree" ? "primary" : "text"
                                }
                                size="small"
                                icon={<MenuFoldOutlined />}
                                onClick={() => setViewMode("tree")}
                            />
                        </Tooltip>
                        <Tooltip title="组合视图">
                            <Button
                                type={
                                    viewMode === "combined" ? "primary" : "text"
                                }
                                size="small"
                                icon={<MenuUnfoldOutlined />}
                                onClick={() => setViewMode("combined")}
                            />
                        </Tooltip>
                        <Tooltip title="时间线视图">
                            <Button
                                type={
                                    viewMode === "timeline" ? "primary" : "text"
                                }
                                size="small"
                                icon={<ClockCircleFilled />}
                                onClick={() => setViewMode("timeline")}
                            />
                        </Tooltip>
                        <div className="w-px h-4 bg-gray-300 mx-1" />
                        <Tooltip
                            title={collapsed ? "展开章节" : "收起章节"}
                        >
                            <Button
                                type="text"
                                size="small"
                                icon={
                                    collapsed ? (
                                        <RightOutlined />
                                    ) : (
                                        <LeftOutlined />
                                    )
                                }
                                onClick={() => setCollapsed(!collapsed)}
                            />
                        </Tooltip>
                    </div>
                </div>
            }
            className="w-full"
        >
            {/* 统计概览 */}
            {renderStats()}

            {/* 视图内容 */}
            {viewMode === "timeline" || viewMode === "combined"
                ? renderTimeline()
                : renderTreeView()}
        </Card>
    );
};

export default LearningPath_components;
