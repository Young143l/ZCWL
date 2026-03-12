import { type Monaco, Editor } from "@monaco-editor/react";
import { Menu, Button, Popover, Modal, theme } from "antd";
import { type FC, useEffect, useState } from "react";
import { type SF } from "../pages/Code/CodeSF";
import {
    SelectOutlined,
    CodeOutlined,
    DeleteOutlined,
} from "@ant-design/icons";
import github from "../../public/GitHub Light.json";
import { Console } from "console-feed";
import { delCodeSF } from "../api/Code_api";
import useLogin from "../status/Login_status";
import { useNavigate } from "react-router-dom";

type Methods =
    | "log"
    | "debug"
    | "info"
    | "warn"
    | "error"
    | "table"
    | "clear"
    | "time"
    | "timeEnd"
    | "count"
    | "assert"
    | "command"
    | "result"
    | "dir";

interface Message {
    id: string;
    method: Methods;
    data: unknown[];
    timestamp?: string;
}

const ItemsLable: FC<{
    text: "html" | "css" | "javascript";
    setCur: React.Dispatch<React.SetStateAction<"html" | "css" | "javascript">>;
}> = ({ text, setCur }) => {
    return (
        <div
            className="font-bold select-none"
            onClick={() => {
                setCur(text);
            }}
        >
            {text}
        </div>
    );
};

const SF_Editor_components: FC<{
    sf: SF;
    setSf: React.Dispatch<React.SetStateAction<SF>>;
    isSelect: boolean;
    setIsSelect: React.Dispatch<React.SetStateAction<boolean>>;
}> = ({ sf, setSf, isSelect, setIsSelect }) => {
    // "use no memo";
    const { token } = useLogin();
    const [cur, setCur] = useState<"html" | "css" | "javascript">("html");
    const items = [
        {
            label: <ItemsLable text="html" setCur={setCur} />,
            key: "html",
        },
        {
            label: <ItemsLable text="css" setCur={setCur} />,
            key: "css",
        },
        {
            label: <ItemsLable text="javascript" setCur={setCur} />,
            key: "javascript",
        },
    ];
    const nav = useNavigate();

    const handleEditorWillMount = (monaco: Monaco) => {
        monaco.editor.defineTheme("github-light", github);
    };

    const handleDel = () => {
        delCodeSF(sf.sfId, token).then((res) => {
            if (!res.ok) {
                return;
            } else {
                nav("/code");
            }
        });
    };

    const pColor = theme.useToken().token.colorPrimaryBorder;

    const [showLogs, setShowLogs] = useState<boolean>(false);
    const [logs, setLogs] = useState<Message[]>([]);

    useEffect(() => {
        const handleMessage = (event: MessageEvent) => {
            const data = event.data;
            if (data.type === "consoleLog") {
                const feedLog: Message = {
                    id: data,
                    timestamp: data.timestamp,
                    method: data.level,
                    data: data.payload,
                };
                setLogs((prevLogs) => [...prevLogs, feedLog]);
            }
        };
        window.addEventListener("message", handleMessage);
        return () => {
            window.removeEventListener("message", handleMessage);
        };
    }, []);

    return (
        <div className="flex flex-col h-full p-1">
            <div
                className="flex justify-between items-center mb-1  border-2 rounded-xl overflow-x-hidden pr-1 gap-0.5"
                style={{
                    borderColor: pColor,
                }}
            >
                <h1
                    className="ml-2 mr-2 text-xl border-l-4 pl-1"
                    style={{
                        borderColor: pColor,
                    }}
                >
                    {sf.name}
                </h1>
                <div className="flex-1 min-w-0">
                    <Menu
                        mode="horizontal"
                        items={items}
                        selectedKeys={[cur]}
                    />
                </div>
                <Button
                    color="primary"
                    icon={<DeleteOutlined />}
                    variant="text"
                    onClick={() => {
                        handleDel();
                    }}
                />
                <Modal
                    title="控制台输出"
                    open={showLogs}
                    onCancel={() => setShowLogs(false)}
                    footer={null}
                >
                    <div
                        className="w-full h-80 border-2 overflow-auto rounded-2xl bg-gray-50"
                        style={{
                            borderColor: pColor,
                        }}
                    >
                        <div className="flex justify-between items-center p-2 bg-white border-b border-gray-200 rounded-t-2xl">
                            <span className="text-sm font-medium text-gray-700">
                                可显示部分控制台输出
                            </span>
                            <Button
                                type="primary"
                                size="small"
                                icon={<DeleteOutlined />}
                                onClick={() => {
                                    setLogs([]);
                                }}
                            >
                                清空
                            </Button>
                        </div>
                        <div className="h-auto overflow-y-auto p-2 custom-scrollbar">
                            <Console logs={logs} />
                        </div>
                    </div>
                </Modal>
                <Button
                    color="primary"
                    icon={<CodeOutlined />}
                    variant="text"
                    onClick={() => {
                        setShowLogs(true);
                    }}
                />
                <Popover
                    content={
                        <>
                            <p>选择模式已开启，选</p>
                            <p>择你要更改的组件。</p>
                        </>
                    }
                    placement="bottom"
                    trigger="click"
                    open={isSelect}
                >
                    <Button
                        color="primary"
                        variant={isSelect ? "solid" : "text"}
                        icon={<SelectOutlined />}
                        onClick={() => {
                            setIsSelect(!isSelect);
                        }}
                    />
                </Popover>
            </div>
            <div
                className="flex-1 border-2 rounded-xl overflow-hidden"
                style={{
                    borderColor: pColor,
                }}
            >
                <Editor
                    height="100%"
                    theme="github-light"
                    beforeMount={handleEditorWillMount}
                    language={cur}
                    options={{
                        minimap: { enabled: false },
                        fixedOverflowWidgets: true,
                        fontSize: 14,
                        scrollBeyondLastLine: false,
                        renderLineHighlight: "all",
                        wordWrap: "on",
                    }}
                    value={sf.code[cur]}
                    onChange={(value: string | undefined) => {
                        if (value !== undefined) {
                            setSf((prevStatus) => ({
                                ...prevStatus,
                                code: {
                                    ...prevStatus.code,
                                    [cur]: value,
                                },
                            }));
                            setIsSelect(false);
                        }
                    }}
                />
            </div>
        </div>
    );
};

export default SF_Editor_components;
