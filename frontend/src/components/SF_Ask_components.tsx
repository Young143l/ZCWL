import { Tag, Button, message, theme, Modal, Tabs } from "antd";
import {
    AppstoreOutlined,
    DeleteOutlined,
    PlusOutlined,
    SendOutlined,
    CheckOutlined,
    CloseOutlined,
    EyeOutlined,
    Html5Outlined,
    BgColorsOutlined,
    JavaScriptOutlined,
} from "@ant-design/icons";
import { useEffect, useRef, useState, type FC } from "react";
import { Editor } from "@monaco-editor/react";
import { type SF, type code } from "../pages/Code/CodeSF";
import { askCodeSF } from "../api/Code_api";
import useLogin from "../status/Login_status";
import LoadingWindow_components from "./AILoadingWindow_components";
import useIsDark from "../status/IsDark_status";

interface SF_Ask {
    isSelect: boolean;
    setIsSelect: React.Dispatch<React.SetStateAction<boolean>>;
    sf: SF;
    setSf: React.Dispatch<React.SetStateAction<SF>>;
}

const SF_Ask_components: FC<SF_Ask> = ({
    isSelect,
    setIsSelect,
    sf,
    setSf,
}) => {
    const [messageApi, contextHolder] = message.useMessage();
    const selectedIDsRef = useRef<string[]>([]);
    const [selectedIds, setSelectedIds] = useState<string[]>([]);
    const { token } = useLogin();
    const { isDark } = useIsDark();
    const [userMessage, setUserMessage] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);
    const [isOpen, setIsOpen] = useState<boolean>(false);
    const [success, setSuccess] = useState<boolean>(false);
    const [isReviewOpen, setIsReviewOpen] = useState<boolean>(false);
    const [pendingCode, setPendingCode] = useState<code | null>(null);
    const [activeTab, setActiveTab] = useState<string>("html");
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const mainColor = theme.useToken().token.colorPrimary;

    useEffect(() => {
        selectedIDsRef.current = selectedIds;
    }, [selectedIds]);

    const handleAsk = () => {
        setIsOpen(true);
        setLoading(true);
        askCodeSF(token, sf.code, selectedIds, userMessage, sf.sfId).then(
            (res) => {
                if (res.ok) {
                    setSuccess(true);
                    const newCode = (res as { ok: boolean; code: code }).code;
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
        if (pendingCode) {
            setSf((prev) => {
                return {
                    ...prev,
                    code: pendingCode,
                };
            });
        }
        setUserMessage("");
        setSelectedIds([]);
        setPendingCode(null);
        setIsReviewOpen(false);
        messageApi.success("代码已应用");
    };

    const handleReject = () => {
        setPendingCode(null);
        setIsReviewOpen(false);
        messageApi.info("已取消应用");
    };

    useEffect(() => {
        const handleMessage = (event: MessageEvent) => {
            const { type, id, tagName } = event.data;

            if (type !== "iframe-click") return;

            if (!id || id === "") {
                messageApi.warning("请为此元素设置ID！");
                return;
            }

            if (selectedIDsRef.current.includes(tagName + ":" + id)) {
                messageApi.warning("此元素已选择！");
                return;
            }

            setSelectedIds((prev) => [...prev, tagName + ":" + id]);
            setIsSelect((prev) => !prev);
        };

        window.addEventListener("message", handleMessage);
        return () => window.removeEventListener("message", handleMessage);
    }, [messageApi, setSelectedIds, setIsSelect]);

    const tabItems = [
        {
            key: "html",
            label: (
                <span className="flex items-center gap-1">
                    <Html5Outlined />
                    HTML
                </span>
            ),
            children: pendingCode && (
                <div style={{ height: 380 }}>
                    <Editor
                        height="100%"
                        theme={isDark ? "vs-dark" : "light"}
                        language="html"
                        value={pendingCode.html}
                        options={{
                            readOnly: true,
                            minimap: { enabled: false },
                            fontSize: 14,
                            scrollBeyondLastLine: false,
                            wordWrap: "on",
                        }}
                    />
                </div>
            ),
        },
        {
            key: "css",
            label: (
                <span className="flex items-center gap-1">
                    <BgColorsOutlined />
                    CSS
                </span>
            ),
            children: pendingCode && (
                <div style={{ height: 380 }}>
                    <Editor
                        height="100%"
                        theme={isDark ? "vs-dark" : "light"}
                        language="css"
                        value={pendingCode.css}
                        options={{
                            readOnly: true,
                            minimap: { enabled: false },
                            fontSize: 14,
                            scrollBeyondLastLine: false,
                            wordWrap: "on",
                        }}
                    />
                </div>
            ),
        },
        {
            key: "javascript",
            label: (
                <span className="flex items-center gap-1">
                    <JavaScriptOutlined />
                    JavaScript
                </span>
            ),
            children: pendingCode && (
                <div style={{ height: 380 }}>
                    <Editor
                        height="100%"
                        theme={isDark ? "vs-dark" : "light"}
                        language="javascript"
                        value={pendingCode.javascript}
                        options={{
                            readOnly: true,
                            minimap: { enabled: false },
                            fontSize: 14,
                            scrollBeyondLastLine: false,
                            wordWrap: "on",
                        }}
                    />
                </div>
            ),
        },
    ];

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
                width="85%"
                style={{ maxWidth: 1000 }}
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
                        className="border-2 rounded-xl overflow-hidden p-1"
                        style={{ borderColor: pColor, height: 450 }}
                    >
                        <Tabs
                            activeKey={activeTab}
                            onChange={setActiveTab}
                            items={tabItems}
                            className="h-full"
                            style={{ height: "100%" }}
                        />
                    </div>
                </div>
            </Modal>
            <div className="h-full p-1">
                <div
                    className="h-full border-2 rounded-2xl p-1.5 flex flex-col gap-2 overflow-auto "
                    style={{
                        borderColor: pColor,
                    }}
                >
                    {selectedIds.length ? (
                        <div className="flex flex-wrap gap-0.5">
                            {selectedIds.map((i: string) => (
                                <Tag
                                    closeIcon
                                    key={i}
                                    icon={<AppstoreOutlined />}
                                    onClose={(e) => {
                                        e.preventDefault();
                                        setSelectedIds((prevStatus) => {
                                            return prevStatus.filter((j) => {
                                                return j !== i;
                                            });
                                        });
                                    }}
                                >
                                    {i}
                                </Tag>
                            ))}
                            <Tag
                                onClick={() => {
                                    setSelectedIds([]);
                                }}
                                className="cursor-pointer"
                            >
                                <DeleteOutlined />
                            </Tag>
                        </div>
                    ) : (
                        <div className="flex justify-start gap-0.5">
                            <Tag
                                onClick={() => {
                                    setIsSelect(!isSelect);
                                }}
                                className="cursor-pointer"
                            >
                                <PlusOutlined />
                            </Tag>
                            <div
                                className="
                                            overflow-hidden text-ellipsis whitespace-nowrap text-gray-400"
                            >
                                此处添加需更改组件
                            </div>
                        </div>
                    )}
                    <textarea
                        className="w-full focus:outline-none border-none resize-none grow min-h-12"
                        placeholder="此处输入您的消息。"
                        value={userMessage}
                        onChange={(e) => {
                            setUserMessage(e.target.value);
                        }}
                    ></textarea>
                    <div className="flex justify-between items-baseline">
                        <span className="font-medium! text-gray-500! text-[0.75rem]">ⓘ 编辑器中 Alt + \ 调用AI补全</span>
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

export default SF_Ask_components;
