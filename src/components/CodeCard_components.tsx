import { theme } from "antd";
import type { FC } from "react";
import SF from "../../public/sf.svg?react";
import { useNavigate } from "react-router-dom";
import { type CodeProject } from "../api/Code_api";

const CodeCard_components: FC<CodeProject> = ({ name, id, type }) => {
    const nav = useNavigate();
    const pColor = theme.useToken().token.colorPrimaryBorder;
    return (
        <>
            <div
                className= "rounded-xl bg-gray-50 hover:bg-gray-200 border-2   p-2 flex gap-2.5 "
                onClick={() => {
                    nav(`/code/${type == "sf" ? "sf" : (type=="cp"?"cp":"")}/${id}`);
                }}
                style={{
                    borderColor: pColor,
                }}
            >
                {type === "sf" ? (
                    <SF className="shrink-0 w-10 h-10" />
                ) : (
                    <SF className="shrink-0 w-10 h-10" />
                )}
                <div className="font-bold text-2xl h-full overflow-hidden text-ellipsis whitespace-nowrap">
                    {name}
                </div>
            </div>
        </>
    );
};
export default CodeCard_components;
