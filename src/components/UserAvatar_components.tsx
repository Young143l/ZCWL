import useLogin from "../status/Login_status";
import { Avatar } from "antd";
import { UserOutlined } from "@ant-design/icons";

const UserAvatar_components = () => {
    const { avatar, isLogin } = useLogin();
    return (
        <Avatar
            size="large"
            icon={<UserOutlined />}
            src={isLogin && avatar != "" ? avatar : undefined}
        />
    );
};

export default UserAvatar_components;
