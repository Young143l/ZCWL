import { FloatButton, Modal } from "antd";
import {
    ArrowUpOutlined,
    SyncOutlined,
    HomeOutlined,
} from "@ant-design/icons";
import Icon from "@ant-design/icons";
import { useState, type FC } from "react";
import { useNavigate,useLocation } from "react-router-dom";
import AIChatDoc_components from "./AIChatDoc_components.tsx";
import QwenIcon from '../../public/qwen.svg?react';

const FloatTools_components: FC = () => {
    const location = useLocation();
    const [isAIChatOpen, setIsAIChatOpen] = useState<boolean>(false);
    const nav = useNavigate();
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
                    icon={<Icon component={QwenIcon}/>}
                    onClick={() => setIsAIChatOpen(true)}
                />
                {location.pathname==="/"?<></> :<FloatButton icon={<HomeOutlined />} onClick={() => nav("/")} />}
                
                <FloatButton.BackTop icon={<ArrowUpOutlined />} />
                <FloatButton
                    icon={<SyncOutlined />}
                    onClick={() => {
                        window.location.reload();
                    }}
                />
            </FloatButton.Group>
        </>
    );
};

export default FloatTools_components;
