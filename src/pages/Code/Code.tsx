import Template_Page from "../Template_Page";
import Hello_Sum_components from "../../components/Hello_Sum_components";
import { useEffect, useState, type FC } from "react";
import { Button, theme, Select, Skeleton, Input, Empty } from "antd";
import { newCodeSF, getCodeList, type CodeProject } from "../../api/Code_api";
import useLogin from "../../status/Login_status";
import { SearchOutlined, SendOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import CodeCard_components from "../../components/CodeCard_components";
import { type CodeType } from "../../api/Code_api";
import LoadingWindow_components from "../../components/LoadingWindow_components";

const Code: FC = () => {
    const [codeList, setCodeList] = useState<CodeProject[] | null>(null);
    const [nowCodeList, setNowCodeList] = useState<CodeProject[] | null>(null);
    const { token, userId, isLogin } = useLogin();
    const [message, setMessage] = useState<string>("");
    const [appType, setAppType] = useState<CodeType>("simple_frontend");
    const [appName, setAppName] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);
    const [isOpen, setIsOpen] = useState<boolean>(false);
    const [success, setSuccess] = useState<boolean>(false);
    const [waiting, setWaiting] = useState<boolean>(true);
    const nav = useNavigate();
    const pColor = theme.useToken().token.colorPrimary;
    const creatCodeSF = () => {
        newCodeSF(message, userId, appName, token).then((res) => {
            if (!res.ok) {
                setLoading(false);
                setSuccess(false);
                setTimeout(() => {
                    setIsOpen(false);
                }, 1500);
                return;
            }
            setLoading(false);
            setSuccess(true);
            setTimeout(() => {
                nav(`/code/sf/${(res as { ok: boolean; sfId: string }).sfId}`);
            }, 300);
        });
    };
    const handleMake = () => {
        if (!isLogin) {
            nav("/login");
            return;
        }
        setIsOpen(true);
        setLoading(true);
        switch (appType) {
            case "simple_frontend": {
                creatCodeSF();
                break;
            }
        }
    };

    const handleSearch = () => {
        let time: number | null = null;

        return (e: React.ChangeEvent<HTMLInputElement>) => {
            if (time != null) clearTimeout(time);
            time = setTimeout(() => {
                setNowCodeList(
                    codeList?.filter((i: CodeProject) =>
                        i.name.includes(e.target.value),
                    ) as CodeProject[],
                );
            }, 260);
        };
    };

    useEffect(() => {
        let time: number | null = null;
        if (!isLogin) {
            nav("/login");
            return;
        } else {
            getCodeList(token, userId).then((res) => {
                if (res.ok) {
                    setCodeList(
                        (
                            res as {
                                ok: boolean;
                                list: CodeProject[];
                            }
                        ).list,
                    );
                    setNowCodeList(
                        (
                            res as {
                                ok: boolean;
                                list: CodeProject[];
                            }
                        ).list,
                    );
                    time = setTimeout(() => {
                        setWaiting(false);
                    }, 300);
                }
            });
        }
        return () => {
            if (time != null) {
                clearTimeout(time);
            }
        };
    }, [token, userId, isLogin, nav]);
    return (
        <>
            <LoadingWindow_components
                isOpen={isOpen}
                loading={loading}
                success={success}
            />
            <Hello_Sum_components AllCmd="Cfww --code" />
            <Template_Page>
                <div className="w-full p-2 flex flex-col gap-2.5">
                    <h1
                        className={`text-xl border-l-4 pl-1  `}
                        style={{
                            borderColor: pColor,
                        }}
                    >
                        项目应用创建
                    </h1>
                    <div
                        className="border-2  w-full rounded-xl p-2 "
                        style={{
                            borderColor: pColor,
                        }}
                    >
                        <input
                            placeholder="请输入应用名。"
                            className="focus:border-none border-none w-full focus:outline-none"
                            value={appName}
                            onChange={(e) => {
                                setAppName(e.target.value);
                            }}
                        />
                    </div>
                    <div
                        className="border-2 w-full rounded-xl flex-row items-center p-2 "
                        style={{
                            borderColor: pColor,
                        }}
                    >
                        <textarea
                            className="w-full focus:outline-none border-none resize-none grow min-h-16"
                            placeholder="说出您的需求创建应用！"
                            onChange={(e) => {
                                setMessage(e.target.value);
                            }}
                            value={message}
                        ></textarea>
                        <div className="flex items-center justify-between">
                            <Select
                                options={[
                                    {
                                        value: "simple_frontend",
                                        label: "html单页应用",
                                    },
                                ]}
                                className="w-[120]"
                                value={appType}
                                onChange={(e) => {
                                    setAppType(e);
                                }}
                            />
                            <Button
                                onClick={() => {
                                    handleMake();
                                }}
                                disabled={message === "" || appName === ""}
                            >
                                <SendOutlined />
                                发送
                            </Button>
                        </div>
                    </div>
                </div>

                <div className="w-full p-2 flex flex-col gap-2.5">
                    <div className="flex justify-between">
                        <h1
                            className="text-xl border-l-4 pl-1 "
                            style={{
                                borderColor: pColor,
                            }}
                        >
                            已创建项目列表
                        </h1>
                        <div className="max-w-64">
                            <Input
                                placeholder="Search..."
                                prefix={<SearchOutlined />}
                                onChange={handleSearch()}
                            />
                        </div>
                    </div>

                    {waiting ? (
                        <div className="w-full grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-4 gap-5 ">
                            <Skeleton.Node
                                active
                                style={{
                                    height: 58,
                                    width: "100%",
                                    borderRadius: 12,
                                }}
                            />
                            <Skeleton.Node
                                active
                                style={{
                                    height: 58,
                                    width: "100%",
                                    borderRadius: 12,
                                }}
                            />
                            <Skeleton.Node
                                active
                                style={{
                                    height: 58,
                                    width: "100%",
                                    borderRadius: 12,
                                }}
                            />
                            <Skeleton.Node
                                active
                                style={{
                                    height: 58,
                                    width: "100%",
                                    borderRadius: 12,
                                }}
                            />
                        </div>
                    ) : nowCodeList?.length ? (
                        <div className="w-full grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-4 gap-5 ">
                            {nowCodeList?.map((i: CodeProject) => (
                                <CodeCard_components
                                    type={i.type}
                                    name={i.name}
                                    id={i.id}
                                    key={i.type + i.id}
                                />
                            ))}
                        </div>
                    ) : (
                        <div className="mt-20">
                            <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
                        </div>
                    )}
                </div>
            </Template_Page>
        </>
    );
};

export default Code;
