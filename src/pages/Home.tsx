import Hello_Sum_component from "../components/Hello_Sum_components.jsx";
import type { FC } from "react";
import LearnMind_components from "../components/LearnMind_components.js";
// import { theme } from "antd";
const Home: FC = () => {
    // const pColor = theme.useToken().token.colorPrimaryBorder;

    return (
        <>
            <Hello_Sum_component AllCmd="Cfww --info" title="Home" />
            <div className=" bg-white rounded-xl p-2 flex flex-col gap-2">
                {/* <h1
                    className={`text-xl border-l-4 pl-1 `}
                    style={{
                        borderColor: pColor,
                    }}
                >
                    学习文档
                </h1> */}
                <div className=" h-100">
                    <LearnMind_components />
                </div>
            </div>
        </>
    );
};

export default Home;
