import { type FC, useEffect, useState } from "react";
import { Card, Tag, Empty, Spin, Progress, Button } from "antd";
import { useNavigate } from "react-router-dom";
import { getRecentLearning, type LearningRecord } from "../api/Learning_api";
import { getDoc, type DocInfo, type DocDir } from "../api/Doc_api";
import useLogin from "../status/Login_status";
import useIsDark from "../status/IsDark_status";

interface ExtendedLearningRecord extends LearningRecord {
    docName?: string;
    chapterName?: string;
    docInfo?: DocInfo;
    docDir?: DocDir[];
}

const RecentLearning: FC = () => {
    const { token } = useLogin();
    const { isDark } = useIsDark();
    const nav = useNavigate();
    const [records, setRecords] = useState<ExtendedLearningRecord[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // 去重函数：按 docId + chapterId 分组，只保留最新的记录
        const deduplicateRecords = (
            records: ExtendedLearningRecord[],
        ): ExtendedLearningRecord[] => {
            const uniqueMap = new Map<string, ExtendedLearningRecord>();

            records.forEach((record) => {
                const key = `${record.docId}-${record.chapterId}`;
                const existing = uniqueMap.get(key);

                if (
                    !existing ||
                    new Date(record.lastAccessTime) >
                        new Date(existing.lastAccessTime)
                ) {
                    uniqueMap.set(key, record);
                }
            });

            return Array.from(uniqueMap.values());
        };

        const fetchRecords = async () => {
            if (token) {
                setLoading(true);
                const res = await getRecentLearning(token);
                if (res.success && res.data) {
                    const extendedRecords: ExtendedLearningRecord[] = [];

                    for (const record of res.data) {
                        const extendedRecord: ExtendedLearningRecord = {
                            ...record,
                        };

                        const docRes = await getDoc(record.docId.toString());
                        if (docRes.ok) {
                            extendedRecord.docName = docRes.docInfo.name;
                            extendedRecord.docInfo = docRes.docInfo;
                            extendedRecord.docDir = docRes.docDir;

                            if (record.chapterId) {
                                const chapter = docRes.docDir.find(
                                    (c) =>
                                        c.id === record.chapterId?.toString(),
                                );
                                if (chapter) {
                                    extendedRecord.chapterName = chapter.name;
                                }
                            }
                        }

                        extendedRecords.push(extendedRecord);
                    }

                    // 去重：按 docId + chapterId 分组，只保留最新的记录
                    const uniqueRecords = deduplicateRecords(extendedRecords);
                    setRecords(uniqueRecords);
                }
                setLoading(false);
            }
        };
        fetchRecords();
    }, [token]);

    const formatDate = (dateStr: string) => {
        const date = new Date(dateStr);
        const now = new Date();
        const diff = now.getTime() - date.getTime();
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));

        if (days === 0) return "今天";
        if (days === 1) return "昨天";
        if (days < 7) return `${days}天前`;
        return date.toLocaleDateString();
    };

    const handleClick = (record: ExtendedLearningRecord) => {
        if (record.chapterId) {
            nav(`/document/${record.docId}/${record.chapterId}`);
        } else {
            const firstChapter = record.docDir?.[0];
            if (firstChapter) {
                nav(`/document/${record.docId}/${firstChapter.id}`);
            } else {
                nav(`/document/${record.docId}`);
            }
        }
    };

    if (loading) {
        return (
            <Card title="章节学习记录" className={`recent-learning-card ${isDark ? "dark" : ""}`}>
                <div className="flex justify-center items-center py-8">
                    <Spin />
                </div>
            </Card>
        );
    }

    if (records.length === 0) {
        return (
            <Card title="章节学习记录" className={`recent-learning-card ${isDark ? "dark" : ""}`}>
                <Empty description="暂无学习记录" />
            </Card>
        );
    }

    const groupedByDoc = records.reduce(
        (acc, record) => {
            const docId = record.docId;
            if (!acc[docId]) {
                acc[docId] = {
                    docName: record.docName || `文档 ${docId}`,
                    chapters: [],
                };
            }
            acc[docId].chapters.push(record);
            return acc;
        },
        {} as Record<
            number,
            { docName: string; chapters: ExtendedLearningRecord[] }
        >,
    );

    return (
        <Card title="章节学习记录" className="recent-learning-card">
            <div className="flex flex-col gap-4">
                {Object.entries(groupedByDoc).map(([docId, docGroup]) => (
                    <div
                        key={docId}
                        className={`border rounded-lg p-4 ${isDark ? "border-gray-700 bg-[#1a1a1a]" : "border-gray-200"}`}
                    >
                        <div className="flex items-center justify-between mb-3">
                            <h3 className={`font-medium text-lg ${isDark ? "text-gray-100" : "text-gray-800"}`}>
                                {docGroup.docName}
                            </h3>
                            <Tag color="blue">
                                共 {docGroup.chapters.length} 个章节
                            </Tag>
                        </div>
                        <div className="flex flex-col gap-2">
                            {docGroup.chapters.map((record) => (
                                <div
                                    key={`${record.docId}-${record.chapterId}`}
                                    className={`flex flex-col sm:flex-row sm:items-center justify-between p-3 rounded-lg transition-colors gap-2 sm:gap-3 ${isDark ? "bg-[#252525] hover:bg-[#303030]" : "bg-gray-50 hover:bg-gray-100"}`}
                                >
                                    <div className="flex items-center gap-3 flex-1 min-w-0">
                                        <div className="flex flex-col gap-1 min-w-0 flex-1">
                                            <span className={`font-medium text-sm truncate ${isDark ? "text-gray-200" : "text-gray-800"}`}>
                                                {record.chapterName ||
                                                    `章节 ${record.chapterId}`}
                                            </span>
                                            <div className="flex items-center gap-2 flex-wrap">
                                                <Progress
                                                    percent={
                                                        record.progress || 0
                                                    }
                                                    size="small"
                                                    status={
                                                        record.status ===
                                                        "completed"
                                                            ? "success"
                                                            : "active"
                                                    }
                                                    strokeColor={{
                                                        "0%": "#108ee9",
                                                        "100%": "#87d068",
                                                    }}
                                                    className="flex-1"
                                                    style={{ width: "100px" }}
                                                />
                                                <Tag
                                                    color={
                                                        record.status ===
                                                        "completed"
                                                            ? "success"
                                                            : "processing"
                                                    }
                                                    className="text-xs"
                                                >
                                                    {record.status ===
                                                    "completed"
                                                        ? "已完成"
                                                        : "学习中"}
                                                </Tag>
                                            </div>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-3 justify-end">
                                        <span className={`text-xs whitespace-nowrap ${isDark ? "text-gray-500" : "text-gray-400"}`}>
                                            {formatDate(record.lastAccessTime)}
                                        </span>
                                        <Button
                                            type="link"
                                            size="small"
                                            onClick={() => handleClick(record)}
                                            className="text-blue-500"
                                        >
                                            {record.progress === 100
                                                ? "复习"
                                                : "继续"}
                                        </Button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                ))}
            </div>
        </Card>
    );

};

export default RecentLearning;
