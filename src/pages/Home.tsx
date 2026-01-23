import { Layout } from "antd";
const { Content } = Layout;
import Hello_Sum_component from "../components/Hello_Sum_components.jsx";
import type { FC } from "react";
const Home: FC = () => {
    return (
        <Content>
            <div className="flex flex-row justify-center min-h-screen">
                <Hello_Sum_component AllCmd="Cfww --info" />
            </div>
        </Content>
    );
};

export default Home;
