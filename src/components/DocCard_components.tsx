import { Avatar, theme, Progress } from "antd";
import type { FC } from "react";
import { Link } from "react-router-dom";
import useIsDark from "../status/IsDark_status";

interface DocInfo {
    DocId: string;
    DocName: string;
    DocImg: string;
    DocSum: string;
    progress?: number;
}

const DocCard_component: FC<DocInfo> = ({ DocId, DocName, DocImg, DocSum, progress }) => {
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const {isDark}=useIsDark()

    return (
        <Link to={`/document/${DocId}`}>
            <div
                className= {`rounded-xl bg-${isDark?"bg-gray-600":"gray-50"} ${isDark?"hover:bg-gray-300":"hover:bg-gray-200"}  border-2 p-2 flex items-center gap-2.5 `}
                style={{
                    borderColor: pColor,
                }}
            >
                <div className="flex h-full">
                    <Avatar size={56} shape="square" src={DocImg} />
                </div>
                <div className={`flex flex-col text-${isDark?"white":"black"} flex-1 min-w-0`}>
                    <div className="font-bold text-xl h-full overflow-hidden text-ellipsis whitespace-nowrap">
                        {DocName}
                    </div>
                    <div className="text-[12px] line-clamp-2 min-h-10">
                        {DocSum}
                    </div>
                    {progress !== undefined && progress > 0 && (
                        <div className="mt-1">
                            <Progress 
                                percent={progress} 
                                size="small" 
                                status={progress >= 100 ? "success" : "active"}
                                showInfo={true}
                                format={(percent) => `${percent?.toFixed(0)}%`}
                            />
                        </div>
                    )}
                </div>
            </div>
        </Link>
    );
};

export default DocCard_component;
