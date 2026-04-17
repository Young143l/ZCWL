import { useEffect, useState, useRef, type FC } from "react";
import Template_Page from "../Template_Page";
import { Splitter, theme, Button, Popconfirm, message } from "antd";
import { Editor, type Monaco, type OnMount } from "@monaco-editor/react";
import github from "../../../public/GitHub Light.json";
import Console_components from "../../components/Console_components";
import { useParams, useNavigate } from "react-router-dom";
import { getCodeCP, delCodeCP } from "../../api/Code_api";
import useLogin from "../../status/Login_status";
import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import CP_Ask_components from "../../components/CP_Ask_components";
import useAIChatDoc from "../../status/AIChatDoc_status";
import useIsDark from "../../status/IsDark_status";

export type CodeType = "python";

export interface CP {
    cpId: string;
    name: string;
    type: CodeType;
    code: string;
}

const CodeCP: FC = () => {
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const [cp, setCP] = useState<CP>({
        type: "python",
        name: "test",
        cpId: "",
        code: "",
    });
    const { cp_id } = useParams();
    const { token } = useLogin();
    const nav = useNavigate();
    const [messageApi, contextHolder] = message.useMessage();
    const { setCode: setAIChatCode, setIsAIChatOpen } = useAIChatDoc();
    const editorRef = useRef<Parameters<OnMount>[1] | null>(null);
    const { isDark } = useIsDark();
    useEffect(() => {
        getCodeCP(token, cp_id as string).then((res) => {
            if (res.ok) {
                console.log(res.cp);
                setCP(res.cp as CP);
            }
        });
    }, [cp_id, token]);

    const handleEditorWillMount = (monaco: Monaco) => {
        monaco.editor.defineTheme("github-light", github);
    };

    const handleEditorMount: OnMount = (editor) => {
        editorRef.current = editor;
    };

    const getSelectedContent = () => {
        const editor = editorRef.current;
        if (!editor) return;

        const selection = editor.getSelection();
        const selectedText = editor.getModel()?.getValueInRange(selection);

        if (selection.isEmpty()) {
            messageApi.error("请选中部分代码");
            return;
        } else {
            setAIChatCode(selectedText);
            messageApi.success("添加成功");
            setTimeout(() => {
                setIsAIChatOpen(true);
            }, 200);
        }
    };

    const handleDel = () => {
        return new Promise((resolve) => {
            setTimeout(() => {
                delCodeCP(cp_id as string, token).then((res) => {
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

    const setCode = (code: React.SetStateAction<string>) => {
        setCP((prev) => {
            return {
                ...prev,
                code: typeof code === "function" ? code(prev.code) : code,
            };
        });
    };

    return (
        <>
            {contextHolder}
            <Template_Page>
                <div className="h-[calc(100vh-95px)] w-full">
                    <Splitter>
                        <Splitter.Panel
                            defaultSize="65%"
                            min="10%"
                            max="90%"
                            collapsible
                        >
                            <Splitter orientation="vertical">
                                <Splitter.Panel
                                    defaultSize="65%"
                                    min="10%"
                                    max="90%"
                                    collapsible
                                >
                                    <div className="p-1 h-full">
                                        <div
                                            className=" border-2 rounded-xl overflow-hidden h-full flex flex-col"
                                            style={{
                                                borderColor: pColor,
                                            }}
                                        >
                                            <div
                                                className="h-10 border-b-2 border-b-gray-600 flex justify-between items-center px-4"
                                                style={{
                                                    borderColor: pColor,
                                                }}
                                            >
                                                <div className="font-medium text-xl">
                                                    {cp.name}
                                                </div>
                                                <div className="flex gap-1">
                                                    <Button
                                                        color="primary"
                                                        icon={<PlusOutlined />}
                                                        variant="text"
                                                        onClick={
                                                            getSelectedContent
                                                        }
                                                        // title="添加选中代码到 AIChatDoc"
                                                    />
                                                    <Popconfirm
                                                        title="删除此项目"
                                                        description={`您确定要删除 ${cp.name} 吗？`}
                                                        onConfirm={handleDel}
                                                        okText="删除"
                                                        cancelText="取消"
                                                    >
                                                        <Button
                                                            color="primary"
                                                            icon={
                                                                <DeleteOutlined />
                                                            }
                                                            variant="text"
                                                        />
                                                    </Popconfirm>
                                                </div>
                                            </div>
                                            <Editor
                                                theme={
                                                    isDark
                                                        ? "vs-dark"
                                                        : "github-light"
                                                }
                                                beforeMount={
                                                    handleEditorWillMount
                                                }
                                                onMount={handleEditorMount}
                                                language={cp.type}
                                                options={{
                                                    minimap: { enabled: false },
                                                    fixedOverflowWidgets: true,
                                                    fontSize: 14,
                                                    scrollBeyondLastLine: false,
                                                    renderLineHighlight: "all",
                                                    wordWrap: "on",
                                                }}
                                                value={cp.code}
                                                onChange={(val) => {
                                                    setCP((prev) => {
                                                        return {
                                                            ...prev,
                                                            code: val as string,
                                                        };
                                                    });
                                                }}
                                            />
                                        </div>
                                    </div>
                                </Splitter.Panel>
                                <Splitter.Panel>
                                    <CP_Ask_components
                                        cpId={cp_id as string}
                                        code={cp.code}
                                        setCode={setCode}
                                    />
                                </Splitter.Panel>
                            </Splitter>
                        </Splitter.Panel>
                        <Splitter.Panel>
                            <Console_components
                                cpId={cp_id || ""}
                                token={token}
                                code={cp.code}
                            />
                        </Splitter.Panel>
                    </Splitter>
                </div>
            </Template_Page>
        </>
    );
};

export default CodeCP;
