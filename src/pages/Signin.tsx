import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button, Form, Input, App } from "antd";
import { UserOutlined, LockOutlined, MailOutlined } from "@ant-design/icons";
import {SigninUser} from "../api/User_api.ts";

const SigninPage = () => {
    const [userId, setUserId] = useState("");
    const [password, setPassword] = useState("");
    const [mail, setMail] = useState("");
    const { modal } = App.useApp();
    const navigate = useNavigate();
    const finish = async () => {
        const res = await SigninUser(userId,mail,password);
        if (res.success) {
            modal.success({
                afterClose: () => {
                    navigate("/login");
                },
                title: "注册成功",
                content: "前去登陆吧！",
            });
        } else {
            modal.error({
                title: "注册失败",
                content: "未知错误",
            });
        }
    };

    return (
        <div className="flex items-center justify-center h-screen bg-gray-100">
            <div
                className="z-0 fixed  inset-0 pointer-events-none"
                style={{
                    backgroundImage:
                        "linear-gradient(#e5e7eb 1px, transparent 1px),linear-gradient(90deg, #e5e7eb 1px, transparent 1px)",
                    backgroundSize: "20px 20px",
                }}
            ></div>
            <div className="z-1 bg-white p-8 rounded-2xl shadow-md w-full max-w-xs">
                <h2 className="text-2xl font-bold text-center text-gray-800 mb-6">
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
                        name="Mail"
                        rules={[
                            {
                                required: true,
                                message: "请输入邮箱!",
                            },
                        ]}
                    >
                        <Input
                            prefix={<MailOutlined />}
                            placeholder="Mail"
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
                    <Link to={"/"}>
                        <div className="text-gray-500">{"< 返回主页"}</div>
                    </Link>
                    <Link to={"/login"}>
                        <div className="text-gray-500">{"登录 >"}</div>
                    </Link>
                </div>
            </div>
        </div>
    );
};

const Signin = () => {
    return (
        <App>
            <SigninPage />
        </App>
    );
};

export default Signin;
