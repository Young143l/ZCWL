import { Carousel, Modal } from "antd";
import { useEffect, type FC } from "react";
import useHelp from "../status/Help_status";

const contentStyle: React.CSSProperties = {
    margin: 0,
    height: "160px",
    color: "#fff",
    lineHeight: "160px",
    textAlign: "center",
    background: "#364d79",
};

const Help_components: FC = () => {
    const { setFirst, setIsOpen, first, isOpen } = useHelp();
    useEffect(() => {
        setIsOpen(first);
    }, [first, setIsOpen]);
    return (
        <Modal
            title="帮助文档"
            open={isOpen}
            onCancel={() => {
                setFirst(false);
                setIsOpen(false);
            }}
            footer={null}
        >
            <Carousel arrows infinite={false}>
                <div>
                    <h3 style={contentStyle}>1</h3>
                </div>
                <div>
                    <h3 style={contentStyle}>2</h3>
                </div>
                <div>
                    <h3 style={contentStyle}>3</h3>
                </div>
                <div>
                    <h3 style={contentStyle}>4</h3>
                </div>
            </Carousel>
        </Modal>
    );
};

export default Help_components;
