import { Tag, Button, message } from "antd";
import {
    AppstoreOutlined,
    DeleteOutlined,
    PlusOutlined,
    SendOutlined,
} from "@ant-design/icons";
import { useEffect, useRef, useState, type FC } from "react";
import { type SF } from "../pages/Code/CodeSF";

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
    useEffect(() => {
        selectedIDsRef.current = selectedIds;
    }, [selectedIds]);

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
            <div className="h-full p-1">
                <div className="h-full border-2 border-gray-300 rounded-2xl p-1.5 flex flex-col gap-2 overflow-auto">
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
                    ></textarea>
                    <div className="flex justify-end">
                        <Button>
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
