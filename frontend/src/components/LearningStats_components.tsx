import { type FC, useEffect, useState } from "react";
import { Card, Statistic, Row, Col } from "antd";
import { BookOutlined, CheckCircleOutlined, FireOutlined, FileTextOutlined } from "@ant-design/icons";
import { getLearningStats } from "../api/Learning_api";
import useLogin from "../status/Login_status";

const LearningStats: FC = () => {
    const { token } = useLogin();
    const [stats, setStats] = useState({
        totalDocs: 0,
        completedChapters: 0,
        streakDays: 0,
        totalStudyDays: 0,
    });

    useEffect(() => {
        const fetchStats = async () => {
            if (token) {
                const res = await getLearningStats(token);
                if (res.success && res.data) {
                    setStats({
                        totalDocs: res.data.totalDocs || 0,
                        completedChapters: res.data.completedChapters || 0,
                        streakDays: res.data.streakDays || 0,
                        totalStudyDays: res.data.totalStudyDays || 0,
                    });
                }
            }
        };
        fetchStats();
    }, [token]);

    return (
        <Card title="学习统计" className="learning-stats-card">
            <Row gutter={16}>
                <Col span={12}>
                    <Statistic
                        title="学习文档数"
                        value={stats.totalDocs}
                        prefix={<BookOutlined />}
                    />
                </Col>
                <Col span={12}>
                    <Statistic
                        title="完成章节数"
                        value={stats.completedChapters}
                        prefix={<CheckCircleOutlined />}
                    />
                </Col>
                <Col span={12}>
                    <Statistic
                        title="累计学习天数"
                        value={stats.totalStudyDays}
                        suffix="天"
                        prefix={<FileTextOutlined />}
                    />
                </Col>
                <Col span={12}>
                    <Statistic
                        title="连续学习"
                        value={stats.streakDays}
                        suffix="天"
                        prefix={<FireOutlined />}
                        styles={{ content: { color: '#cf1322' } }}
                    />
                </Col>
            </Row>
        </Card>
    );
};

export default LearningStats;
