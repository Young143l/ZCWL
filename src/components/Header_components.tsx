import { useState, useEffect, type FC } from "react";
import { Menu, Avatar, Drawer, Popover, Button, ConfigProvider } from "antd";
import type { MenuProps } from "antd";
import { Link, useLocation } from "react-router-dom";
import {
    HomeOutlined,
    BookOutlined,
    FolderOutlined,
    CodeOutlined,
    UserOutlined,
    MenuOutlined,
} from "@ant-design/icons";
import useLogin from "../status/Login_status";

type MenuItem = Required<MenuProps>["items"][number];

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
    const [current, setCurrent] = useState("home");
    const [mobileMenuVisible, setMobileMenuVisible] = useState(false);
    const location = useLocation();
    const { isLogin, userName, clearLoginStatus } = useLogin();

    useEffect(() => {
        let newPath: string = "home";
        if (location.pathname === "/") {
            newPath = "home";
        } else if (location.pathname.startsWith("/document")) {
            newPath = "document";
        } else if (location.pathname.startsWith("/project")) {
            newPath = "project";
        } else if (location.pathname.startsWith("/code")) {
            newPath = "code";
        }
        
        // 只有当 newPath 与 current 不同时才更新状态
        if (current !== newPath) {
            setCurrent(newPath);
        }
    }, [location.pathname, current]);

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
                        src="/public/logo.svg"
                        alt="Logo"
                        className="h-8 w-24 object-contain"
                    />
                </Link>
                {/* 导航栏 */}
                <div className="hidden md:block flex-1 ">
                    <Menu
                        onClick={(e) => {
                            setCurrent(e.key);
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
                        <Avatar size="large" icon={<UserOutlined />} src={isLogin?"https://img.young143.top/young143/a.jpg":undefined} />
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
                        <Avatar icon={<UserOutlined />} size={"large"} />
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
                        onClick={(e) => {
                            setCurrent(e.key);
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
