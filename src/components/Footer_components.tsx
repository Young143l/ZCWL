import type { FC } from "react";
import { Link } from "react-router-dom";
import { isCdn, cdn } from "../config/cdn";
import useIsDark from "../status/IsDark_status";
const Footer_components: FC = () => {
    const { isDark } = useIsDark();
    return (
        <div className="flex justify-center items-center">
            <img
                src={
                    (isCdn ? cdn : "/public/") +
                    (isDark ? "logodark.svg" : "logo.svg")
                }
                alt="Logo"
                className="h-4 w-12 object-contain"
            />
            <Link to={"/"}>
                <span className={isDark ? "text-white" : "text-black"}>
                    {" "}
                    智码启行
                </span>
            </Link>
            ©{new Date().getFullYear()}
        </div>
    );
};

export default Footer_components;
