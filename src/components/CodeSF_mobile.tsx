import useIsMobile from "../hooks/useIsMobile";
import DesktopOnlyNotice from "./DesktopOnlyNotice";
import CodeSF_inner from "../pages/Code/CodeSF";
import Template_Page from "../pages/Template_Page";

// 移动端检测包装组件
const CodeSF = () => {
    const isMobile = useIsMobile();

    if (isMobile) {
        return (
            <Template_Page>
                <DesktopOnlyNotice
                    title="请使用电脑端访问"
                    description="前端开发页面需要在电脑端访问以获得最佳体验。"
                />
            </Template_Page>
        );
    }

    return <CodeSF_inner />;
};

export default CodeSF;
