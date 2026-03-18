import Hello_Sum_component from "../components/Hello_Sum_components.jsx";
import type { FC } from "react";
const Home: FC = () => {
    return (
        <>
            <Hello_Sum_component AllCmd="Cfww --info" title="Home" />
        </>
    );
};

export default Home;
