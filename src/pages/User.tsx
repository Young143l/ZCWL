import { useEffect, useState, type FC } from "react";
import { updateUserInfo, updatePassword } from "../api/User_api";
import useLogin from "../status/Login_status";
import Template_Page from "./Template_Page";
import UserAvatar_components from "../components/UserAvatar_components";
import { Button, Divider, Form, Input, message, theme, Tabs } from "antd";
import { useNavigate } from "react-router-dom";
import { LockOutlined, MailOutlined, UserOutlined } from "@ant-design/icons";
import { Md5 } from "ts-md5";
import LearningStats_components from "../components/LearningStats_components";
import RecentLearning_components from "../components/RecentLearning_components";
import LearningHeatmap_components from "../components/LearningHeatmap_components";

const User: FC = () => {
    const {
        avatar,
        token,
        email,
        userName,
        userId,
        isLogin,
        setLoginStatus,
        clearLoginStatus,
    } = useLogin();
    const [newEMail, setNewEMail] = useState<string>("");
    const [newName, setNewName] = useState<string>("");
    const nav = useNavigate();
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const mpColor = theme.useToken().token.colorPrimary;
    const [messageApi, contextHolder] = message.useMessage();
    const [oldPassword, setOldPassword] = useState<string>("");
    const [newPassword, setNewPassword] = useState<string>("");
    const [confirmPassword, setConfirmPassword] = useState<string>("");
    useEffect(() => {
        if (!isLogin) {
            nav("/login");
        }
    }, [nav, isLogin, userName, avatar, email]);

    const [infoForm] = Form.useForm();
    const handleChangeInfo = async () => {
        if (newEMail === "" && newName === "") {
            messageApi.warning({
                content: "请输入修改信息。",
            });
        }
        messageApi.loading({
            duration: 0,
            content: "修改中",
        });
        const res = await updateUserInfo(
            userId,
            token,
            newName !== "" ? newName : userName,
            newEMail !== "" ? newEMail : email,
        );
        messageApi.destroy();
        if (res.success) {
            messageApi.success({
                content: "修改成功",
            });
            setNewName("");
            setNewEMail("");
            setLoginStatus(
                res.data?.name as string,
                userId,
                token,
                `https://cravatar.cn/avatar/${Md5.hashStr(res.data?.email as string)}`,
                res.data?.email as string,
            );
            infoForm.resetFields();
        } else {
            messageApi.error({
                content: "修改失败",
            });
        }
    };
    const handleChangePassword = async () => {
        if (newPassword === "" && oldPassword === "") {
            messageApi.warning({
                content: "请输入新旧密码。",
            });
        }
        messageApi.loading({
            duration: 0,
            content: "修改中",
        });
        const res = await updatePassword(
            userId,
            token,
            oldPassword,
            newPassword,
        );
        messageApi.destroy();
        if (res.success) {
            messageApi.success({
                content: "修改成功,请重新登录。",
            });

            setTimeout(() => {
                clearLoginStatus();
            }, 1000);
        } else {
            messageApi.error({
                content: "修改失败",
            });
        }
    };

    const items = [
        {
            key: "info",
            label: "个人信息",
            children: (
                <>
                    <div className="flex flex-col gap-2">
                        <h1
                            className={`text-xl border-l-4 pl-1 `}
                            style={{
                                borderColor: mpColor,
                            }}
                        >
                            个人信息
                        </h1>
                        <div
                            className="h-36 p-2 w-full rounded-xl border-2 flex justify-between items-center"
                            style={{
                                borderColor: pColor,
                            }}
                        >
                            <div className="flex flex-col items-center gap-0.5">
                                <UserAvatar_components size={106} />
                                <a
                                    href="https://cravatar.com"
                                    target="_blank"
                                    className="font-medium! text-gray-500! text-[0.75rem]!"
                                >
                                    ⓘ头像服务:Cravatar
                                </a>
                            </div>
                            <div className="mr-3 flex flex-col items-end">
                                <span
                                    className="font-extrabold text-3xl "
                                    style={{ color: mpColor }}
                                >
                                    {userName}
                                </span>
                                <div className="flex flex-col items-end text-gray-500">
                                    <div>
                                        <span className="font-bold">ID：</span>
                                        <span className="font-medium">
                                            {userId}
                                        </span>
                                    </div>
                                    <div>
                                        <span className="font-bold">eMail：</span>
                                        <span className="font-medium">{email}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <Divider size="large" />

                    <div>
                        <h1
                            className={`text-xl border-l-4 pl-1 `}
                            style={{
                                borderColor: mpColor,
                            }}
                        >
                            信息修改
                        </h1>
                        <div className="p-3">
                            <Form
                                name="changeInfo"
                                layout="inline"
                                onFinish={() => {
                                    handleChangeInfo();
                                }}
                                form={infoForm}
                            >
                                <Form.Item
                                    label="邮箱："
                                    name="eMail"
                                    rules={[
                                        {
                                            max: 20,
                                            message: "邮箱长度不能超过20个字符!",
                                        },
                                        {
                                            pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                                            message: "请输入正确的邮箱格式!",
                                        },
                                    ]}
                                >
                                    <Input
                                        placeholder={"留空则不修改邮箱"}
                                        maxLength={20}
                                        prefix={<MailOutlined />}
                                        value={newEMail}
                                        onChange={(e) => {
                                            setNewEMail(e.target.value);
                                        }}
                                    ></Input>
                                </Form.Item>
                                <Form.Item
                                    label="昵称："
                                    name="Name"
                                    rules={[
                                        {
                                            min: 6,
                                            max: 12,
                                            message: "昵称长度必须在6-12之间!",
                                        },
                                    ]}
                                >
                                    <Input
                                        placeholder={"留空则不修改昵称"}
                                        prefix={<UserOutlined />}
                                        value={newName}
                                        onChange={(e) => {
                                            setNewName(e.target.value);
                                        }}
                                    ></Input>
                                </Form.Item>
                                <Form.Item shouldUpdate>
                                    {() => (
                                        <Button
                                            type="primary"
                                            disabled={
                                                newEMail === "" && newName === ""
                                            }
                                            htmlType="submit"
                                        >
                                            修改
                                        </Button>
                                    )}
                                </Form.Item>
                            </Form>
                        </div>
                    </div>
                    <Divider size="large" />
                    <div>
                        <h1
                            className={`text-xl border-l-4 pl-1 `}
                            style={{
                                borderColor: mpColor,
                            }}
                        >
                            密码修改
                        </h1>
                        <div className="p-3">
                            <Form
                                name="changeInfo"
                                layout="inline"
                                onFinish={() => {
                                    handleChangePassword();
                                }}
                            >
                                <Form.Item
                                    label="旧密码："
                                    name="oldPassword"
                                    rules={[
                                        {
                                            required: true,

                                            message: "请输入旧密码！",
                                        },
                                    ]}
                                >
                                    <Input
                                        placeholder="此处输入旧密码。"
                                        prefix={<LockOutlined />}
                                        type="password"
                                        value={oldPassword}
                                        onChange={(e) => {
                                            setOldPassword(e.target.value);
                                        }}
                                    ></Input>
                                </Form.Item>
                                <Form.Item
                                    label="新密码："
                                    name="newPassword"
                                    rules={[
                                        {
                                            required: true,

                                            message: "请输入新密码！",
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
                                        placeholder="此处输入新密码。"
                                        prefix={<LockOutlined />}
                                        type="password"
                                        value={newPassword}
                                        onChange={(e) => {
                                            setNewPassword(e.target.value);
                                        }}
                                    ></Input>
                                </Form.Item>
                                <Form.Item
                                    label="确认新密码："
                                    name="confirmPassword"
                                    rules={[
                                        {
                                            required: true,
                                            message: "请输入新密码！",
                                        },
                                        {
                                            validator: (_, value) =>
                                                value === newPassword
                                                    ? Promise.resolve()
                                                    : Promise.reject(
                                                          new Error(
                                                              "两次输入的密码不一致！",
                                                          ),
                                                      ),
                                        },
                                    ]}
                                >
                                    <Input
                                        placeholder="此处输入新密码。"
                                        prefix={<LockOutlined />}
                                        type="password"
                                        value={confirmPassword}
                                        onChange={(e) => {
                                            setConfirmPassword(e.target.value);
                                        }}
                                    ></Input>
                                </Form.Item>
                                <Form.Item shouldUpdate>
                                    {() => (
                                        <Button
                                            type="primary"
                                            disabled={
                                                !(
                                                    newPassword !== "" &&
                                                    oldPassword !== "" &&
                                                    newPassword === confirmPassword
                                                )
                                            }
                                            htmlType="submit"
                                        >
                                            修改
                                        </Button>
                                    )}
                                </Form.Item>
                            </Form>
                        </div>
                    </div>
                </>
            ),
        },
        {
            key: "learning",
            label: "学习记录",
            children: (
                <div className="flex flex-col gap-4">
                    <LearningStats_components />
                    <LearningHeatmap_components />
                    <RecentLearning_components />
                </div>
            ),
        },
    ];

    return (
        <>
            {contextHolder}
            <Template_Page>
                <Tabs defaultActiveKey="info" items={items} />
            </Template_Page>
        </>
    );
};

export default User;
