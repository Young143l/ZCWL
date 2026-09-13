import { Outlet } from "react-router-dom";
import { useEffect, type FC } from "react";
import { Layout, ConfigProvider, theme } from "antd";
const { Header, Footer } = Layout;
import Header_components from "../components/Header_components";
import Footer_components from "../components/Footer_components";
import FloatTools_components from "../components/FloatTools_components";
import { useLocation } from "react-router-dom";
import NProgress from "nprogress";
import "nprogress/nprogress.css";
import useIsDark from "../status/IsDark_status";
import "../css/github-markdown-dark.css";
import "../css/github-markdown-light.css";
const Main_layout: FC = () => {
    const { isDark } = useIsDark();

    const location = useLocation();

    useEffect(() => {
        document.documentElement.style.colorScheme = isDark ? "dark" : "light";
        if (isDark) {
            document.documentElement.classList.add('dark-mode');
            document.documentElement.style.backgroundColor = '#1e1e1e';
        } else {
            document.documentElement.classList.remove('dark-mode');
            document.documentElement.style.backgroundColor = '#f5f5f5';
        }
    }, [isDark]);

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
        <ConfigProvider
            theme={{
                algorithm: isDark
                    ? theme.darkAlgorithm
                    : theme.defaultAlgorithm,

                components: {
                    Layout: {
                        // headerBg: "#F8F8F875",
                        // footerBg: "#ffffff",
                        // siderBg: "#ffffff",
                        // bodyBg:"#ffffff",
                        headerBg: isDark ? "#14141475" : "#F8F8F875",
                        footerBg: isDark ? "#141414" : "#ffffff",
                        siderBg: isDark ? "#141414" : "#ffffff",
                        bodyBg: isDark ? "#1e1e1e" : "#F5F5F5",

                        headerPadding: "0 0px",
                    },
                },
                // token:{
                //     motion:false
                // }
            }}
        >
            <Layout className="min-h-screen">
                {/* <img
                    src="http://img.young143.top/young143/b.jpg"
                    alt="Background"
                    className="fixed inset-0 w-full h-full object-fill z-0"
                /> */}
                <div
                    className="fixed inset-0 pointer-events-none"
                    style={{
                        backgroundImage: isDark
                            ? "linear-gradient(#374151 1px, transparent 1px), linear-gradient(90deg, #374151 1px, transparent 1px)"
                            : "linear-gradient(#e5e7eb 1px, transparent 1px), linear-gradient(90deg, #e5e7eb 1px, transparent 1px)",
                        backgroundSize: "20px 20px",
                    }}
                ></div>
                <Header className={`border-b-2 ${isDark?"border-gray-600":"border-gray-300"} sticky top-0 z-2 flex justify-center backdrop-blur-sm`}>
                    <Header_components />
                </Header>
                <div className="w-full z-1">
                    <div className="md:w-5/6 w-full ml-auto mr-auto p-2 md:pt-2 md:pb-2">
                        <Outlet />
                    </div>
                </div>
                <Footer className="z-1">
                    <Footer_components />
                </Footer>
            </Layout>
            <FloatTools_components />
        </ConfigProvider>
    );
};

export default Main_layout;
