import {
    useState,
    useEffect,
    useCallback,
    useRef,
    type FC,
} from "react";
import {
    Button,
    Card,
    Radio,
    Space,
    message,
    Progress,
    List,
    Tag,
    Typography,
    Spin,
    Empty,
    Collapse,
} from "antd";
import {
    CheckCircleOutlined,
    CloseCircleOutlined,
    ThunderboltOutlined,
    ReloadOutlined,
} from "@ant-design/icons";
import { generateQuiz, gradeQuiz, type QuizQuestion } from "../api/Quiz_api";
import useLogin from "../status/Login_status";
import useIsDark from "../status/IsDark_status";

const { Text, Title, Paragraph } = Typography;

interface QuizProps {
    docId: number;
    chapterId: number | null;
    /** 文档内容是否为空的标记，为空则不显示 */
    hasContent: boolean;
}

const Quiz_components: FC<QuizProps> = ({ docId, chapterId, hasContent }) => {
    const [loading, setLoading] = useState(false);
    const [grading, setGrading] = useState(false);
    const [questions, setQuestions] = useState<QuizQuestion[]>([]);
    const [answers, setAnswers] = useState<Record<number, string>>({});
    const [result, setResult] = useState<{
        score: number;
        total: number;
        summary: string;
        details: Array<{
            correct: boolean;
            userAnswer: string;
            correctAnswer: string;
            feedback: string;
        }>;
    } | null>(null);
    const [messageApi, contextHolder] = message.useMessage();
    const { token } = useLogin();
    const { isDark } = useIsDark();
    
    // 使用 ref 追踪已初始化的 key
    const initRef = useRef<string>("");

    // 重置状态
    const resetQuiz = useCallback(() => {
        setQuestions([]);
        setAnswers({});
        setResult(null);
    }, []);

    // 生成题目
    const handleGenerate = useCallback(async () => {
        if (!token) {
            messageApi.warning("请先登录");
            return;
        }
        setLoading(true);
        setResult(null);
        setAnswers({});

        const res = await generateQuiz(docId, chapterId, 5, token);

        if (res.ok && res.questions) {
            setQuestions(res.questions);
            messageApi.success(`已生成 ${res.count} 道题目`);
        } else {
            messageApi.error(res.error || "生成失败，请检查文档内容是否为空");
        }
        setLoading(false);
    }, [docId, chapterId, token, messageApi]);

    // 监听参数变化，重置初始化状态
    useEffect(() => {
        initRef.current = "";
    }, [docId, chapterId]);

    // 组件挂载时自动生成题目
    useEffect(() => {
        const currentKey = `${docId}-${chapterId}`;
        if (initRef.current !== currentKey && hasContent) {
            initRef.current = currentKey;
            // 使用 setTimeout 延迟调用，避免在 effect 中直接 setState
            const timer = setTimeout(() => {
                handleGenerate();
            }, 0);
            return () => clearTimeout(timer);
        }
    }, [docId, chapterId, hasContent, handleGenerate]);

    // 选择答案
    const handleAnswerChange = (questionIndex: number, value: string) => {
        setAnswers((prev) => ({ ...prev, [questionIndex]: value }));
    };

    // 提交批改
    const handleSubmit = async () => {
        if (!token) return;

        // 检查是否所有题目都作答了
        const unansweredIndexes: number[] = [];
        questions.forEach((_, i) => {
            if (!answers[i]) {
                unansweredIndexes.push(i + 1);
            }
        });
        
        if (unansweredIndexes.length > 0) {
            const count = unansweredIndexes.length;
            if (count === 1) {
                messageApi.warning(`第 ${unansweredIndexes[0]} 题还未作答`);
            } else {
                messageApi.warning(`还有 ${count} 道题未作答（第 ${unansweredIndexes.join("、")} 题）`);
            }
            return;
        }

        setGrading(true);
        const answerList = questions.map((_, i) => answers[i] || "");
        const res = await gradeQuiz(
            docId,
            chapterId,
            questions,
            answerList,
            token,
        );

        if (res.ok && res.details) {
            setResult({
                score: res.score,
                total: res.total,
                summary: res.summary,
                details: res.details,
            });
            messageApi.success("批改完成！");
        } else {
            messageApi.error(res.error || "批改失败");
        }
        setGrading(false);
    };

    // 重新测验
    const handleRetry = useCallback(() => {
        resetQuiz();
        handleGenerate();
    }, [resetQuiz, handleGenerate]);

    if (!hasContent) {
        return (
            <Empty description="请在文档页面使用测验功能" className="py-8" />
        );
    }

    return (
        <>
            {contextHolder}
            <div className="quiz-content">
                {/* 标题 */}
                <div className="flex justify-between items-center mb-4 pb-3 border-b border-gray-200">
                    <Space>
                        <ThunderboltOutlined
                            style={{ color: "#faad14", fontSize: 20 }}
                        />
                        <Title level={4} className="mb-0!">
                            AI 智能测验
                        </Title>
                        {result && (
                            <Tag
                                color={
                                    result.score / result.total >= 0.6
                                        ? "success"
                                        : "error"
                                }
                            >
                                {result.score}/{result.total}
                            </Tag>
                        )}
                    </Space>
                    <Space>
                        {questions.length > 0 && !result && (
                            <>
                                <Button
                                    icon={<ReloadOutlined />}
                                    onClick={handleRetry}
                                    disabled={loading || grading}
                                >
                                    重新生成
                                </Button>
                                <Button
                                    type="primary"
                                    loading={grading}
                                    onClick={handleSubmit}
                                    icon={<CheckCircleOutlined />}
                                >
                                    提交批改
                                </Button>
                            </>
                        )}
                        {result && (
                            <>
                                <Button
                                    type="primary"
                                    icon={<ReloadOutlined />}
                                    onClick={handleRetry}
                                >
                                    再来一次
                                </Button>
                            </>
                        )}
                    </Space>
                </div>

                {/* 加载状态 */}
                {loading && (
                    <div className="flex flex-col items-center justify-center py-12">
                        <Spin size="large" />
                        <Text className="mt-4 text-gray-500">
                            AI正在根据文档内容生成题目...
                        </Text>
                    </div>
                )}

                {/* 无内容提示 */}
                {!loading && questions.length === 0 && !result && (
                    <Empty
                        description="点击生成按钮，AI将根据文档内容自动出题"
                        className="py-8"
                    >
                        <Button
                            type="primary"
                            onClick={handleGenerate}
                            loading={loading}
                            icon={<ThunderboltOutlined />}
                        >
                            生成测验
                        </Button>
                    </Empty>
                )}

                {/* 题目列表 */}
                {!loading && questions.length > 0 && !result && (
                    <div className="space-y-6 max-h-[60vh] overflow-y-auto pr-2">
                        {questions.map((q, index) => (
                            <Card
                                key={index}
                                size="small"
                                title={
                                    <Space>
                                        <Text strong>第 {index + 1} 题</Text>
                                        <Tag
                                            color="blue"
                                            style={{ fontSize: 11 }}
                                        >
                                            选择题
                                        </Tag>
                                    </Space>
                                }
                                className="shadow-sm"
                            >
                                <Paragraph className="text-base font-medium mb-3">
                                    {q.question}
                                </Paragraph>
                                <Radio.Group
                                    value={answers[index]}
                                    onChange={(e) =>
                                        handleAnswerChange(
                                            index,
                                            e.target.value,
                                        )
                                    }
                                    className="w-full"
                                >
                                    <Space
                                        direction="vertical"
                                        className="w-full"
                                    >
                                        {Object.entries(q.options).map(
                                            ([key, value]) => (
                                                <Radio
                                                    key={key}
                                                    value={key}
                                                    className={`p-2 rounded w-full ${isDark ? "hover:bg-gray-800" : "hover:bg-gray-50"}`}
                                                >
                                                    <Text>
                                                        <Text strong>
                                                            {key}.
                                                        </Text>{" "}
                                                        {value}
                                                    </Text>
                                                </Radio>
                                            ),
                                        )}
                                    </Space>
                                </Radio.Group>
                            </Card>
                        ))}
                    </div>
                )}

                {/* 批改结果 */}
                {result && (
                    <div className="space-y-4">
                        {/* 得分概览 */}
                        <Card className="text-center bg-linear-to-r from-blue-50 to-green-50">
                            <Progress
                                type="circle"
                                percent={Math.round(
                                    (result.score / result.total) * 100,
                                )}
                                format={() => `${result.score}/${result.total}`}
                                size={100}
                                status={
                                    result.score / result.total >= 0.6
                                        ? "success"
                                        : "exception"
                                }
                            />
                            <div className="mt-4">
                                <Title level={5}>
                                    {result.score / result.total >= 0.8
                                        ? "🌟 太棒了！"
                                        : result.score / result.total >= 0.6
                                          ? "👍 不错！继续加油"
                                          : "💪 需要多加复习哦"}
                                </Title>
                                <Paragraph type="secondary" className="mb-0">
                                    {result.summary}
                                </Paragraph>
                            </div>
                        </Card>

                        {/* 逐题解析 */}
                        <Collapse
                            items={[
                                {
                                    key: "details",
                                    label: "查看逐题解析",
                                    children: (
                                        <List
                                            dataSource={result.details}
                                            renderItem={(detail, index) => (
                                                <List.Item>
                                                    <List.Item.Meta
                                                        avatar={
                                                            detail.correct ? (
                                                                <CheckCircleOutlined className="text-green-500 text-lg" />
                                                            ) : (
                                                                <CloseCircleOutlined className="text-red-500 text-lg" />
                                                            )
                                                        }
                                                        title={
                                                            <Space>
                                                                <Text
                                                                    strong
                                                                    className={
                                                                        detail.correct
                                                                            ? "text-green-600"
                                                                            : "text-red-600"
                                                                    }
                                                                >
                                                                    第{" "}
                                                                    {index + 1}{" "}
                                                                    题
                                                                </Text>
                                                                <Tag
                                                                    color={
                                                                        detail.correct
                                                                            ? "success"
                                                                            : "error"
                                                                    }
                                                                >
                                                                    {detail.correct
                                                                        ? "正确"
                                                                        : "错误"}
                                                                </Tag>
                                                            </Space>
                                                        }
                                                        description={
                                                            <div className="space-y-1">
                                                                <Text type="secondary">
                                                                    你的答案：
                                                                    {detail.userAnswer ||
                                                                        "未作答"}
                                                                </Text>
                                                                <br />
                                                                {!detail.correct && (
                                                                    <>
                                                                        <Text type="secondary">
                                                                            正确答案：
                                                                            <Text
                                                                                strong
                                                                                className="text-green-600"
                                                                            >
                                                                                {
                                                                                    detail.correctAnswer
                                                                                }
                                                                            </Text>
                                                                        </Text>
                                                                        <br />
                                                                    </>
                                                                )}
                                                                <Text type="secondary">
                                                                    {
                                                                        detail.feedback
                                                                    }
                                                                </Text>
                                                            </div>
                                                        }
                                                    />
                                                </List.Item>
                                            )}
                                        />
                                    ),
                                },
                            ]}
                        />
                    </div>
                )}
            </div>
        </>
    );
};

export default Quiz_components;