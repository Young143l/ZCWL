import { type Monaco, Editor } from "@monaco-editor/react";
import { Menu, Button, Popover } from "antd";
import { type FC, useState } from "react";
import { type SF } from "../pages/Code/CodeSF";
import { SelectOutlined, DownloadOutlined } from "@ant-design/icons";
import github from "../../public/GitHub Light.json";

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

    const handleEditorWillMount = (monaco: Monaco) => {
        monaco.editor.defineTheme("github-light", github);
    };

    return (
        <div className="flex flex-col h-full p-1">
            <div className="flex justify-between items-center mb-1 border-gray-300 border-2 rounded-xl overflow-x-hidden pr-1 gap-0.5">
                <div className="flex-1 min-w-0">
                    <Menu
                        mode="horizontal"
                        items={items}
                        selectedKeys={[cur]}
                    />
                </div>
                <Button
                    color="primary"
                    icon={<DownloadOutlined />}
                    variant="text"
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
            <div className="flex-1 border-gray-300 border-2 rounded-xl overflow-hidden">
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
                        }
                    }}
                />
            </div>
        </div>
    );
};

export default SF_Editor_components;
