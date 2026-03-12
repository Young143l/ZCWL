import { Tag, Button, message, theme } from "antd";
import {
    AppstoreOutlined,
    DeleteOutlined,
    PlusOutlined,
    SendOutlined,
} from "@ant-design/icons";
import { useEffect, useRef, useState, type FC } from "react";
import { type SF, type code } from "../pages/Code/CodeSF";
import { askCodeSF } from "../api/Code_api";
import useLogin from "../status/Login_status";
import LoadingWindow_components from "./AILoadingWindow_components";

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
    const [userMessage, setUserMessage] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);
    const [isOpen, setIsOpen] = useState<boolean>(false);
    const [success, setSuccess] = useState<boolean>(false);
    const pColor = theme.useToken().token.colorPrimaryBorder;

    useEffect(() => {
        selectedIDsRef.current = selectedIds;
    }, [selectedIds]);

    const handleAsk = () => {
        setIsOpen(true);
        setLoading(true);
        askCodeSF(token, sf.code, selectedIds, userMessage, sf.sfId).then(
            (res) => {
                // console.log(res)
                if (res.ok) {
                    setSuccess(true);
                    setUserMessage("");
                    setSelectedIds([]);
                    setSf((prev) => {
                        return {
                            ...prev,
                            code: (res as { ok: boolean; code: code }).code,
                        };
                    });
                } else {
                    setSuccess(false);
                }
                setTimeout(() => {
                    setIsOpen(false);
                }, 200);
            },
        );
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

    return (
        <>
            {contextHolder}
            <LoadingWindow_components
                isOpen={isOpen}
                loading={loading}
                success={success}
            />
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
                    <div className="flex justify-end">
                        <Button
                            onClick={() => {
                                handleAsk();
                            }}
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
