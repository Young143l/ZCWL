import { FloatButton } from "antd";
import { ArrowUpOutlined } from "@ant-design/icons";
import { useState, useEffect, type FC } from "react";

const ToTop_components:FC = () => {
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        const handleScroll = () => {
            if (window.pageYOffset > 100) {
                setVisible(true);
            } else {
                setVisible(false);
            }
        };

        window.addEventListener("scroll", handleScroll);

        return () => window.removeEventListener("scroll", handleScroll);
    }, []);

    return (
        <div className={`${visible ? "" : "hidden"}`}>
            <FloatButton
                icon={<ArrowUpOutlined />}
                onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}
            />
        </div>
    );
};

export default ToTop_components;
