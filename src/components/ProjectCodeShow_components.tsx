import { useMemo, type FC } from "react";
import github from "../../public/GitHub Light.json";
import { Editor, type Monaco } from "@monaco-editor/react";
import { Button, Empty, theme, Spin } from "antd";
import { CloseOutlined, LoadingOutlined } from "@ant-design/icons";
export interface PCSProps {
    filePath: string | "NOFILE";
    setFilePath: React.Dispatch<React.SetStateAction<string>>;
    code: string;
    loading?: boolean;
}

const ProjectCodeShow_components: FC<PCSProps> = ({
    filePath,
    code,
    setFilePath,
    loading = false,
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

    // console.log(fileType)
    return (
        <div className="p-1 h-full">
            <div
                className=" border-2 rounded-2xl p-1 overflow-hidden h-full"
                style={{
                    borderColor: pColor,
                }}
            >
                {filePath != "NOFILE" ? (
                    <>
                        <div className="flex h-8 items-center justify-between bg-gray-100 rounded-t-lg p-5">
                            <span className="ml-2 text-sm font-mono truncate ">
                                {filePath}
                            </span>
                            <Button
                                color="primary"
                                icon={<CloseOutlined />}
                                variant="text"
                                onClick={() => {
                                    setFilePath("NOFILE");
                                }}
                            />
                        </div>

                        <div className="relative h-full">
                            {loading && (
                                <div className="absolute inset-0 flex items-center justify-center bg-white/80 z-10">
                                    <Spin
                                        indicator={<LoadingOutlined spin />}
                                        size="large"
                                    />
                                </div>
                            )}
                            <Editor
                                height="100%"
                                theme="github-light"
                                beforeMount={handleEditorWillMount}
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
                        <Empty description={<div>请选择一个文件</div>} />
                    </div>
                )}
            </div>
        </div>
    );
};

export default ProjectCodeShow_components;
