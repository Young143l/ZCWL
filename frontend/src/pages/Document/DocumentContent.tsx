import { useEffect, useState, useRef, useCallback, type FC } from "react";
import { Link, useParams } from "react-router-dom";
import Template_Page from "../Template_Page";
import ReactMarkdown from "react-markdown";
import {
    Button,
    Divider,
    Empty,
    Menu,
    message,
    Spin,
    Tag,
    theme,
    type MenuProps,
} from "antd";
import {
    getDocContent,
    getDoc,
    type DocInfo,
    type DocContent,
    type DocDir,
} from "../../api/Doc_api";
import DocBreadcrumb_components from "../../components/DocBreadcrumb_components";
type MenuItem = Required<MenuProps>["items"][number];

import remarkGfm from "remark-gfm";
import { LoadingOutlined, SendOutlined } from "@ant-design/icons";
import DocComment_compents from "../../components/DocComment_compents";
import { type Comment } from "../../components/DocComment_compents";
import {
    getComments,
    postComment,
    type CommentRequest,
} from "../../api/DocComment.api";
import useLogin from "../../status/Login_status";
import useIsDark from "../../status/IsDark_status";
import {
    startLearning,
    updateProgress,
    endLearning,
    getLearningRecord,
    type LearningRecord,
} from "../../api/Learning_api";
import LearningProgress_components from "../../components/LearningProgress_components";

const DocumentContent: FC = () => {
    const [messageApi, contextHolder] = message.useMessage();
    const { d_id, c_id } = useParams();
    const [items, setItems] = useState<MenuItem[]>([]);
    const [docContent, setDocContent] = useState<DocContent | undefined>(
        undefined,
    );
    const [docInfo, setdocInfo] = useState<DocInfo | undefined>(undefined);
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const [loading, setLoading] = useState<boolean>(true);
    const [at, setAt] = useState<{ name: string; fa: string }>({
        name: "",
        fa: "-1",
    });
    const [comments, setComments] = useState<Comment[]>([]);
    const [commentContent, setCommentContent] = useState<string>("");
    const { isLogin, userId, token, email } = useLogin();
    const { isDark } = useIsDark();

    const contentRef = useRef<HTMLDivElement>(null);
    const [learningRecord, setLearningRecord] = useState<LearningRecord | null>(
        null,
    );
    const sessionRef = useRef<{
        docId: number;
        chapterId: number | null;
        startTime: number;
    } | null>(null);
    const progressTimerRef = useRef<ReturnType<typeof setInterval> | null>(
        null,
    );
    const currentProgressRef = useRef<number>(0);

    const fetchComments = async () => {
        if (d_id && c_id) {
            const res = await getComments(d_id, c_id);
            if (res.ok) {
                setComments(res.comments);
            }
        }
    };

    const handleSubmitComment = async () => {
        if (!isLogin) {
            return;
        }
        if (!commentContent.trim() || !d_id || !c_id) {
            return;
        }
        messageApi.loading({
            content: "提交中 ",
            duration: 0,
        });
        const commentRequest: CommentRequest = {
            uId: userId,
            email: email,
            content: (at.fa != "-1" ? `**@${at.name}** ` : "") + commentContent,
            fa: at.fa,
        };

        const res = await postComment(d_id, c_id, commentRequest, token);
        messageApi.destroy();
        if (res.ok) {
            setCommentContent("");
            setAt({ name: "", fa: "-1" });
            fetchComments();
            messageApi.success({
                content: "提交成功",
            });
        } else {
            messageApi.error({
                content: "提交失败",
            });
        }
    };

    const handleScroll = useCallback(() => {
        if (contentRef.current && isLogin && token && sessionRef.current) {
            const { scrollTop, scrollHeight, clientHeight } =
                contentRef.current;
            const maxScroll = scrollHeight - clientHeight;
            // 修复进度计算，当滚动到底部时设置为100%
            let progress;
            if (maxScroll > 0) {
                progress = Math.floor((scrollTop / maxScroll) * 100);
                // 当滚动到接近底部时，设置为100%
                if (scrollTop >= maxScroll - 10) {
                    progress = 100;
                }
            } else {
                // 内容不足一屏时，设置为100%
                progress = 100;
            }
            const position = scrollTop;

            if (progress >= currentProgressRef.current) {
                currentProgressRef.current = progress;
                updateProgress(
                    sessionRef.current.docId,
                    sessionRef.current.chapterId,
                    progress,
                    position,
                    token,
                ).then((res) => {
                    if (res.success && res.data) {
                        setLearningRecord(res.data);
                    }
                });
            }
        }
    }, [isLogin, token]);

    useEffect(() => {
        getDoc(d_id as string).then((res) => {
            if (res.ok) {
                setdocInfo(
                    (
                        res as {
                            ok: boolean;
                            docInfo: DocInfo;
                            docDir: DocDir[];
                        }
                    ).docInfo,
                );
                setItems(
                    (
                        res as {
                            ok: boolean;
                            docInfo: DocInfo;
                            docDir: DocDir[];
                        }
                    ).docDir.map((i) => {
                        return {
                            key: i.id,
                            label: (
                                <Link to={`/document/${d_id}/${i.id}`}>
                                    {i.name}
                                </Link>
                            ),
                        } as MenuItem;
                    }),
                );
                setTimeout(() => {
                    setLoading(false);
                }, 500);
            }
        });
        getDocContent(d_id as string, c_id as string).then((res) => {
            if (res.ok) {
                setDocContent(
                    (
                        res as {
                            ok: boolean;
                            docContent: DocContent;
                        }
                    ).docContent,
                );
                // 章节切换时滚动到顶部
                if (contentRef.current) {
                    contentRef.current.scrollTop = 0;
                }
            }
        });
    }, [d_id, c_id]);

    useEffect(() => {
        const loadComments = async () => {
            if (d_id && c_id) {
                const res = await getComments(d_id, c_id);
                if (res.ok) {
                    setComments(res.comments);
                }
            }
        };
        loadComments();
    }, [d_id, c_id]);

    // 处理章节切换
    useEffect(() => {
        if (!d_id || !isLogin || !token) {
            return;
        }

        const docId = parseInt(d_id, 10);
        const chapterId = c_id ? parseInt(c_id, 10) : null;

        // 结束上一个章节的学习
        if (sessionRef.current && token) {
            endLearning(
                sessionRef.current.docId,
                sessionRef.current.chapterId,
                token,
            );
        }

        // 重置状态 - 使用 setTimeout 避免同步调用 setState
        const resetTimer = setTimeout(() => {
            setLearningRecord(null);
        }, 0);
        currentProgressRef.current = 0;
        sessionRef.current = null;

        // 获取新章节的学习记录
        getLearningRecord(docId, chapterId, token).then((res) => {
            if (res.success && res.data) {
                setLearningRecord(res.data);
                currentProgressRef.current = res.data.progress || 0;
            }

            // 开始新章节的学习
            startLearning(docId, chapterId, token).then((res) => {
                if (res.success && res.data) {
                    setLearningRecord(res.data);
                    currentProgressRef.current = res.data.progress || 0;
                    sessionRef.current = {
                        docId,
                        chapterId,
                        startTime: Date.now(),
                    };
                }
            });
        });

        // 组件卸载时结束学习并清理定时器
        return () => {
            clearTimeout(resetTimer);
            if (sessionRef.current && token) {
                endLearning(
                    sessionRef.current.docId,
                    sessionRef.current.chapterId,
                    token,
                );
                sessionRef.current = null;
            }
        };
    }, [d_id, c_id, isLogin, token]);

    useEffect(() => {
        if (isLogin && token && contentRef.current) {
            progressTimerRef.current = setInterval(() => {
                handleScroll();
            }, 5000);
        }

        return () => {
            if (progressTimerRef.current) {
                clearInterval(progressTimerRef.current);
            }
        };
    }, [isLogin, token, handleScroll]);

    return (
        <>
            {contextHolder}
            <Spin
                indicator={<LoadingOutlined spin />}
                spinning={loading}
                size="large"
                fullscreen
            />
            <DocBreadcrumb_components
                d_id={d_id as string}
                d_name={docInfo?.name as string}
                c_id={c_id}
                c_name={docContent?.title as string}
            />
            <div className="flex flex-col gap-4">
                {isLogin && (
                    <div className="p-1">
                        <LearningProgress_components
                            progress={learningRecord?.progress || 0}
                            status={
                                (learningRecord?.status as
                                    | "learning"
                                    | "completed") || "learning"
                            }
                        />
                    </div>
                )}
                <Template_Page
                    children={
                        <div
                            ref={contentRef}
                            onScroll={handleScroll}
                            className={`${isDark ? "markdown-body-dark" : "markdown-body"} p-3 overflow-auto max-h-[calc(100vh-95px)]`}
                        >
                            <ReactMarkdown remarkPlugins={[remarkGfm]}>
                                {docContent?.content}
                            </ReactMarkdown>
                        </div>
                    }
                    sider={
                        <div
                            className="border-2 rounded-xl  overflow-hidden  "
                            style={{
                                borderColor: pColor,
                            }}
                        >
                            <Menu
                                defaultSelectedKeys={[c_id as string]}
                                mode="inline"
                                items={items}
                            />
                        </div>
                    }
                />
                <Template_Page>
                    <div className="p-2 flex flex-col gap-4 ">
                        <h1
                            className="text-xl border-l-4 pl-1 "
                            style={{
                                borderColor: pColor,
                            }}
                        >
                            讨论区
                        </h1>
                        <div
                            className="h-full border-2 rounded-2xl p-1.5 flex flex-col gap-2 overflow-auto "
                            style={{
                                borderColor: pColor,
                            }}
                        >
                            <textarea
                                className="w-full focus:outline-none border-none resize-none grow min-h-12"
                                placeholder="此处输入您的评论。"
                                value={commentContent}
                                onChange={(e) => {
                                    setCommentContent(e.target.value);
                                }}
                            ></textarea>
                            <div className="flex justify-between items-center">
                                <div className="h-full flex items-center justify-center">
                                    {at.fa != "-1" ? (
                                        <Tag
                                            closeIcon
                                            onClose={() => {
                                                setAt({ name: "", fa: "-1" });
                                            }}
                                            variant="filled"
                                        >
                                            {"@" + at.name}
                                        </Tag>
                                    ) : (
                                        <></>
                                    )}
                                </div>
                                <Button
                                    onClick={handleSubmitComment}
                                    disabled={commentContent === ""}
                                >
                                    <SendOutlined />
                                    发送
                                </Button>
                            </div>
                        </div>
                        <Divider size="small" />

                        {comments && comments.length ? (
                            comments.map((i) => (
                                <DocComment_compents
                                    comment={i}
                                    setAt={setAt}
                                    key={i.id}
                                />
                            ))
                        ) : (
                            <div className="h-full w-full flex justify-center items-center">
                                <Empty
                                    description={<div>目前没有讨论哦！</div>}
                                />
                            </div>
                        )}
                    </div>
                </Template_Page>
            </div>
        </>
    );
};
export default DocumentContent;
