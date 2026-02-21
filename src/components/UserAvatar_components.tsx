import useLogin from "../status/Login_status";
import { Avatar } from "antd";
import { UserOutlined } from "@ant-design/icons";
import type { FC } from "react";
import type { AvatarSize } from "antd/es/avatar/AvatarContext";

const UserAvatar_components:FC<{size:AvatarSize}> = ({size}) => {
    const { avatar, isLogin } = useLogin();
    return (
        <Avatar
            size={size}
            icon={<UserOutlined />}
            src={isLogin && avatar != "" ? avatar : undefined}
        />
    );
};

export default UserAvatar_components;
