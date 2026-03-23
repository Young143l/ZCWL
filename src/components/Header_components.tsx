import { useState, type FC } from "react";
import { Menu, Drawer, Popover, Button, ConfigProvider } from "antd";
import type { MenuProps } from "antd";
import { Link, useLocation } from "react-router-dom";
import {
    HomeOutlined,
    BookOutlined,
    FolderOutlined,
    CodeOutlined,
    MenuOutlined,
} from "@ant-design/icons";
import useLogin from "../status/Login_status";
import UserAvatar_components from "./UserAvatar_components";
type MenuItem = Required<MenuProps>["items"][number];
import useAIChatDoc from "../status/AIChatDoc_status";
import { cdn, isCdn} from "../config/cdn";
const items: MenuItem[] = [
    {
        label: (
            <Link to={"/"} className=" text-[16px]">
                Home
            </Link>
        ),
        key: "home",
        icon: <HomeOutlined />,
    },
    {
        label: (
            <Link to={"/document"} className=" text-[16px]">
                Document
            </Link>
        ),
        key: "document",
        icon: <BookOutlined />,
    },
    {
        label: (
            <Link to={"/project"} className=" text-[16px]">
                Project
            </Link>
        ),
        key: "project",
        icon: <FolderOutlined />,
    },
    {
        label: (
            <Link to={"/code"} className=" text-[16px]">
                Code
            </Link>
        ),
        key: "code",
        icon: <CodeOutlined />,
    },
];

const Header_components: FC = () => {
    const [mobileMenuVisible, setMobileMenuVisible] = useState(false);
    const location = useLocation();
    const { isLogin, userName, clearLoginStatus } = useLogin();
    const { clear } = useAIChatDoc();
    // 计算当前激活的菜单项，而不是使用状态
    const getCurrentKey = (pathname: string): string => {
        if (pathname === "/") {
            return "home";
        } else if (pathname.startsWith("/document")) {
            return "document";
        } else if (pathname.startsWith("/project")) {
            return "project";
        } else if (pathname.startsWith("/code")) {
            return "code";
        }
        return "home";
    };

    // 直接从当前路径计算当前选中的key
    const current = getCurrentKey(location.pathname);
    const unLoginContent = (
        <div className="flex justify-center items-center">
            <Link to={"/login"}>
                <Button>登录</Button>
            </Link>
        </div>
    );

    const LoginContent = (
        <div className="flex justify-center items-center">
            <Button
                onClick={() => {
                    clearLoginStatus();
                    clear();
                }}
            >
                登出
            </Button>
        </div>
    );

    const toggleMobileMenu = () => {
        setMobileMenuVisible(!mobileMenuVisible);
    };

    // 关闭抽屉菜单
    const closeMobileMenu = () => {
        setMobileMenuVisible(false);
    };

    return (
        <ConfigProvider
            theme={{
                components: {
                    Menu: {
                        iconSize: 16,
                        itemBg: "#FFFFFF00",
                    },
                },
            }}
        >
            <div className="flex justify-between items-center h-full md:w-5/6 w-full pl-5 pr-5 md:pl-0 md:pr-0">
                {/* Logo */}
                <Link to={"/"}>
                    <img
                        src={(isCdn?cdn:"/public/")+"logo.svg"}
                        alt="Logo"
                        className="h-8 w-24 object-contain"
                    />
                </Link>
                {/* 导航栏 */}
                <div className="hidden md:block flex-1 ">
                    <Menu
                        onClick={() => {
                            closeMobileMenu();
                        }}
                        selectedKeys={[current]}
                        mode="horizontal"
                        items={items}
                        className="flex justify-center"
                    />
                </div>
                {/* 桌面端登陆状态 */}
                <div className="hidden md:block">
                    <Popover
                        title={
                            isLogin ? (
                                <div className="text-center">{userName}</div>
                            ) : (
                                <div className="text-center">未登录</div>
                            )
                        }
                        content={isLogin ? LoginContent : unLoginContent}
                    >
                        <div>
                            <UserAvatar_components size={"large"} />
                        </div>
                    </Popover>
                </div>
                {/* 手机端开抽屉关 */}
                <div
                    className="block md:hidden text-xl text-black"
                    onClick={toggleMobileMenu}
                >
                    <MenuOutlined />
                </div>
                {/* 抽屉 */}
                <Drawer
                    placement="right"
                    closable={true}
                    onClose={closeMobileMenu}
                    open={mobileMenuVisible}
                    mask={true}
                    size="280"
                >
                    <div className="flex flex-col justify-center items-center  m-2 gap-1">
                        <UserAvatar_components size={"large"} />
                        {isLogin ? (
                            <>
                                <p className="text-xl text-center text-black">
                                    {userName}
                                </p>
                                <Button
                                    onClick={() => {
                                        clearLoginStatus();
                                    }}
                                >
                                    登出
                                </Button>
                            </>
                        ) : (
                            <Link to={"/login"}>
                                <Button
                                    onClick={() => {
                                        clearLoginStatus();
                                    }}
                                >
                                    登录
                                </Button>
                            </Link>
                        )}
                    </div>
                    <Menu
                        onClick={() => {
                            closeMobileMenu();
                        }}
                        selectedKeys={[current]}
                        mode="inline"
                        items={items}
                        className="border-0 mt-2"
                    />
                </Drawer>
            </div>
        </ConfigProvider>
    );
};

export default Header_components;
