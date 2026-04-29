import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button, Form, Input, App, message, ConfigProvider, theme } from "antd";
import { UserOutlined, LockOutlined } from "@ant-design/icons";
import { login } from "../api/User_api.ts";
import useLoginState from "../status/Login_status.ts";
import { useLocation } from "react-router-dom";
import NProgress from "nprogress";
import "nprogress/nprogress.css";
import useIsDark from "../status/IsDark_status.ts";
const LoginPage = () => {
    const [userId, setUserId] = useState("");
    const [password, setPassword] = useState("");
    const { setLoginStatus } = useLoginState();
    const { modal } = App.useApp();
    const navigate = useNavigate();
    const [messageApi, contextHolder] = message.useMessage();
    const { isDark } = useIsDark();
    const finish = async () => {
        messageApi.loading({
            content: "登陆中",
            duration: 0,
        });
        const res = await login(userId, password, setLoginStatus);
        messageApi.destroy();
        if (res.success) {
            modal.success({
                afterClose: () => {
                    navigate("/");
                },
                title: "登陆成功",
                content: `欢迎回来:${res.userName}`,
            });
        } else {
            modal.error({
                title: "登录失败",
                content: "用户名或密码错误",
            });
        }
    };

    const location = useLocation();

    useEffect(() => {
        NProgress.start();

        let isMounted = true;

        const completeLoading = () => {
            if (isMounted) {
                setTimeout(() => {
                    NProgress.done();
                }, 100);
            }
        };

        if (document.readyState === "complete") {
            completeLoading();
        } else {
            window.addEventListener("load", completeLoading);
        }

        return () => {
            isMounted = false;
            window.removeEventListener("load", completeLoading);
            NProgress.remove();
        };
    }, [location.pathname]);

    return (
        <>
            {contextHolder}
            <div
                className={`flex items-center justify-center h-screen ${isDark ? "bg-gray-900" : "bg-gray-100"}`}
            >
                <div
                    className="fixed inset-0 pointer-events-none"
                    style={{
                        backgroundImage: isDark
                            ? "linear-gradient(#374151 1px, transparent 1px), linear-gradient(90deg, #374151 1px, transparent 1px)"
                            : "linear-gradient(#e5e7eb 1px, transparent 1px), linear-gradient(90deg, #e5e7eb 1px, transparent 1px)",
                        backgroundSize: "20px 20px",
                    }}
                ></div>
                <div
                    className={`z-1 ${isDark ? "bg-black" : "bg-white"} p-8 rounded-2xl shadow-md w-full max-w-xs`}
                >
                    <h2
                        className={`text-2xl font-bold text-center ${isDark ? "text-gray-100" : "text-gray-800"} mb-6`}
                    >
                        登录智码启行
                    </h2>

                    <Form name="login" onFinish={finish}>
                        <Form.Item
                            name="userId"
                            rules={[
                                {
                                    required: true,
                                    message: "请输入账号!",
                                },
                            ]}
                        >
                            <Input
                                prefix={<UserOutlined />}
                                placeholder="UserId"
                                className={
                                    isDark ? "bg-gray-700 text-white" : ""
                                }
                                onChange={(e) => {
                                    setUserId(e.target.value);
                                }}
                            />
                        </Form.Item>
                        <Form.Item
                            name="password"
                            rules={[
                                {
                                    required: true,
                                    message: "请输入密码!",
                                },
                            ]}
                        >
                            <Input
                                prefix={<LockOutlined />}
                                type="password"
                                placeholder="Password"
                                className={
                                    isDark ? "bg-gray-700 text-white" : ""
                                }
                                onChange={(e) => {
                                    setPassword(e.target.value);
                                }}
                            />
                        </Form.Item>

                        <Form.Item>
                            <Button block type="primary" htmlType="submit">
                                登录
                            </Button>
                        </Form.Item>
                    </Form>

                    <div className="flex justify-between mt-4">
                        <Link to={"/"}>
                            <div
                                className={
                                    isDark ? "text-gray-400" : "text-gray-500"
                                }
                            >
                                {"< 返回主页"}
                            </div>
                        </Link>
                        <Link to={"/signin"}>
                            <div
                                className={
                                    isDark ? "text-gray-400" : "text-gray-500"
                                }
                            >
                                {"注册 >"}
                            </div>
                        </Link>
                    </div>
                </div>
            </div>
        </>
    );
};

const Login = () => {
    const { isDark } = useIsDark();

    return (
        <ConfigProvider
            theme={{
                algorithm: isDark
                    ? theme.darkAlgorithm
                    : theme.defaultAlgorithm,
            }}
        >
            <App>
                <LoginPage />
            </App>
        </ConfigProvider>
    );
};

export default Login;
