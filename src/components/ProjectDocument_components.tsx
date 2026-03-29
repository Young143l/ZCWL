import { Button, Modal } from "antd";
import { FileTextOutlined, LoadingOutlined } from "@ant-design/icons";
import { useEffect, useState, type FC } from "react";
import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
// import "github-markdown-css/github-markdown-light.css"
// import "github-markdown-css/github-markdown.css";

import { getProjectDoc } from "../api/Project_api";
import useIsDark from "../status/IsDark_status";
const ProjectDocument_components: FC<{ pName: string }> = ({ pName }) => {
    const [isOpen, setIsOpen] = useState<boolean>(false);
    const [leanDoc, setLeanDoc] = useState<string | null>(null);
    const {isDark}=useIsDark();
    useEffect(() => {
        getProjectDoc(pName).then((res) => {
            if (res.ok) {
                setLeanDoc(res.doc);
            }
        });
    }, [pName]);

    return (
        <>
            <Modal
                title="项目学习文档"
                open={isOpen}
                onCancel={() => {
                    setIsOpen(false);
                }}
                footer={null}
            >
                <div className={`${isDark?"markdown-body-dark":"markdown-body"} markdown-body h-120 overflow-auto`}>
                    <ReactMarkdown remarkPlugins={[remarkGfm]}>
                        {leanDoc}
                    </ReactMarkdown>
                </div>
            </Modal>
            <Button
                type="primary"
                icon={
                    leanDoc === null ? (
                        <LoadingOutlined spin />
                    ) : (
                        <FileTextOutlined />
                    )
                }
                variant="text"
                onClick={() => {
                    setIsOpen(true);
                }}
                disabled={leanDoc === null}
            />
        </>
    );
};

export default ProjectDocument_components;
