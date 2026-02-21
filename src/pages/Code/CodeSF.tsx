import Template_Page from "../Template_Page";
import { Splitter } from "antd";
import { useState, type FC } from "react";
import SF_Editor_components from "../../components/SF_Editor_components";
import SF_View_componetns from "../../components/SF_View_componetns";
import SF_Ask_components from "../../components/SF_Ask_components";

export interface SF {
    id: string;
    code: {
        html: string;
        css: string;
        javascript: string;
    };
}

const CodeSF: FC = () => {
    const [sf, setSf] = useState<SF>({
        id: "",
        code: {
            html: `<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Document</title>
</head>
<body>
</body>
</html>`,
            css: "",
            javascript: "",
        },
    });
    const [isSelect, setIsSelect] = useState<boolean>(false);

    return (
        <>
            <Template_Page>
                <div className="h-[calc(100vh-95px)] w-full">
                    <Splitter>
                        <Splitter.Panel
                            defaultSize="50%"
                            min="10%"
                            max="90%"
                            collapsible
                        >
                            <SF_Editor_components
                                sf={sf}
                                setSf={setSf}
                                isSelect={isSelect}
                                setIsSelect={setIsSelect}
                            />
                        </Splitter.Panel>
                        <Splitter.Panel collapsible>
                            <Splitter orientation="vertical">
                                <Splitter.Panel
                                    defaultSize="70%"
                                    min="10%"
                                    max="90%"
                                    collapsible
                                >
                                    <SF_View_componetns
                                        code={sf.code}
                                        isSelect={isSelect}
                                    />
                                </Splitter.Panel>
                                <Splitter.Panel collapsible>
                                    <SF_Ask_components
                                        isSelect={isSelect}
                                        setIsSelect={setIsSelect}
                                        sf={sf}
                                        setSf={setSf}
                                    />
                                </Splitter.Panel>
                            </Splitter>
                        </Splitter.Panel>
                    </Splitter>
                </div>
            </Template_Page>
        </>
    );
};

export default CodeSF;
