import {
    Input,
    Button,
    ConfigProvider,
    message,
    theme,
    Tag,
    Popover,
    Upload,
} from "antd";
import { useState, type FC, useEffect, useRef } from "react";
import {
    SendOutlined,
    ClearOutlined,
    PlusCircleOutlined,
    PictureOutlined,
} from "@ant-design/icons";
const { TextArea } = Input;
import AIChatBody_components from "./AIChatBody_components";
import useAIChatDoc from "../status/AIChatDoc_status";
import { getNewChat, getAsk } from "../api/AIChatDoc_api";
import { ragSearch } from "../api/RAG_api";
import useLogin from "../status/Login_status";
import { useNavigate } from "react-router-dom";
import { flushSync } from "react-dom";
import useIsDark from "../status/IsDark_status";
import { ensureCompressedImage } from "../utils/imageCompress";

const AIChatDoc_components: FC = () => {
    const [inputValue, setInputValue] = useState<string>("");
    const [asking, setAsking] = useState<boolean>(false);
    const { setAns, have, chat, id, newChat, clear, addChat, code, setCode, setSources, img, setImg } =
        useAIChatDoc();
    const { isLogin, token, userId } = useLogin();
    const nav = useNavigate();
    const chatContainerRef = useRef<HTMLDivElement>(null);
    const [messageApi, contextHolder] = message.useMessage();
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const {isDark} =useIsDark()
    const scrollToBottom = () => {
        if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop =
                chatContainerRef.current.scrollHeight;
        }
    };

    useEffect(() => {
        scrollToBottom();
    }, [chat]);

    // 处理图片上传（带压缩）
    const handleImgUpload = (file: File) => {
        const isImage = file.type.startsWith("image/");
        if (!isImage) {
            messageApi.error("只能上传图片文件！");
            return false;
        }
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = async () => {
            try {
                // 压缩图片到约 100KB
                const compressed = await ensureCompressedImage(reader.result as string, 100);
                setImg(compressed);
                messageApi.success("图片已压缩并上传");
            } catch (e) {
                console.error("图片压缩失败:", e);
                // 压缩失败时使用原图
                setImg(reader.result as string);
            }
        };
        return false;
    };

    const handleAsk = async () => {
        if (chat.length >= 100) {
            messageApi.open({
                type: "warning",
                content: "此对话次数超限，请开启新的对话。",
            });
            return;
        }
        if (!isLogin) {
            nav("/login");
            return;
        }
        setAsking(true);

        let currentId = id;
        if (!have) {
            const res = await getNewChat(userId, token);
            if (res.ok) {
                newChat(res.id);
                currentId = res.id;
            } else {
                // console.error("Failed to create new chat:", res.message);
                messageApi.open({
                    type: "error",
                    content: "内部错误",
                });
                setAsking(false);
                return;
            }
        }
        const ask =
            (code !== null ? "```\n" + code + "\n```\n" : "") + inputValue;
        const currentImg = img;
        setInputValue("");
        setCode(null);
        setImg(null);

        addChat({ id: "", ask: ask, over: false, ans: "", img: currentImg || undefined });

        // 同时进行RAG搜索获取溯源信息
        const ragPromise = ragSearch(inputValue, token);

        const askRes = await getAsk(userId, currentId, ask, token, currentImg || undefined);
        if (askRes.ok) {
            const t = chat.length;
            const reader = askRes.ans as ReadableStreamDefaultReader<
                Uint8Array<ArrayBuffer>
            >;
            const decoder = new TextDecoder();
            let buffer = "";

            while (true) {
                const { done, value } = await reader.read();
                if (done) {
                    break;
                }
                buffer += decoder.decode(value, { stream: true });
                flushSync(() => {
                    setAns(t.toString(), buffer);
                });
            }
            // 流式输出完成后，设置RAG溯源信息
            // 设置 RAG 距离阈值常量（distance 越小相似度越高）
            const RAG_DISTANCE_THRESHOLD = 0.55;
            try {
                const ragResults = await ragPromise;
                // 过滤出所有 distance 小于阈值的结果
                const filteredResults = ragResults.filter(r => r.distance < RAG_DISTANCE_THRESHOLD);
                if (filteredResults.length > 0) {
                    setSources(filteredResults);
                }
            } catch (e) {
                console.error("RAG search failed:", e);
            }
        } else {
            messageApi.open({
                type: "error",
                content: "内部错误",
            });
            setAns(chat.length.toString(), "");
        }
        setAsking(false);
    };

    const newAsk = () => {
        clear();
        setAsking(false);
    };

    return (
        <ConfigProvider
            theme={{
                components: {
                    Switch: {
                        trackHeight: 24,
                        handleSize: 20,
                    },
                },
            }}
        >
            {contextHolder}
            <div
                ref={chatContainerRef}
                className="mb-2 p-2  min-h-75 max-h-75 rounded-[5px] border border-[${theme.useToken().token.colorPrimaryBorder}] overflow-auto"
                style={{
                    borderColor: pColor,
                }}
            >
                {have ? (
                    chat.map((i) => (
                        <AIChatBody_components
                            over={i.over}
                            ans={i.ans}
                            ask={i.ask}
                            id={i.id}
                            key={i.id}
                            sources={i.sources}
                            img={i.img}
                        />
                    ))
                ) : (
                    <h1 className="mt-30 text-xl text-center font-bold text-gray-600">
                        输入您的问题开始对话吧！
                    </h1>
                )}
            </div>
            <TextArea
                rows={3}
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
                placeholder="请输入您的问题。(Enter发送, Shift+Enter换行)"
                maxLength={2000}
                showCount
                onKeyDown={(e) => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                        e.preventDefault();
                        if (!asking && inputValue !== "") {
                            handleAsk();
                        }
                    }
                }}
            />
            <div className="w-full pt-1 flex justify-end gap-2 mt-5">
                <div className="w-full flex items-center gap-1">
                    {/* <Switch
                        checkedChildren="快速模式"
                        unCheckedChildren="详细模式"
                        defaultChecked
                    /> */}
                    <PlusCircleOutlined
                        onClick={() => {
                            newAsk();
                        }}
                    />
                    {code ? (
                        <Popover
                            content={
                                <div className={`w-60 h-60 ${isDark?"bg-gray-800":"bg-gray-100"} rounded-xl p-2 flex justify-center whitespace-pre-wrap break-all overflow-y-scroll`}>
                                    {code}
                                </div>
                            }
                        >
                            <Tag
                                closeIcon
                                onClose={() => {
                                    setCode(null);
                                }}
                                variant="filled"
                                // color={pColor}
                            >
                                Code Snippet
                            </Tag>
                        </Popover>
                    ) : (
                        <></>
                    )}
                    {img ? (
                        <Popover
                            content={
                                <div className="w-60 h-60 rounded-xl p-2 flex justify-center items-center overflow-hidden">
                                    <img src={img} alt="预览" className="max-w-full max-h-full object-contain" />
                                </div>
                            }
                        >
                            <Tag
                                closeIcon
                                onClose={() => {
                                    setImg(null);
                                }}
                                variant="filled"
                                color="blue"
                            >
                                图片
                            </Tag>
                        </Popover>
                    ) : (
                        <Upload
                            accept="image/*"
                            beforeUpload={handleImgUpload}
                            showUploadList={false}
                            disabled={!!img}
                        >
                            <Button icon={<PictureOutlined />} size="small" disabled={!!img}>
                                图片
                            </Button>
                        </Upload>
                    )}
                </div>

                <Button
                    onClick={() => {
                        setInputValue("");
                        setCode(null);
                    }}
                >
                    <ClearOutlined />
                    清空
                </Button>
                <Button
                    onClick={() => {
                        handleAsk();
                    }}
                    disabled={asking || inputValue == ""}
                >
                    <SendOutlined />
                    发送
                </Button>
            </div>
        </ConfigProvider>
    );
};

export default AIChatDoc_components;
