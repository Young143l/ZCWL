import { Layout } from "antd";
import type { FC, ReactNode } from "react";
import useIsDark from "../status/IsDark_status";

const { Content, Sider } = Layout;
interface TemplateNoSiderProps {
    children: ReactNode;
    sider?: ReactNode;
}

const Template_Page: FC<TemplateNoSiderProps> = ({ children, sider }) => {
    const { isDark } = useIsDark();

    return (
        <div className={`w-full ${isDark?"bg-black":"bg-white"} rounded-xl  p-2`}>
            <Layout>
                {sider ? (
                    <Sider className="hidden md:block">{sider}</Sider>
                ) : (
                    <></>
                )}
                <Content className={isDark? "bg-black!" : " bg-white!"}>
                    <div className="min-h-[calc(100vh-95px)]">{children}</div>
                </Content>
            </Layout>
        </div>
    );
};

export default Template_Page;
