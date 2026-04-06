import { Divider, theme } from "antd";
import type { FC } from "react";

export const Command: FC<{ cmd: string }> = ({ cmd }) => {
    return (
        <>
            <div>{cmd}</div>
            <Divider size="small" />
        </>
    );
};

const Console_components: FC = () => {
    const pColor = theme.useToken().token.colorPrimaryBorder;

    return (
        <div className="w-full h-full p-1">
            <div
                className="w-full h-full rounded-2xl border-2 flex flex-col justify-between items-center overflow-hidden"
                style={{
                    borderColor: pColor,
                }}
            >
                <div className=" w-full h-full ">a</div>
                <div className="mb-2 w-full flex border-t border-gray-400">
                    <div className="pl-1.5 pr-1.5 font-bold text-sm">{">"}</div>
                    <input
                        type="text"
                        className="outline-0 w-full"
                        placeholder="由此进行输入"
                    />
                </div>
            </div>
        </div>
    );
};

export default Console_components;
