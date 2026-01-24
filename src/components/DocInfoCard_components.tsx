import type { FC } from "react";
import type { DocInfo } from "../api/Doc_api";

const DocInfoCard_components: FC<DocInfo> = ({ name, summary, img }) => {
    return (
        <div className="bg-white rounded-xl  overflow-hidden  transition-shadow duration-300">
            <div className="p-5 flex gap-2.5 flex-wrap">
                <div className="w-1/3">
                    <img
                        src={img}
                        alt={name}
                        className="w-50 h-50 object-cover rounded-lg"
                    />
                </div>
                <div className="w-1/2">
                    <h3 className="text-lg font-semibold text-gray-800 mb-2 truncate">
                        {name}
                    </h3>

                    <p className="text-gray-600 text-sm line-clamp-3">
                        {summary}
                    </p>
                </div>
            </div>
        </div>
    );
};

export default DocInfoCard_components;
