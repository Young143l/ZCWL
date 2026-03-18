import { useMemo, type FC } from "react";
import github from "../../public/GitHub Light.json";
import { Editor, type Monaco } from "@monaco-editor/react";
import { Button, Empty, theme } from "antd";
import { CloseOutlined } from "@ant-design/icons";
export interface PCSProps {
    filePath: string | "NOFILE";
    setFilePath: React.Dispatch<React.SetStateAction<string>>;
    code: string;
}

const ProjectCodeShow_components: FC<PCSProps> = ({
    filePath,
    code,
    setFilePath,
}) => {
    const handleEditorWillMount = (monaco: Monaco) => {
        monaco.editor.defineTheme("github-light", github);
    };
    const pColor = theme.useToken().token.colorPrimaryBorder;

    const fileType: string = useMemo<string>(() => {
        switch ((filePath.split(".").pop() as string).toLowerCase()) {
            case "js":
                return "javascript";
            case "jsx":
                return "javascript";
            case "ts":
                return "typescript";
            case "tsx":
                return "typescript";
            case "cpp":
                return "cpp";
            case "c":
                return "c";
            case "py":
                return "pyhton";
            case "md":
                return "markdown";
            case "java":
                return "java";
            case "json":
                return "json";
            case "css":
                return "css";
            case "html":
                return "html";
            default:
                return "";
        }
    }, [filePath]);


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
