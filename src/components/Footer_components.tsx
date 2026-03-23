import type { FC } from "react";
import { Link } from "react-router-dom";
import { isCdn, cdn } from "../config/cdn";
const Footer_components: FC = () => {
    return (
        <div className="flex justify-center">
            <img
                    src={(isCdn?cdn:"/public/")+"logo.svg"}
                    alt="Logo"
                    className="h-4 w-12 object-contain"
                />
            <Link to={"/"}>
                <span className="text-black"> 智创未来</span>
            </Link>
            ©{new Date().getFullYear()}
        </div>
    );
};

export default Footer_components;
