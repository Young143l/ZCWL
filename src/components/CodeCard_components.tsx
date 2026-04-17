import { theme } from "antd";
import type { FC } from "react";
import SF from "../../public/sf.svg?react";
import SF_dark from "../../public/sf_dark.svg?react";
import CP_PY from "../../public/cp_py.svg?react";
import CP_PY_dark from "../../public/cp_py_dark.svg?react";

import { useNavigate } from "react-router-dom";
import { type CodeProject } from "../api/Code_api";
import useIsDark from "../status/IsDark_status";

const CodeCard_components: FC<CodeProject> = ({ name, id, type }) => {
    const nav = useNavigate();
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const { isDark } = useIsDark();
    return (
        <>
            <div
                className={`rounded-xl bg-${isDark ? "bg-gray-600" : "gray-50"} ${isDark ? "hover:bg-gray-300" : "hover:bg-gray-200"} border-2   p-2 flex gap-2.5`}
                onClick={() => {
                    nav(
                        `/code/${type == "sf" ? "sf" : type == "cp" ? "cp" : ""}/${id}`,
                    );
                }}
                style={{
                    borderColor: pColor,
                }}
            >
                {type === "sf" ? (
                    isDark ? (
                        <SF_dark className="shrink-0 w-10 h-10" />
                    ) : (
                        <SF className="shrink-0 w-10 h-10" />
                    )
                ) : isDark ? (
                    <CP_PY_dark className="shrink-0 w-10 h-10" />
                ) : (
                    <CP_PY className="shrink-0 w-10 h-10" />
                )}
                <div className="font-bold text-2xl h-full overflow-hidden text-ellipsis whitespace-nowrap">
                    {name}
                </div>
            </div>
        </>
    );
};
export default CodeCard_components;
