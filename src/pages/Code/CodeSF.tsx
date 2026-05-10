import Template_Page from "../Template_Page";
import { Spin, Splitter } from "antd";
import { useEffect, useState, type FC } from "react";
import SF_Editor_components from "../../components/SF_Editor_components";
import SF_View_componetns from "../../components/SF_View_componetns";
import SF_Ask_components from "../../components/SF_Ask_components";
import { useParams } from "react-router-dom";
import { getCodeSF } from "../../api/Code_api";
import { LoadingOutlined } from "@ant-design/icons";
import useLogin from "../../status/Login_status";

export interface code {
    html: string;
    css: string;
    javascript: string;
}

export interface SF {
    sfId: string;
    name: string;
    code: code;
    isDeployed?: boolean;
}

const CodeSF: FC = () => {
    const [sf, setSf] = useState<SF>({
        sfId: "",
        name: "",
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
    const { sf_id } = useParams();
    const { token } = useLogin();
    const [loading, setLoading] = useState<boolean>(true);
    const [reLoadKey, setReLoadKey] = useState<number>(0);

    useEffect(() => {
        getCodeSF(token, sf_id as string).then((res) => {
            if (res.ok) {
                setSf(res.sf as SF);
                setTimeout(() => {
                    setLoading(false);
                }, 500);
            }
        });
    }, [sf_id, token]);

    return (
        <>
            <Spin
                indicator={<LoadingOutlined spin />}
                spinning={loading}
                size="large"
                fullscreen
            />
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
                                setReLoadKey={setReLoadKey}
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
                                        reLoadKey={reLoadKey}
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
