import { Result, Button } from "antd";
import { DesktopOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
import { type FC } from "react";

interface DesktopOnlyNoticeProps {
    title?: string;
    description?: string;
}

const DesktopOnlyNotice: FC<DesktopOnlyNoticeProps> = ({
    title = "请使用电脑端访问",
    description = "此页面需要更大的屏幕来展示代码和编辑器，请在电脑端访问以获得最佳体验。"
}) => {
    const nav = useNavigate();
    
    return (
        <div className="h-[calc(100vh-95px)] w-full flex items-center justify-center">
            <Result
                icon={<DesktopOutlined style={{ fontSize: 64, color: "#13c2c2" }} />}
                title={title}
                subTitle={description}
                extra={
                    <Button 
                        type="primary" 
                        size="large"
                        onClick={() => nav("/")}
                    >
                        返回首页
                    </Button>
                }
            />
        </div>
    );
};

export default DesktopOnlyNotice;
