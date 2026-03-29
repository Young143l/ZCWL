import { useEffect, useState, type FC } from "react";
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

    // 获取评论列表
    const fetchComments = async () => {
        if (d_id && c_id) {
            const res = await getComments(d_id, c_id);
            if (res.ok) {
                setComments(res.comments);
            }
        }
    };

    // 提交评论
    const handleSubmitComment = async () => {
        if (!isLogin) {
            // 未登录提示
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
            email: email, // 从登录状态获取或后端处理
            content: (at.fa != "-1" ? `**@${at.name}** ` : "") + commentContent,
            fa: at.fa,
        };

        const res = await postComment(d_id, c_id, commentRequest, token);
        messageApi.destroy();
        if (res.ok) {
            setCommentContent("");
            setAt({ name: "", fa: "-1" });
            fetchComments(); // 刷新评论列表
            messageApi.success({
                content: "提交成功",
            });
        } else {
            messageApi.error({
                content: "提交失败",
            });
        }
    };

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
                <Template_Page
                    children={
                        <div
                            className={`${isDark ? "markdown-body-dark" : "markdown-body"} p-3`}
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
                                            color="#108ee9"
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
