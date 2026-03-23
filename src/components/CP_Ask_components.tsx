import { Button, message, theme } from "antd";
import { DeleteOutlined, SendOutlined } from "@ant-design/icons";
import { useState, type FC } from "react";
import { askCodeCP } from "../api/Code_api";
import useLogin from "../status/Login_status";
import LoadingWindow_components from "./AILoadingWindow_components";

interface CP_Ask {
    cpId: string;
    code: string;
    setCode: React.Dispatch<React.SetStateAction<string>>;
}

const CP_Ask_components: FC<CP_Ask> = ({
    cpId,
    code,
    setCode,
}) => {
    const [messageApi, contextHolder] = message.useMessage();
    const { token } = useLogin();
    const [userMessage, setUserMessage] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);
    const [isOpen, setIsOpen] = useState<boolean>(false);
    const [success, setSuccess] = useState<boolean>(false);
    const pColor = theme.useToken().token.colorPrimaryBorder;

    const handleAsk = () => {
        setIsOpen(true);
        setLoading(true);
        askCodeCP(token, code, userMessage, cpId).then(
            (res) => {
                if (res.ok) {
                    setSuccess(true);
                    setUserMessage("");
                    setCode((res as { ok: boolean; code: string }).code);
                } else {
                    setSuccess(false);
                    messageApi.error("请求失败，请重试");
                }
                setTimeout(() => {
                    setIsOpen(false);
                }, 200);
            },
        );
    };

    return (
        <>
            {contextHolder}
            <LoadingWindow_components
                isOpen={isOpen}
                loading={loading}
                success={success}
            />
            <div className="h-full p-1 overflow-auto">
                <div
                    className="h-full border-2 rounded-2xl p-1.5 flex flex-col gap-2 overflow-auto"
                    style={{
                        borderColor: pColor,
                    }}
                >
                    <div className="flex justify-between items-center">
                        <div className="font-medium text-lg">AI 助手</div>
                        <Button
                            color="primary"
                            icon={<DeleteOutlined />}
                            variant="text"
                            onClick={() => setUserMessage("")}
                        >
                            清空
                        </Button>
                    </div>
                    <textarea
                        className="w-full focus:outline-none border-none resize-none grow min-h-18 p-2 border-2 rounded-xl"
                        style={{ borderColor: pColor }}
                        placeholder="在此输入您的需求，AI 将帮您生成代码..."
                        value={userMessage}
                        onChange={(e) => {
                            setUserMessage(e.target.value);
                        }}
                    ></textarea>
                    <div className="flex justify-end">
                        <Button
                            onClick={() => {
                                handleAsk();
                            }}
                            disabled={userMessage.length === 0}
                            // type="text"
                        >
                            <SendOutlined />
                            发送
                        </Button>
                    </div>
                </div>
            </div>
        </>
    );
};

export default CP_Ask_components;
