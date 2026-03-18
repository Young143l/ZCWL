import Template_Page from "../Template_Page";
import Hello_Sum_components from "../../components/Hello_Sum_components";
import { type FC } from "react";
import CodeList_components from "../../components/CodeList_components";
import CodeCreater_components from "../../components/CodeCreater_components";

const Code: FC = () => {
    return (
        <>
            <Hello_Sum_components AllCmd="Cfww --code" title="Code" />
            <Template_Page>
                <CodeCreater_components />
                <CodeList_components />
            </Template_Page>
        </>
    );
};

export default Code;
