import Template_Page from "../Template_Page";
import Hello_Sum_components from "../../components/Hello_Sum_components";
import type { FC } from "react";

const ProjectList: FC = () => {
    return (
        <>
            <Hello_Sum_components AllCmd="Cfww --project" />
            <Template_Page>
                a
            </Template_Page>
        </>
    );
};

export default ProjectList;
