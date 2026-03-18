import type { FC } from "react";
import type { DocInfo } from "../api/Doc_api";
import { theme } from "antd";

const DocInfoCard_components: FC<DocInfo> = ({ name, summary, img }) => {
    const pColor = theme.useToken().token.colorPrimaryBorder;

    return (
        <div
            className="bg-white rounded-xl  border overflow-hidden"
            style={{
                borderColor: pColor,
            }}
        >
            <div className="p-5 flex gap-4 h-full">
                <div className="shrink-0 w-24 h-24 sm:w-32 sm:h-32 relative">
                    <img
                        src={img}
                        alt={name}
                        className="w-full h-full object-cover rounded-lg "
                    />
                </div>

                <div className="flex-1 min-w-0 flex flex-col justify-start">
                    <h3
                        className="text-4xl font-bold  mb-3 truncate"
                        title={name}
                        style={{
                            color: pColor,
                        }}
                    >
                        {name}
                    </h3>

                    <p className="text-base text-gray-600 leading-relaxed line-clamp-3">
                        {summary}
                    </p>
                </div>
            </div>
        </div>
    );
};

export default DocInfoCard_components;
