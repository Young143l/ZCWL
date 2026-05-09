import { useEffect, useState, useRef, useCallback, type FC } from "react";
import Template_Page from "../Template_Page";
import { Splitter, theme, Button, Popconfirm, message } from "antd";
import { Editor, type OnMount } from "@monaco-editor/react";
import Console_components from "../../components/Console_components";
import { useParams, useNavigate } from "react-router-dom";
import { getCodeCP, delCodeCP, saveCodeCP } from "../../api/Code_api";
import useLogin from "../../status/Login_status";
import { DeleteOutlined, PlusOutlined, SaveOutlined } from "@ant-design/icons";
import CP_Ask_components from "../../components/CP_Ask_components";
import useAIChatDoc from "../../status/AIChatDoc_status";
import useIsDark from "../../status/IsDark_status";
import { registerInlineCompletion } from "../../hooks/useInlineCompletion";

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
    const [saving, setSaving] = useState(false);
    const { setCode: setAIChatCode, setIsAIChatOpen } = useAIChatDoc();
    const editorRef = useRef<Parameters<OnMount>[1] | null>(null);
    const completionDisposableRef = useRef<{ dispose: () => void } | null>(null);
    const { isDark } = useIsDark();

    useEffect(() => {
        getCodeCP(token, cp_id as string).then((res) => {
            if (res.ok) {
                console.log(res.cp);
                setCP(res.cp as CP);
            }
        });
    }, [cp_id, token]);

    const handleEditorMount: OnMount = (editor, monaco) => {
        editorRef.current = editor;
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        completionDisposableRef.current = registerInlineCompletion(monaco as any, editor as any, token, () => cp.type, cp_id, false);
    };

    const handleSave = useCallback(async () => {
        setSaving(true);
        try {
            const res = await saveCodeCP(token, cp.code, cp_id as string);
            if (res.ok) {
                messageApi.success("保存成功");
            } else {
                messageApi.error("保存失败");
            }
        } catch {
            messageApi.error("保存失败");
        } finally {
            setSaving(false);
        }
    }, [token, cp.code, cp_id, messageApi]);

    // Ctrl+S / Cmd+S 保存快捷键
    useEffect(() => {
        const ed = editorRef.current;
        if (!ed) return;

        const keyDisposable = ed.addAction({
            id: "manual-save",
            label: "手动保存",
            keybindings: [
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                (window as any).monaco?.KeyMod?.CtrlCmd | (window as any).monaco?.KeyCode?.KeyS,
            ],
            run: () => {
                handleSave();
            },
        });

        return () => {
            keyDisposable?.dispose();
        };
    }, [handleSave]);

    // 语言切换时重新注册快捷键
    useEffect(() => {
        const ed = editorRef.current;
        if (!ed) return;

        // 先清理旧的
        if (completionDisposableRef.current) {
            completionDisposableRef.current.dispose();
        }

        // 从编辑器实例获取 Monaco 实例
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        const monacoInstance = (window as any).monaco;
        if (!monacoInstance) return;

        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        completionDisposableRef.current = registerInlineCompletion(monacoInstance, ed as any, token, () => cp.type, cp_id, false);
    }, [cp.type, token, cp_id]);

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
                                                        icon={<SaveOutlined />}
                                                        variant="text"
                                                        onClick={handleSave}
                                                        loading={saving}
                                                        title="手动保存 (Ctrl+S)"
                                                    />
                                                    <Button
                                                        color="primary"
                                                        icon={<PlusOutlined />}
                                                        variant="text"
                                                        onClick={
                                                            getSelectedContent
                                                        }
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
                                                        : "vs"
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
                                                            code: val ?? "",
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
