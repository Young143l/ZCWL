import { Avatar, Spin } from "antd";
import type { FC } from "react";
import { type AIChatBody } from "../status/AIChatDoc_status";
import ReactMarkdown from "react-markdown";
// import "github-markdown-css/github-markdown-light.css"
// import "github-markdown-css/github-markdown.css";

import remarkGfm from "remark-gfm";
import UserAvatar_components from "./UserAvatar_components";
import { cdn, isCdn } from "../config/cdn";
import useIsDark from "../status/IsDark_status";
const AIChatBody_components: FC<AIChatBody> = ({ ask, ans, over }) => {
    const { isDark } = useIsDark();
    return (
        <div className="w-full">
            <div className="flex justify-end gap-1.5">
                <div className="border border-gray-300 rounded-xl p-2 whitespace-normal max-w-5/6">
                    <div
                        className={`${isDark ? "markdown-body-dark" : "markdown-body"}`}
                    >
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>
                            {ask}
                        </ReactMarkdown>
                    </div>
                </div>
                <div>
                    <UserAvatar_components size={36} />
                </div>
            </div>
            <br />
            <div className="flex justify-start gap-1.5">
                <div>
                    <Avatar
                        size={32}
                        src={(isCdn ? cdn : "../../public/") + "qwen.svg"}
                    />
                </div>
                <div className="border border-gray-300 rounded-xl p-2 whitespace-normal max-w-5/6">
                    {over ? (
                        <div
                            className={`${isDark ? "markdown-body-dark" : "markdown-body"}`}
                        >
                            <ReactMarkdown remarkPlugins={[remarkGfm]}>
                                {ans}
                            </ReactMarkdown>
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
