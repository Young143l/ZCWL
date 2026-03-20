import { useEffect, useState, type FC } from "react";
import { Link, useParams } from "react-router-dom";
import Template_Page from "../Template_Page";
import ReactMarkdown from "react-markdown";
import {
    Button,
    Divider,
    Empty,
    Menu,
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
import "github-markdown-css/github-markdown.css";
import remarkGfm from "remark-gfm";
import { LoadingOutlined, SendOutlined } from "@ant-design/icons";
import DocComment_compents from "../../components/DocComment_compents";
import { type Comment } from "../../components/DocComment_compents";
const DocumentContent: FC = () => {
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
    const [comments, setComments] = useState<Comment[]>([
        // {
        //                         id: "comment-123",
        //                         uId: "user-456",
        //                         email: "2563043887@qq.com",
        //                         content:
        //                             "这是一",
        //                         children: [
        //                             {
        //                                 id: "child-comment-789",
        //                                 uId: "user-789",
        //                                 email: "child@example.com",
        //                                 content: "这是子评论内容",
        //                             },
        //                         ],
        //                     }
    ]);

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

    return (
        <>
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
                        <div className="markdown-body p-3">
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
                                // onChange={(e) => {
                                //     setUserMessage(e.target.value);
                                // }}
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
                                <Button onClick={() => {}}>
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
