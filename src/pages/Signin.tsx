import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button, Form, Input, App, message, ConfigProvider, theme } from "antd";
import { UserOutlined, LockOutlined, MailOutlined } from "@ant-design/icons";
import { SigninUser } from "../api/User_api.ts";
import { useLocation } from "react-router-dom";
import NProgress from "nprogress";
import "nprogress/nprogress.css";
import useIsDark from "../status/IsDark_status.ts";
const SigninPage = () => {
    const [userId, setUserId] = useState("");
    const [password, setPassword] = useState("");
    const [mail, setMail] = useState("");
    const { modal } = App.useApp();
    const navigate = useNavigate();
    const [messageApi, contextHolder] = message.useMessage();
    const { isDark } = useIsDark();

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

    const finish = async () => {
        messageApi.loading({
            content: "注册中",
            duration: 0,
        });
        const res = await SigninUser(userId, mail, password);
        if (res.success) {
            messageApi.destroy();
            modal.success({
                afterClose: () => {
                    navigate("/login");
                },
                title: "注册成功",
                content: "前去登陆吧！",
            });
        } else {
            messageApi.destroy();
            modal.error({
                title: "注册失败",
                content: "未知错误",
            });
        }
    };

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
                        注册智创未来
                    </h2>

                    <Form name="login" onFinish={finish}>
                        <Form.Item
                            name="userId"
                            rules={[
                                {
                                    required: true,
                                    message: "请输入账号!",
                                },
                                {
                                    min: 6,
                                    max: 12,
                                    message: "用户名长度必须在6-12个字符之间!",
                                },
                                {
                                    pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/,
                                    message:
                                        "用户 ID 只能包含字母、数字、下划线和中文字符!",
                                },
                            ]}
                        >
                            <Input
                                prefix={<UserOutlined />}
                                placeholder="UserId"
                                maxLength={12}
                                className={
                                    isDark ? "bg-gray-700 text-white" : ""
                                }
                                onChange={(e) => {
                                    setUserId(e.target.value);
                                }}
                            />
                        </Form.Item>
                        <Form.Item
                            name="Mail"
                            rules={[
                                {
                                    required: true,
                                    message: "请输入邮箱!",
                                },
                                {
                                    max: 20,
                                    message: "邮箱长度不能超过20个字符!",
                                },
                            ]}
                        >
                            <Input
                                prefix={<MailOutlined />}
                                placeholder="Mail"
                                maxLength={20}
                                className={
                                    isDark ? "bg-gray-700 text-white" : ""
                                }
                                onChange={(e) => {
                                    setMail(e.target.value);
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
                                {
                                    min: 8,
                                    max: 18,
                                    message: "密码长度必须在8-18个字符之间!",
                                },
                                {
                                    pattern:
                                        /^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$/,
                                    message:
                                        "密码必须包含大小写字母、数字和特殊字符，且长度至少为 8 位!",
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
                                注册
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
                        <Link to={"/login"}>
                            <div
                                className={
                                    isDark ? "text-gray-400" : "text-gray-500"
                                }
                            >
                                {"登录 >"}
                            </div>
                        </Link>
                    </div>
                </div>
            </div>
        </>
    );
};

const Signin = () => {
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
                <SigninPage />
            </App>
        </ConfigProvider>
    );
};

export default Signin;
