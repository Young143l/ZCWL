import { Button, message, theme, Modal, Tag } from "antd";
import { DeleteOutlined, SendOutlined, CheckOutlined, CloseOutlined, EyeOutlined } from "@ant-design/icons";
import { useState, type FC } from "react";
import { Editor } from "@monaco-editor/react";
import { askCodeCP } from "../api/Code_api";
import useLogin from "../status/Login_status";
import LoadingWindow_components from "./AILoadingWindow_components";
import useIsDark from "../status/IsDark_status";

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
    const { isDark } = useIsDark();
    const [userMessage, setUserMessage] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);
    const [isOpen, setIsOpen] = useState<boolean>(false);
    const [success, setSuccess] = useState<boolean>(false);
    const [isReviewOpen, setIsReviewOpen] = useState<boolean>(false);
    const [pendingCode, setPendingCode] = useState<string>("");
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const mainColor = theme.useToken().token.colorPrimary;

    const handleAsk = () => {
        setIsOpen(true);
        setLoading(true);
        askCodeCP(token, code, userMessage, cpId).then(
            (res) => {
                if (res.ok) {
                    setSuccess(true);
                    const newCode = (res as { ok: boolean; code: string }).code;
                    setPendingCode(newCode);
                    setTimeout(() => {
                        setIsOpen(false);
                        setIsReviewOpen(true);
                    }, 200);
                } else {
                    setSuccess(false);
                    messageApi.error("请求失败，请重试");
                    setTimeout(() => {
                        setIsOpen(false);
                    }, 200);
                }
            },
        );
    };

    const handleAccept = () => {
        setCode(pendingCode);
        setUserMessage("");
        setPendingCode("");
        setIsReviewOpen(false);
        messageApi.success("代码已应用");
    };

    const handleReject = () => {
        setPendingCode("");
        setIsReviewOpen(false);
        messageApi.info("已取消应用");
    };

    return (
        <>
            {contextHolder}
            <LoadingWindow_components
                isOpen={isOpen}
                loading={loading}
                success={success}
            />
            <Modal
                title={
                    <div className="flex items-center gap-2">
                        <EyeOutlined style={{ color: mainColor }} />
                        <span>代码审查</span>
                        <Tag color="blue">AI 生成</Tag>
                    </div>
                }
                open={isReviewOpen}
                onCancel={handleReject}
                width="80%"
                style={{ maxWidth: 900 }}
                footer={
                    <div className="flex justify-end gap-2">
                        <Button
                            icon={<CloseOutlined />}
                            onClick={handleReject}
                        >
                            拒绝
                        </Button>
                        <Button
                            type="primary"
                            icon={<CheckOutlined />}
                            onClick={handleAccept}
                        >
                            应用更改
                        </Button>
                    </div>
                }
            >
                <div className="flex flex-col gap-3">
                    <div
                        className="text-sm text-gray-500"
                        style={{ color: isDark ? "#aaa" : "#666" }}
                    >
                        请审查 AI 生成的代码，确认无误后点击"应用更改"
                    </div>
                    <div
                        className="border-2 rounded-xl overflow-hidden"
                        style={{ borderColor: pColor, height: 400 }}
                    >
                        <Editor
                            height="100%"
                            theme={isDark ? "vs-dark" : "light"}
                            language="python"
                            value={pendingCode}
                            options={{
                                readOnly: true,
                                minimap: { enabled: false },
                                fontSize: 14,
                                scrollBeyondLastLine: false,
                                wordWrap: "on",
                            }}
                        />
                    </div>
                </div>
            </Modal>
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
