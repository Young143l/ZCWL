import { FloatButton, Modal } from "antd";
import {
    ArrowUpOutlined,
    SyncOutlined,
    // HomeOutlined,
    SunOutlined,
    MoonOutlined,
    QuestionCircleOutlined,
} from "@ant-design/icons";
import Icon from "@ant-design/icons";
import { type FC } from "react";
// import { useNavigate, useLocation } from "react-router-dom";
import AIChatDoc_components from "./AIChatDoc_components.tsx";
import QwenIcon from "../../public/qwen.svg?react";
import useAIChatDoc from "../status/AIChatDoc_status.ts";
import useIsDark from "../status/IsDark_status.ts";
import Help_components from "./Help_components.tsx";
import useHelp from "../status/Help_status.ts";

const FloatTools_components: FC = () => {
    // const location = useLocation();
    const { isAIChatOpen, setIsAIChatOpen } = useAIChatDoc();
    // const nav = useNavigate();
    const { isDark, toggleDark } = useIsDark();
    const { setIsOpen } = useHelp();
    return (
        <>
            <Modal
                title="AI Chat"
                open={isAIChatOpen}
                onCancel={() => setIsAIChatOpen(false)}
                footer={null}
            >
                <AIChatDoc_components />
            </Modal>
            <FloatButton.Group shape="square">
                <FloatButton
                    icon={isDark ? <MoonOutlined /> : <SunOutlined />}
                    onClick={() => {
                        toggleDark();
                    }}
                />
                <FloatButton
                    icon={<Icon component={QwenIcon} />}
                    onClick={() => setIsAIChatOpen(true)}
                />
                {/* {location.pathname === "/" ? (
                    <></>
                ) : (
                    <FloatButton
                        icon={<HomeOutlined />}
                        onClick={() => nav("/")}
                    />
                )} */}
                <FloatButton.BackTop icon={<ArrowUpOutlined />} />
                <FloatButton
                    icon={<SyncOutlined />}
                    onClick={() => {
                        window.location.reload();
                    }}
                />
                <Help_components />
                <FloatButton
                    icon={<QuestionCircleOutlined />}
                    onClick={() => {
                        setIsOpen(true);
                    }}
                />
            </FloatButton.Group>
        </>
    );
};

export default FloatTools_components;
