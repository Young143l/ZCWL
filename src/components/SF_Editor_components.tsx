import { type Monaco, Editor, type OnMount } from "@monaco-editor/react";
import { Menu, Button, Popover, Popconfirm, Modal, theme, message } from "antd";
import { type FC, useEffect, useRef, useState } from "react";
import { type SF } from "../pages/Code/CodeSF";
import {
    SelectOutlined,
    CodeOutlined,
    DeleteOutlined,
    ReloadOutlined,
    PlusOutlined,
} from "@ant-design/icons";
import github from "../../public/GitHub Light.json";
import { Console } from "console-feed";
import { delCodeSF } from "../api/Code_api";
import useLogin from "../status/Login_status";
import { useNavigate } from "react-router-dom";
import useAIChatDoc from "../status/AIChatDoc_status";
import useIsDark from "../status/IsDark_status";

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
    setReLoadKey: React.Dispatch<React.SetStateAction<number>>;
}> = ({ sf, setSf, isSelect, setIsSelect, setReLoadKey }) => {
    // "use no memo";
    const { setCode, setIsAIChatOpen } = useAIChatDoc();
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
    const [messageApi, contextHolder] = message.useMessage();
    const { isDark } = useIsDark();
    const editorRef = useRef<Parameters<OnMount>[1]>(null);

    const handleEditorWillMount = (monaco: Monaco) => {
        monaco.editor.defineTheme("github-light", github);
    };
    const handleEditorMount: OnMount = (editor) => {
        editorRef.current = editor;
    };
    const getSelectedContent = () => {
        const editor = editorRef.current;
        // if (!editor) return;

        const selection = editor.getSelection();
        const selectedText = editor.getModel()?.getValueInRange(selection);
        // console.log(selectedText);

        if (selection.isEmpty()) {
            messageApi.error("请选中部分代码");
            return;
        } else {
            setCode(selectedText);
            messageApi.success("添加成功");
            setTimeout(() => {
                setIsAIChatOpen(true);
            }, 200);
        }
    };

    const handleDel = () => {
        return new Promise((resolve) => {
            setTimeout(() => {
                delCodeSF(sf.sfId, token).then((res) => {
                    if (!res.ok) {
                        messageApi.error("删除失败");
                        resolve(null);
                    } else {
                        messageApi.success("删除成功");
                        setTimeout(() => {
                            nav("/code");
                        }, 500);
                        resolve(null);
                    }
                });
            }, 1000);
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
        <>
            {contextHolder}
            <div className="flex flex-col h-full p-1">
                <div
                    className="flex justify-between items-center mb-1  border-2 rounded-xl overflow-x-hidden pr-1 gap-0.5"
                    style={{
                        borderColor: pColor,
                    }}
                >
                    <h1 className="ml-2 mr-2 text-xl  pl-1">{sf.name}</h1>
                    <div className="flex-1 min-w-0">
                        <Menu
                            mode="horizontal"
                            items={items}
                            selectedKeys={[cur]}
                            className="bg-[#00000000]!"
                        />
                    </div>
                    <Popconfirm
                        title="删除此项目"
                        description={`您确定要删除${sf.name}吗？`}
                        onConfirm={handleDel}
                        okText="删除"
                        cancelText="取消"
                    >
                        <Button
                            color="primary"
                            icon={<DeleteOutlined />}
                            variant="text"
                        />
                    </Popconfirm>
                    <Button
                        color="primary"
                        icon={<ReloadOutlined />}
                        variant="text"
                        onClick={() => {
                            setReLoadKey((prev: number) => prev + 1);
                        }}
                    />
                    <Modal
                        title="控制台输出"
                        open={showLogs}
                        onCancel={() => setShowLogs(false)}
                        footer={null}
                    >
                        <div
                            className={`w-full h-80 border-2 overflow-auto rounded-2xl bg-${isDark ? "gray-800" : "gray-50"}`}
                            style={{
                                borderColor: pColor,
                            }}
                        >
                            <div
                                className={`flex justify-between items-center p-2 bg-${isDark ? "black" : "white"} border-b rounded-t-2xl`}
                                style={{
                                    borderColor: pColor,
                                }}
                            >
                                <span
                                    className={`text-sm font-medium text-${isDark ? "gray-50" : "gray-700"}`}
                                >
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
                    <Button
                        color="primary"
                        icon={<PlusOutlined />}
                        variant="text"
                        onClick={() => {
                            getSelectedContent();
                        }}
                    />
                </div>
                <div
                    className="flex-1 border-2 rounded-xl overflow-hidden"
                    style={{
                        borderColor: pColor,
                    }}
                >
                    <Editor
                        height="100%"
                        theme={isDark ? "vs-dark" : "github-light"}
                        beforeMount={handleEditorWillMount}
                        language={cur}
                        onMount={handleEditorMount}
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
                            // console.log(editorRef.current.getPosition())
                        }}
                    />
                </div>
            </div>
        </>
    );
};

export default SF_Editor_components;
