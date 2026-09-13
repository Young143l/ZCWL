import { Avatar, Spin, Popover, theme } from "antd";
import type { FC } from "react";
import useAIChatDoc, { type AIChatBody } from "../status/AIChatDoc_status";
import ReactMarkdown from "react-markdown";
// import "github-markdown-css/github-markdown-light.css"
// import "github-markdown-css/github-markdown.css";

import remarkGfm from "remark-gfm";
import UserAvatar_components from "./UserAvatar_components";
import { cdn, isCdn } from "../config/cdn";
import useIsDark from "../status/IsDark_status";
import { useNavigate } from "react-router-dom";

const AIChatBody_components: FC<AIChatBody> = ({ ask, ans, over, sources, img }) => {
    const {setIsAIChatOpen}=useAIChatDoc();
    const nav = useNavigate()
    const { isDark } = useIsDark();
    const { token: antdToken } = theme.useToken();
    return (
        <div className="w-full">
            <div className="flex justify-end gap-1.5">
                <div className="border border-gray-300 rounded-xl p-2 whitespace-normal max-w-5/6">
                    {img && (
                        <div className="mb-2">
                            <img 
                                src={img} 
                                alt="用户上传的图片" 
                                className="max-w-full max-h-40 rounded-lg object-contain" 
                            />
                        </div>
                    )}
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
                        src={(isCdn ? cdn : "../../public/") + "AIChat.jpg"}
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
                    {/* 溯源小方块 */}
                    {over && sources && sources.length > 0 && (
                        <div className="flex gap-2 mt-3 pt-2 border-t border-gray-200">
                            <span className="text-xs text-gray-500 self-center">
                                来源:
                            </span>
                            {sources.map((source, index) => (
                                <Popover
                                    key={source.id}
                                    content={
                                        <div
                                            className={`max-w-xs max-h-60 overflow-y-auto p-2 rounded ${isDark ? "bg-gray-800 text-white" : "bg-white"}`}
                                        >
                                            
                                            <div
                                                className={`${isDark ? "markdown-body-dark" : "markdown-body"}`}
                                            >
                                                <ReactMarkdown
                                                    remarkPlugins={[remarkGfm]}
                                                >
                                                    {source.content}
                                                </ReactMarkdown>
                                            </div>
                                            {/* <p className="text-sm whitespace-pre-wrap break-all">
                                                {source.content}
                                            </p> */}
                                        </div>
                                    }
                                    title={`来源 ${index + 1}`}
                                    trigger="hover"
                                >
                                    <div
                                        className="w-6 h-6 flex items-center justify-center rounded cursor-pointer text-xs font-medium transition-all hover:scale-110"
                                        style={{
                                            backgroundColor: antdToken.colorPrimary,
                                            color: antdToken.colorWhite,
                                        }}
                                        onClick={() => {
                                            // 跳转到文章相应章节
                                            nav(`/document/${source.d_id}/${source.c_id}`);
                                            setIsAIChatOpen(false);
                                        }}
                                    >
                                        {index + 1}
                                    </div>
                                </Popover>
                            ))}
                        </div>
                    )}
                </div>
            </div>
            <br />
        </div>
    );
};

export default AIChatBody_components;
