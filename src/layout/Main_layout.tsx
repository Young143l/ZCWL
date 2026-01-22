import { Outlet } from "react-router-dom";
import { type FC } from "react";
import { Layout, ConfigProvider } from "antd";
const { Header, Footer } = Layout;
import Header_components from "../components/Header_components";
import Footer_components from "../components/Footer_components";
import ToTop_components from "../components/ToTop_components";


const Main_layout:FC = () => {
    return (
        <ConfigProvider
            theme={{
                components: {
                    Layout: {
                        headerBg: "#ffffff",
                        footerBg: "#ffffff",
                        headerPadding: "0 0px",
                    },
                },
            }}
        >
            <Layout>
                <Header className="shadow-sm sticky top-0 z-1 flex justify-center">
                    <Header_components />
                </Header>

                <div className="w-full flex justify-center ite min-h-screen ">
                    <div className="md:w-4/5 w-full flex items-center justify-center mb-5 mt-2">
                    <Outlet />
                    </div>
                </div>
                <Footer>
                    <Footer_components />
                </Footer>
                <ToTop_components />
            </Layout>
        </ConfigProvider>
    );
};

export default Main_layout;
