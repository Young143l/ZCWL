import useIsMobile from "../hooks/useIsMobile";
import DesktopOnlyNotice from "./DesktopOnlyNotice";
import CodeCP_inner from "../pages/Code/CodeCP";
import Template_Page from "../pages/Template_Page";

// 移动端检测包装组件
const CodeCP = () => {
    const isMobile = useIsMobile();

    if (isMobile) {
        return (
            <Template_Page>
                <DesktopOnlyNotice
                    title="请使用电脑端访问"
                    description="代码助手页面需要在电脑端访问以获得最佳体验。"
                />
            </Template_Page>
        );
    }

    return <CodeCP_inner />;
};

export default CodeCP;
