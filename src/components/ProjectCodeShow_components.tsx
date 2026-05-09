import { useMemo, useRef, useState, type FC, useEffect } from "react";
import github from "../../public/GitHub Light.json";
import { Editor, type Monaco, type OnMount } from "@monaco-editor/react";
import {
    Button,
    Empty,
    theme,
    Spin,
    Splitter,
    Input,
    message,
    Tag,
    Divider,
} from "antd";
import {
    AppstoreOutlined,
    ClearOutlined,
    CloseOutlined,
    DeleteOutlined,
    LoadingOutlined,
    PlusOutlined,
    SendOutlined,
    HistoryOutlined,
} from "@ant-design/icons";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
// import "github-markdown-css/github-markdown-light.css"
// import "github-markdown-css/github-markdown.css";

import { askProjectStream } from "../api/Project_api";
import ProjectDocument_components from "./ProjectDocument_components";
import useIsDark from "../status/IsDark_status";
export interface PCSProps {
    filePath: string | "NOFILE";
    setFilePath: React.Dispatch<React.SetStateAction<string>>;
    code: string;
    loading?: boolean;
    pName: string;
}

export interface CodeSnap {
    fileName: string;
    lineStart: number;
    lineEnd: number;
}

export interface ChatMessage {
    ask: string;
    ans: string;
}

const ProjectCodeShow_components: FC<PCSProps> = ({
    filePath,
    code,
    setFilePath,
    loading = false,
    pName,
}) => {
    const handleEditorWillMount = (monaco: Monaco) => {
        monaco.editor.defineTheme("github-light", github);
    };
    const pColor = theme.useToken().token.colorPrimaryBorder;

    const fileType = useMemo(() => {
        const ext = filePath.split(".").pop()?.toLowerCase() || "";
        const typeMap: Record<string, string> = {
            js: "javascript",
            jsx: "javascript",
            ts: "typescript",
            tsx: "typescript",
            hpp: "cpp",
            h: "c",
            cpp: "cpp",
            c: "c",
            py: "python",
            md: "markdown",
            java: "java",
            json: "json",
            css: "css",
            html: "html",
            xml: "xml",
            yaml: "yaml",
            yml: "yaml",
            sql: "sql",
            sh: "shell",
            bash: "shell",
            zsh: "shell",
            ps1: "powershell",
            go: "go",
            rs: "rust",
            rb: "ruby",
            php: "php",
            swift: "swift",
            kt: "kotlin",
            scala: "scala",
            r: "r",
            dart: "dart",
            lua: "lua",
            perl: "perl",
            dockerfile: "dockerfile",
        };
        return typeMap[ext] || "";
    }, [filePath]);

    // 状态管理：历史问答数组、当前问答（问题和回答分开）
    const [currentAsk, setCurrentAsk] = useState<string>("");
    const [ans, setAns] = useState<string>("");
    const [isLoading, setIsLoading] = useState<boolean>(false);
    const [inputValue, setInputValue] = useState<string>("");
    const [messageApi, contextHolder] = message.useMessage();
    const editorRef = useRef<Parameters<OnMount>[1]>(null);
    const [codeSnap, setCodeSnap] = useState<CodeSnap[]>([]);
    const [chatHistory, setChatHistory] = useState<ChatMessage[]>([]);
    const { isDark } = useIsDark();
    const chatContainerRef = useRef<HTMLDivElement>(null);
    const handleEditorMount: OnMount = (editor) => {
        editorRef.current = editor;
    };

    // 自动滚动到底部
    useEffect(() => {
        if (chatContainerRef.current) {
            chatContainerRef.current.scrollTop =
                chatContainerRef.current.scrollHeight;
        }
    }, [ans, chatHistory]);

    const handleAsk = async () => {
        const currentInput = inputValue;

        // 如果当前有问答在进行中，先保存到历史
        if (currentAsk !== "") {
            setChatHistory((prev) => [...prev, { ask: currentAsk, ans: ans }]);
        }

        // 开始新的问答
        setIsLoading(true);
        setCurrentAsk(currentInput);
        setAns("");
        console.log(codeSnap);
        setInputValue("");
        setCodeSnap([]);

        // 构建带有历史上下文的提问
        const historyContext = chatHistory.map((h) => h.ask).join("\n");
        const fullQuestion = historyContext
            ? `之前的对话:\n${historyContext}\n\n当前问题: ${currentInput}`
            : currentInput;

        let fullAns = "";
        const res = await askProjectStream(
            pName,
            fullQuestion,
            codeSnap,
            (text) => {
                setIsLoading(false);
                fullAns += text;
                setAns(fullAns);
            },
        );

        if (res.ok) {
            // 问答完成，不自动添加到历史，等待下一次提问时再添加
            return;
        } else {
            messageApi.error({
                content: "未知错误",
            });
        }
    };

    const getSelectedContent = () => {
        const editor = editorRef.current;
        // if (!editor) return;

        const selection = editor.getSelection();

        if (selection.isEmpty()) {
            messageApi.error("请选中部分代码");
            return;
        } else {
            messageApi.success("添加成功");
            setCodeSnap((prev) => [
                ...prev,
                {
                    fileName: filePath,
                    lineStart: selection.startLineNumber,
                    lineEnd: selection.endLineNumber,
                },
            ]);
            console.log(selection);
        }
    };

    return (
        <div className="p-1 h-full">
            {contextHolder}
            <div
                className=" border-2 rounded-2xl p-1 overflow-hidden h-full"
                style={{
                    borderColor: pColor,
                }}
            >
                <Splitter>
                    <Splitter.Panel defaultSize="60%" min="40%" max="100%">
                        {filePath != "NOFILE" ? (
                            <>
                                <div
                                    className={`flex h-8 items-center justify-between ${isDark ? "bg-gray-700" : "bg-gray-100"} rounded-t-lg p-5 mr-1 outline-1 ${isDark ? "outline-gray-700" : "outline-gray-100"} outline-2`}
                                >
                                    <span
                                        className={`ml-2 text-sm font-mono truncate ${isDark ? "text-gray-200" : ""}`}
                                    >
                                        {filePath}
                                    </span>
                                    <div>
                                        <Button
                                            color="primary"
                                            icon={<PlusOutlined />}
                                            variant="text"
                                            onClick={() => {
                                                getSelectedContent();
                                            }}
                                        />
                                        <Button
                                            color="primary"
                                            icon={<CloseOutlined />}
                                            variant="text"
                                            onClick={() => {
                                                setFilePath("NOFILE");
                                            }}
                                        />
                                    </div>
                                </div>

                                <div
                                    className={`relative h-[calc(100%-42px)] mr-1 ${isDark ? "outline-gray-700" : "outline-gray-100"} rounded-b-lg overflow-auto outline-2`}
                                >
                                    {loading && (
                                        <div
                                            className={`absolute inset-0 flex items-center justify-center bg-${isDark ? "black" : "white"}/80 z-10`}
                                        >
                                            <Spin
                                                indicator={
                                                    <LoadingOutlined spin />
                                                }
                                                size="large"
                                            />
                                        </div>
                                    )}
                                    <Editor
                                        height="100%"
                                        theme={
                                            isDark ? "vs-dark" : "github-light"
                                        }
                                        beforeMount={handleEditorWillMount}
                                        onMount={handleEditorMount}
                                        language={fileType}
                                        options={{
                                            readOnly: true,
                                            minimap: { enabled: false },
                                            fixedOverflowWidgets: true,
                                            fontSize: 14,
                                            scrollBeyondLastLine: false,
                                            renderLineHighlight: "all",
                                            wordWrap: "on",
                                        }}
                                        value={code}
                                    />
                                </div>
                            </>
                        ) : (
                            <div className="h-full w-full flex justify-center items-center">
                                <Empty
                                    description={<div>请选择一个文件</div>}
                                />
                            </div>
                        )}
                    </Splitter.Panel>
                    <Splitter.Panel
                        collapsible={{
                            start: true,
                            end: true,
                        }}
                    >
                        <div
                            className={`rounded-lg overflow-hidden h-full border-2 ${isDark ? "border-gray-700" : "border-gray-100"} ml-1 flex flex-col justify-between p-2 gap-2`}
                        >
                            <div
                                ref={chatContainerRef}
                                className="flex-1 overflow-auto"
                            >
                                {/* 历史对话 */}
                                {chatHistory.map((chat, index) => (
                                    <div key={index} className="p-1">
                                        <div className="w-full flex justify-end mb-2 pl-4 ">
                                            <div className="rounded-xl rounded-br-none border-2 border-gray-300 overflow-hidden p-2 max-w-[85%]">
                                                <div
                                                    className={`${isDark ? "markdown-body-dark" : "markdown-body"}`}
                                                >
                                                    <ReactMarkdown
                                                        remarkPlugins={[
                                                            remarkGfm,
                                                        ]}
                                                    >
                                                        {chat.ask}
                                                    </ReactMarkdown>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="w-full flex justify-start mb-2 pr-4">
                                            <div className="rounded-xl rounded-bl-none border-2 border-gray-300 overflow-hidden p-2 max-w-[85%]">
                                                <div
                                                    className={`${isDark ? "markdown-body-dark" : "markdown-body"}`}
                                                >
                                                    <ReactMarkdown
                                                        remarkPlugins={[
                                                            remarkGfm,
                                                        ]}
                                                    >
                                                        {chat.ans}
                                                    </ReactMarkdown>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                                {/* 当前问答 */}
                                {currentAsk !== "" && (
                                    <div className="p-1">
                                        <div className="w-full flex justify-end mb-2 pl-4 ">
                                            <div className="rounded-xl rounded-br-none border-2 border-gray-300 overflow-hidden p-2 max-w-[85%]">
                                                <div
                                                    className={`${isDark ? "markdown-body-dark" : "markdown-body"}`}
                                                >
                                                    <ReactMarkdown
                                                        remarkPlugins={[
                                                            remarkGfm,
                                                        ]}
                                                    >
                                                        {currentAsk}
                                                    </ReactMarkdown>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="w-full flex justify-start mb-2 pr-4">
                                            <div className="rounded-xl rounded-bl-none border-2 border-gray-300 overflow-hidden p-2 max-w-[85%]">
                                                {isLoading ? (
                                                    <Spin />
                                                ) : (
                                                    <div
                                                        className={`${isDark ? "markdown-body-dark" : "markdown-body"}`}
                                                    >
                                                        <ReactMarkdown
                                                            remarkPlugins={[
                                                                remarkGfm,
                                                            ]}
                                                        >
                                                            {ans}
                                                        </ReactMarkdown>
                                                    </div>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                )}
                                {/* 空状态提示 */}
                                {currentAsk === "" &&
                                    chatHistory.length === 0 && (
                                        <div className="w-full h-full flex justify-center items-center">
                                            <span className="font-medium text-gray-400">
                                                输入问题询问项目相关内容吧！
                                            </span>
                                        </div>
                                    )}
                            </div>
                            <Divider size="small" />

                            <div>
                                {codeSnap.length ? (
                                    <div className="flex flex-wrap gap-0.5">
                                        {codeSnap.map((i: CodeSnap) => (
                                            <Tag
                                                closeIcon
                                                key={
                                                    i.fileName +
                                                    ":" +
                                                    i.lineStart +
                                                    "-" +
                                                    i.lineEnd
                                                }
                                                icon={<AppstoreOutlined />}
                                                onClose={(e) => {
                                                    e.preventDefault();
                                                    setCodeSnap(
                                                        (prevStatus) => {
                                                            return prevStatus.filter(
                                                                (j) => {
                                                                    return (
                                                                        j !== i
                                                                    );
                                                                },
                                                            );
                                                        },
                                                    );
                                                }}
                                            >
                                                {i.fileName +
                                                    ":" +
                                                    i.lineStart +
                                                    "-" +
                                                    i.lineEnd}
                                            </Tag>
                                        ))}
                                        <Tag
                                            onClick={() => {
                                                setCodeSnap([]);
                                            }}
                                            className="cursor-pointer"
                                        >
                                            <DeleteOutlined />
                                        </Tag>
                                    </div>
                                ) : (
                                    // <div className="flex justify-start gap-0.5">
                                    //     <Tag
                                    //         onClick={() => {
                                    //             setIsSelect(!isSelect);
                                    //         }}
                                    //         className="cursor-pointer"
                                    //     >
                                    //         <PlusOutlined />
                                    //     </Tag>
                                    //     <div
                                    //         className="
                                    //             overflow-hidden text-ellipsis whitespace-nowrap text-gray-400"
                                    //     >
                                    //         此处添加需更改组件
                                    //     </div>
                                    // </div>
                                    <></>
                                )}
                            </div>

                            <div>
                                <Input.TextArea
                                    rows={3}
                                    value={inputValue}
                                    onChange={(e) =>
                                        setInputValue(e.target.value)
                                    }
                                    onPressEnter={(e) => {
                                        if (e.shiftKey) {
                                            // Shift + Enter 换行，不做处理
                                            return;
                                        }
                                        // Enter 发送
                                        e.preventDefault();
                                        if (!isLoading && inputValue !== "") {
                                            handleAsk();
                                        }
                                    }}
                                    placeholder="请输入关于项目的问题（Enter 发送，Shift+Enter 换行）"
                                    maxLength={2000}
                                />
                                <div className="w-full pt-1 flex justify-between mt-1">
                                    <ProjectDocument_components pName={pName} />
                                    <div className="flex gap-2 items-center justify-between">
                                        {/* 清空对话历史按钮 */}
                                        {(currentAsk != "" ||
                                            chatHistory.length > 0) && (
                                            <div className="w-full flex justify-end">
                                                <Button
                                                    size="small"
                                                    icon={<HistoryOutlined />}
                                                    onClick={() => {
                                                        setChatHistory([]);
                                                        setCurrentAsk("");
                                                        setAns("");
                                                    }}
                                                >
                                                    清空对话
                                                </Button>
                                            </div>
                                        )}
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
                                            disabled={
                                                isLoading || inputValue === ""
                                            }
                                        >
                                            <SendOutlined />
                                            发送
                                        </Button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </Splitter.Panel>
                </Splitter>
            </div>
        </div>
    );
};

export default ProjectCodeShow_components;
