import { Layout, Divider } from "antd";
import type { FC, ReactNode } from "react";

const { Content, Sider } = Layout;
interface TemplateNoSiderProps {
    children: ReactNode;
    sider?: ReactNode;
}

const Template_Page: FC<TemplateNoSiderProps> = ({ children, sider }) => {
    return (
        <div className="w-full bg-white rounded-xl overflow-hidden p-2">
            <Layout>
                {sider ? (
                    <Sider className="hidden md:block">{sider}</Sider>
                ) : (
                    <></>
                )}
                <Content className="bg-white">
                    <div className="min-h-[calc(100vh-95px)]">{children}</div>
                </Content>
            </Layout>
        </div>
    );
};

export default Template_Page;
