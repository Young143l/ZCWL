import type { FC } from "react";
import { Link } from "react-router-dom";
const Footer_components:FC = () => {
    return (
        <div className="text-center">
            <Link to={"/"}>
                <span className="text-black">智创未来</span>
            </Link>
            ©{new Date().getFullYear()}
        </div>
    );
};

export default Footer_components;
