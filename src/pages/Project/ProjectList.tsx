import Template_Page from "../Template_Page";
import Hello_Sum_components from "../../components/Hello_Sum_components";
import type { FC } from "react";
import { SearchOutlined } from "@ant-design/icons";
import { Input, theme } from "antd";

const ProjectList: FC = () => {
    const pColor = theme.useToken().token.colorPrimaryBorder;

    return (
        <>
            <Hello_Sum_components AllCmd="Cfww --project" />
            <Template_Page>
                <div className="w-full p-2 flex flex-col gap-2.5">
                    <div className="flex justify-between">
                        <h1
                            className="text-xl border-l-4 pl-1 "
                            style={{
                                borderColor: pColor,
                            }}
                        >
                            已拉取仓库列表
                        </h1>
                        <div className="max-w-64">
                            <Input
                                placeholder="Search..."
                                prefix={<SearchOutlined />}
                                // onChange={handleSearch()}
                            />
                        </div>
                    </div>
                    
                </div>
            </Template_Page>
        </>
    );
};

export default ProjectList;
