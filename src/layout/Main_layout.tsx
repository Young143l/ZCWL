import { Outlet } from "react-router-dom";
import { type FC } from "react";
import { Layout, ConfigProvider } from "antd";
const { Header, Footer } = Layout;
import Header_components from "../components/Header_components";
import Footer_components from "../components/Footer_components";
import ToTop_components from "../components/ToTop_components";

const Main_layout: FC = () => {
    return (
        <ConfigProvider
            theme={{
                components: {
                    Layout: {
                        headerBg: "#00000000",
                        footerBg: "#ffffff",
                        headerPadding: "0 0px",
                    },
                },
            }}
        >
            <Layout>
                {/* <img
                    src="http://img.young143.top/young143/b.jpg"
                    alt="Background"
                    className="fixed inset-0 w-full h-full object-fill z-0"
                /> */}
                <Header className="shadow-sm sticky top-0 z-2 flex justify-center backdrop-blur-sm">
                    <Header_components />
                </Header>

                <div className="w-full flex justify-center ite min-h-screen z-1">
                    <div className="md:w-5/6 w-full flex items-center justify-center mb-5 mt-2">
                        <Outlet />
                    </div>
                </div>
                <Footer className="z-1">
                    <Footer_components />
                </Footer>
                <ToTop_components />
            </Layout>
        </ConfigProvider>
    );
};

export default Main_layout;
