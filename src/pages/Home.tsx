import { Layout } from "antd";
const { Content } = Layout;
import Hello_Sum_component from "../components/Hello_Sum_components.jsx";
import type { FC } from "react";
const Home: FC = () => {
    return (
        <Content className="flex flex-col justify-start  w-full min-h-screen p-4 md:p-0">
                <Hello_Sum_component AllCmd="Cfww --info" />
        </Content>
    );
};

export default Home;
