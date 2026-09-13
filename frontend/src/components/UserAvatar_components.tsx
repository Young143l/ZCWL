import useLogin from "../status/Login_status";
import { Avatar, theme } from "antd";
import { UserOutlined } from "@ant-design/icons";
import type { FC } from "react";
import type { AvatarSize } from "antd/es/avatar/AvatarContext";

const UserAvatar_components:FC<{size:AvatarSize}> = ({size}) => {
    const { avatar, isLogin } = useLogin();
    const pColor = theme.useToken().token.colorPrimaryBorder;
    
    return (
        <Avatar
            size={size}
            icon={<UserOutlined />}
            src={isLogin && avatar != "" ? avatar : undefined}
            style={{
                boxShadow: `0 0 0 2px ${pColor}`
            }}
        />
    );
};

export default UserAvatar_components;
