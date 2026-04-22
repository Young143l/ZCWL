import { FloatButton, Modal } from "antd";
import {
    ArrowUpOutlined,
    // SyncOutlined,
    // HomeOutlined,
    QuestionCircleOutlined,
    ThunderboltOutlined,
} from "@ant-design/icons";
import Icon from "@ant-design/icons";
import { type FC, useState } from "react";
// import { useNavigate, useLocation } from "react-router-dom";
import AIChatDoc_components from "./AIChatDoc_components.tsx";
import Quiz_components from "./Quiz_components.tsx";
import QwenIcon from "../../public/qwen.svg?react";
import useAIChatDoc from "../status/AIChatDoc_status.ts";
import Help_components from "./Help_components.tsx";
import useHelp from "../status/Help_status.ts";
import { useParams } from "react-router-dom";

const FloatTools_components: FC = () => {
    // const location = useLocation();
    const { isAIChatOpen, setIsAIChatOpen } = useAIChatDoc();
    const { setIsOpen } = useHelp();
    const { d_id, c_id } = useParams();
    const [isQuizOpen, setIsQuizOpen] = useState(false);

    const docId = d_id ? parseInt(d_id) : 0;
    const chapterId = c_id ? parseInt(c_id) : null;
    // 检查是否在文档阅读页面（有 docId）
    const isDocPage = docId > 0;

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
                {/* <FloatButton
                    icon={<SyncOutlined />}
                    onClick={() => {
                        window.location.reload();
                    }}
                /> */}
                <Help_components />
                <FloatButton
                    icon={<QuestionCircleOutlined />}
                    onClick={() => {
                        setIsOpen(true);
                    }}
                />
                {/* AI智能测验按钮 - 仅在文档页面显示 */}
                {isDocPage && (
                    <FloatButton
                        icon={<ThunderboltOutlined />}
                        onClick={() => setIsQuizOpen(true)}
                        style={{
                            backgroundColor: '#faad14',
                            color: '#fff',
                        }}
                    />
                )}
            </FloatButton.Group>

            {/* 测验弹窗 */}
            <Modal
                open={isQuizOpen}
                onCancel={() => setIsQuizOpen(false)}
                footer={null}
                width={800}
                title={null}
                closable={false}
            >
                <Quiz_components
                    docId={docId}
                    chapterId={chapterId}
                    hasContent={isDocPage}
                />
            </Modal>
        </>
    );
};

export default FloatTools_components;