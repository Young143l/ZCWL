import Hello_Sum_component from "../components/Hello_Sum_components.jsx";
import type { FC } from "react";
import LearnMind_components from "../components/LearnMind_components.js";
import useIsDark from "../status/IsDark_status.js";
import { theme } from "antd";
const Home: FC = () => {
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const { isDark } = useIsDark();
    return (
        <>
            <Hello_Sum_component AllCmd="Cfww --info" title="Home" />
            <div
                className={`${isDark ? "bg-black" : " bg-white"} rounded-xl p-4 flex flex-col  gap-2`}
            >
                <div
                    className="flex flex-col border-l-4 pl-2.5"
                    style={{ color: pColor }}
                >
                    <span
                        className={`text-lg font-bold ${isDark ? "text-white" : "text-gray-800"}`}
                    >
                        知识图谱
                    </span>
                    <span
                        className="text-xl font-medium tracking-wider opacity-60"
                        style={{ color: pColor }}
                    >
                        Knowledge Graph
                    </span>
                </div>
                <div className=" h-100">
                    <LearnMind_components />
                </div>
            </div>
        </>
    );
};

export default Home;
