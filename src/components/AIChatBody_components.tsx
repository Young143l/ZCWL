import { Avatar, Spin } from "antd";
import type { FC } from "react";
import { OpenAIOutlined } from "@ant-design/icons";
import { type AIChatBody } from "../status/AIChatDoc_status";
import ReactMarkdown from "react-markdown";
import "github-markdown-css/github-markdown.css";
import remarkGfm from "remark-gfm";
const AIChatBody_components: FC<AIChatBody> = ({ ask, ans, over }) => {
    return (
        <div className="w-full">
            <div className="flex justify-end gap-1.5">
                <div className="border border-gray-300 rounded-xl p-2 whitespace-normal max-w-5/6">
                    {ask}
                </div>
                <div>
                    <Avatar />
                </div>
            </div>
            <br />
            <div className="flex justify-start gap-1.5">
                <div>
                    <Avatar icon={<OpenAIOutlined />} />
                </div>
                <div className="border border-gray-300 rounded-xl p-2 whitespace-normal max-w-5/6">
                    {over ? (
                        <div className="markdown-body">
                            <ReactMarkdown remarkPlugins={[remarkGfm]}>{ans}</ReactMarkdown>
                        </div>
                    ) : (
                        <Spin />
                    )}
                </div>
            </div>
            <br />
        </div>
    );
};

export default AIChatBody_components;
