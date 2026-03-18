import { useEffect, useState, type FC } from "react";
import Template_Page from "../Template_Page";
import { Splitter, theme } from "antd";
import { Editor, type Monaco } from "@monaco-editor/react";
import github from "../../../public/GitHub Light.json";
import Console_components from "../../components/Console_components";
import { useParams } from "react-router-dom";
import { getCodeCP } from "../../api/Code_api";
import useLogin from "../../status/Login_status";

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


    useEffect(()=>{
        getCodeCP(token,cp_id as string).then((res)=>{
            if(res.ok){
                setCP((res.cp as CP))
            }
        })
    },[cp_id,token])

    const handleEditorWillMount = (monaco: Monaco) => {
        monaco.editor.defineTheme("github-light", github);
    };
    return (
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
                                defaultSize="75%"
                                min="10%"
                                max="90%"
                                collapsible
                            >
                                <div className="p-1 h-full">
                                    <div
                                        className=" border-2 rounded-xl overflow-hidden h-full"
                                        style={{
                                            borderColor: pColor,
                                        }}
                                    >
                                        <div className="h-8 border-b border-gray-300 flex justify-between items-center p-4">
                                            <div className="font-medium text-xl">
                                                {cp.name}
                                            </div>
                                        </div>
                                        <Editor
                                            height="100%"
                                            theme="github-light"
                                            beforeMount={handleEditorWillMount}
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
                                <Console_components />
                            </Splitter.Panel>
                        </Splitter>
                    </Splitter.Panel>
                    <Splitter.Panel>as</Splitter.Panel>
                </Splitter>
            </div>
        </Template_Page>
    );
};

export default CodeCP;
