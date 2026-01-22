import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button, Form, Input, App } from "antd";
import { UserOutlined, LockOutlined } from "@ant-design/icons";
import login from "../api/Login_api.ts";
import useLoginState from "../status/Login_status.tsx";

const LoginPage = () => {
    const [userId, setUserId] = useState("");
    const [password, setPassword] = useState("");
    const { setLoginStatus } = useLoginState();
    const { modal } = App.useApp();
    const navigate = useNavigate();
    const finish = async () => {
        const res = await login(userId, password, setLoginStatus);
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

    return (
        <div className="flex items-center justify-center h-screen bg-gray-100">
            <div className="bg-white p-8 rounded-2xl shadow-md w-full max-w-xs">
                <h2 className="text-2xl font-bold text-center text-gray-800 mb-6">
                    登录智创未来
                </h2>

                <Form name="login" onFinish={finish}>
                    <Form.Item
                        name="userId"
                        rules={[
                            {
                                required: true,
                                message: "请输入账号!",
                            }
                        ]}
                    >
                        <Input
                            prefix={<UserOutlined />}
                            placeholder="UserId"
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
                            }
                        ]}
                    >
                        <Input
                            prefix={<LockOutlined />}
                            type="password"
                            placeholder="Password"
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

                <div className="flex justify-between">
                    <Link to={"/"}><div className="text-gray-500" >{"< 返回主页"}</div></Link>
                    <Link to={"/signin"}><div className="text-gray-500" >{"注册 >"}</div></Link>
                </div>
            </div>
        </div>
    );
};

const Login= () => {
    return (
        <App>
            <LoginPage />
        </App>
    );
};

export default Login;
