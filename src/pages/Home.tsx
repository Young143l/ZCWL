import Hello_Sum_component from "../components/Hello_Sum_components.jsx";
import type { FC } from "react";
import LearnMind_components from "../components/LearnMind_components.js";
import useIsDark from "../status/IsDark_status.js";
import { theme } from "antd";
import EvedayAsk_components from "../components/EvedayAsk_components.js";
import News_components from "../components/News_components.js";
const Home: FC = () => {
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const { isDark } = useIsDark();
    return (
        <>
            <Hello_Sum_component AllCmd="Cfww --info" title="Home" />
            {/* 知识图谱和每日问答 - 宽屏并列，窄屏上下 */}
            <div className="flex flex-col lg:flex-row gap-4">
                {/* 知识图谱 - 占据大部分空间 */}
                <div
                    className={`${isDark ? "bg-black" : "bg-white"} rounded-xl p-4 flex flex-col gap-2 flex-1 lg:w-2/3`}
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
                    <div className="h-full">
                        <LearnMind_components />
                    </div>
                </div>

                {/* 每日问答模块 */}
                <div
                    className={`${isDark ? "bg-[#1E1E1E]" : "bg-white"} rounded-xl p-4 flex flex-col gap-2 lg:w-1/3`}
                >
                    <EvedayAsk_components />
                </div>
            </div>

            {/* 编程资讯模块 - 自成一行 */}
            <div className="mt-4">
                <News_components />
            </div>
        </>
    );
};

export default Home;
