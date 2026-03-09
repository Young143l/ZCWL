import { Avatar, theme } from "antd";
import type { FC } from "react";
import { Link } from "react-router-dom";

interface DocInfo {
    DocId: string;
    DocName: string;
    DocImg: string;
    DocSum: string;
}

const DocCard_component: FC<DocInfo> = ({ DocId, DocName, DocImg, DocSum }) => {
    const pColor = theme.useToken().token.colorPrimaryBorder;

    return (
        <Link to={`/document/${DocId}`}>
            <div
                className= "rounded-xl bg-gray-50 hover:bg-gray-200 border-2 p-2 flex items-center gap-2.5 "
                style={{
                    borderColor: pColor,
                }}
            >
                <div className="flex h-full">
                    <Avatar size={56} shape="square" src={DocImg} />
                </div>
                <div className="flex flex-col text-black">
                    <div className="font-bold text-xl h-full overflow-hidden text-ellipsis whitespace-nowrap">
                        {DocName}
                    </div>
                    <div className="text-[12px] line-clamp-2 min-h-10">
                        {DocSum}
                    </div>
                </div>
            </div>
        </Link>
    );
};

export default DocCard_component;
