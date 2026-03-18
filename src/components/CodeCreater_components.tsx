import { Select, Button, theme } from "antd";
import { useState, type FC } from "react";
import LoadingWindow_components from "./AILoadingWindow_components";
import { newCodeCP, newCodeSF, type CodeType, type CPType } from "../api/Code_api";
import useLogin from "../status/Login_status";
import { useNavigate } from "react-router-dom";
import { SendOutlined } from "@ant-design/icons";

const CodeCreater_components: FC = () => {
    const { token, userId, isLogin } = useLogin();
    const [message, setMessage] = useState<string>("");
    const [appType, setAppType] = useState<CodeType>("simple_frontend");
    const [appName, setAppName] = useState<string>("");
    const [loading, setLoading] = useState<boolean>(false);
    const [isOpen, setIsOpen] = useState<boolean>(false);
    const [success, setSuccess] = useState<boolean>(false);
    const nav = useNavigate();
    const pColor = theme.useToken().token.colorPrimaryBorder;
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
    const creatCodeCP = (type:CPType) => {
        newCodeCP( userId,type, message,appName, token).then((res) => {
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
                nav(`/code/sf/${(res as { ok: boolean; cpId: string }).cpId}`);
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
            case "console_project_py":{
                creatCodeCP("python")
                break;
            }
        }
    };
    return (
        <>
            <LoadingWindow_components
                isOpen={isOpen}
                loading={loading}
                success={success}
            />
            <div className="w-full p-2 flex flex-col gap-2.5">
                <h1
                    className="text-xl border-l-4 pl-1"
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
                                {
                                    value:"console_project_py",
                                    label:"Python控制台应用"
                                }
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
        </>
    );
};

export default CodeCreater_components;
