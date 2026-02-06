import { Input, Button, ConfigProvider } from "antd";
import { useState, type FC, useEffect, useRef } from "react";
import {
    SendOutlined,
    ClearOutlined,
    PlusCircleOutlined,
} from "@ant-design/icons";
const { TextArea } = Input;
import AIChatBody_components from "./AIChatBody_components";
import useAIChatDoc from "../status/AIChatDoc_status";
import { getNewChat, getAsk } from "../api/AIChatDoc_api";
import useLogin from "../status/Login_status";
import { useNavigate } from "react-router-dom";

const AIChatDoc_components: FC = () => {
    const [inputValue, setInputValue] = useState<string>("");
    const [asking, setAsking] = useState<boolean>(false);
    const { setAns, have, chat, id, newChat, clear, addChat } = useAIChatDoc();
    const { isLogin, token, userId } = useLogin();
    const nav = useNavigate();
    const chatContainerRef = useRef<HTMLDivElement>(null);

    const scrollToBottom = () => {
        if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop =
                chatContainerRef.current.scrollHeight;
        }
    };

    useEffect(() => {
        scrollToBottom();
    }, [chat]);

    const handleAsk = async () => {
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
                console.error("Failed to create new chat:", res.message);
                setAsking(false);
                return;
            }
        }
        const ask = inputValue;
        setInputValue("");
        addChat({ id: "", ask: ask, over: false, ans: "" });

        const askRes = await getAsk(userId, currentId, inputValue, token);
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
                setAns(t.toString(), buffer);
            }
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
            <div
                ref={chatContainerRef}
                className="mb-2 p-2  min-h-75 max-h-75 rounded-[5px] border border-gray-300 overflow-auto"
            >
                {have ? (
                    chat.map((i) => (
                        <AIChatBody_components
                            over={i.over}
                            ans={i.ans}
                            ask={i.ask}
                            id={i.id}
                            key={i.id}
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
                placeholder="请输入您的问题。"
            />
            <div className="w-full pt-1 flex justify-end gap-2">
                <div className="w-full flex items-center">
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
                </div>

                <Button
                    onClick={() => {
                        setInputValue("");
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
