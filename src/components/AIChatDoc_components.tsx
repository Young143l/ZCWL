import { Input, Button, Switch, ConfigProvider } from "antd";
import { useState, type FC, useEffect, useRef } from "react";
import { SendOutlined, ClearOutlined,PlusCircleOutlined } from "@ant-design/icons";
const { TextArea } = Input;
import AIChatBody_components from "./AIChatBody_components";
import useAIChatDoc from "../status/AIChatDoc_status";
import { getNewChat,getAsk } from "../api/AIChatDoc_api";
import useLogin from "../status/Login_status";
import { useNavigate } from "react-router-dom";

const AIChatDoc_components: FC = () => {
    const [inputValue, setInputValue] = useState<string>("");
    const [asking, setAsking] = useState<boolean>(false);
    const { setAns,have, chat, id, newChat, clear, addChat } = useAIChatDoc();
    const { isLogin, token, userId } = useLogin();
    const nav = useNavigate();
    const chatContainerRef = useRef<HTMLDivElement>(null);
    
    const scrollToBottom = () => {
        if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight;
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
        // 如果没有现有对话，先创建新对话
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
        
        // 使用正确的对话ID发送消息
        const askRes = await getAsk(userId, currentId, inputValue, token);
        if(askRes.ok) {
            setAns(askRes.id, askRes.ans);
        }
        setAsking(false);
        
    };

    const newAsk=()=>{
        clear();
    }

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
            <div ref={chatContainerRef} className="mb-2 p-2  min-h-75 max-h-75 rounded-[5px] border border-gray-300 overflow-auto">
                {have ? (
                    chat.map((i) => (
                        <AIChatBody_components
                            over={i.over}
                            ans={i.ans}
                            ask={i.ask}
                            id={i.id}
                        />
                    ))
                ) : (
                    <></>
                )}
            </div>
            <TextArea
                rows={3}
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
            />
            <div className="w-full pt-1 flex justify-end gap-2">
                <div className="w-full flex items-center">
                    <Switch
                        checkedChildren="快速模式"
                        unCheckedChildren="详细模式"
                        defaultChecked
                    />
                </div>
                <PlusCircleOutlined onClick={()=>{
                    newAsk()
                }} />
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
                    disabled={asking||(inputValue=="")}
                >
                    <SendOutlined />
                    发送
                </Button>
            </div>
        </ConfigProvider>
    );
};

export default AIChatDoc_components;