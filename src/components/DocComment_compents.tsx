import type { FC } from "react";
import { Avatar, Button, Divider, theme } from "antd";
import { UserOutlined } from "@ant-design/icons";
import { Md5 } from "ts-md5";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import "github-markdown-css/github-markdown.css";

export interface Comment {
    id: string;
    uId: string;
    email: string;
    content: string;
    children: [
        {
            id: string;
            uId: string;
            email: string;
            content: string;
        },
    ];
}

export interface CommentProps {
    comment: Comment;
    setAt: React.Dispatch<
        React.SetStateAction<{
            name: string;
            fa: string;
        }>
    >;
}

const DocComment_compents: FC<CommentProps> = ({ comment, setAt }) => {
    const pColor = theme.useToken().token.colorPrimaryBorder;

    return (
        <>
            <div className="flex gap-2 items-start">
                <div className="shrink">
                    <Avatar
                        size={"large"}
                        icon={<UserOutlined />}
                        src={`https://cravatar.cn/avatar/${Md5.hashStr(comment.email)}`}
                    />
                </div>
                <div className="flex flex-col w-full">
                    <div className="w-full flex justify-between items-center">
                        <span
                            style={{
                                color: pColor,
                            }}
                            className="  font-bold text-xl"
                        >
                            {comment.uId}
                        </span>
                        <Button
                            color="primary"
                            variant="text"
                            onClick={() => {
                                setAt({ name: comment.uId, fa: comment.id });
                            }}
                        >
                            回复
                        </Button>
                    </div>
                    {/* <div className="text-xl break-all text-gray-800"> */}
                    <div className="markdown-body  break-all">
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>
                            {comment.content}
                        </ReactMarkdown>
                    </div>
                    {comment.children.length ? (
                        <div className="w-full flex flex-col gap-2 mt-2">
                            {comment.children.map((i) => {
                                return (
                                    <div className="w-full flex gap-1 items-start pt-2">
                                        <div className="shrink">
                                            <Avatar
                                                size={"large"}
                                                icon={<UserOutlined />}
                                                src={`https://cravatar.cn/avatar/${Md5.hashStr(i.email)}`}
                                            />
                                        </div>
                                        <div className="w-full">
                                            <div className="w-full flex justify-between items-center">
                                                <span
                                                    style={{
                                                        color: pColor,
                                                    }}
                                                    className="  font-bold text-xl"
                                                >
                                                    {i.uId}
                                                </span>
                                                <Button
                                                    color="primary"
                                                    variant="text"
                                                    onClick={() => {
                                                        setAt({
                                                            name: i.uId,
                                                            fa: comment.id,
                                                        });
                                                    }}
                                                >
                                                    回复
                                                </Button>
                                            </div>
                                            {/* <div className="text-xl break-all text-gray-800"> */}
                                            <div className="markdown-body   break-all">
                                                <ReactMarkdown
                                                    remarkPlugins={[remarkGfm]}
                                                >
                                                    {i.content}
                                                    {/* </div> */}
                                                </ReactMarkdown>
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                        </div>
                    ) : (
                        <></>
                    )}
                </div>
            </div>
            <Divider size="small" />
        </>
    );
};

export default DocComment_compents;
