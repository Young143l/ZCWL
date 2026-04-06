import { Carousel, Modal } from "antd";
import { useEffect, type FC } from "react";
import useHelp from "../status/Help_status";

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
            <Carousel
                arrows
                infinite={false}
                className="w-full rounded-xl overflow-auto help-carousel help-carousel-dots"
            >
                <div className="w-full">
                    <img
                        src="https://img.young143.top/zcwl/help/1.png"
                        alt=""
                        className="w-full object-cover "
                    />
                </div>
                <div className="w-full">
                    <img
                        src="https://img.young143.top/zcwl/help/2.png"
                        alt=""
                        className="w-full object-cover "
                    />
                </div>
            </Carousel>
        </Modal>
    );
};

export default Help_components;
