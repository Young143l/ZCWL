import { Card, Avatar } from "antd";
import type { FC } from "react";
import { Link } from "react-router-dom";
const { Meta } = Card;

interface DocInfo {
    DocId: string;
    DocName: string;
    DocImg: string;
    DocSum: string;
}

const DocCard_component: FC<DocInfo> = ({ DocId, DocName, DocImg, DocSum }) => {
    return (
        <Link to={`/document/${DocId}`}>
            <Card
                className="hover:shadow-lg hover:shadow-sky-300 transition-shadow duration-300 h-23"
                size="small"
            >
                <Meta
                    title={DocName}
                    description={
                        <p
                            className="text-[12px] line-clamp-2 min-h-10"
                        >
                            {DocSum}
                        </p>
                    }
                    avatar={
                        <div className="flex items-center h-full">
                            <Avatar size={64} shape="square" src={DocImg} />
                       </div>
                    }
                />
            </Card>
        </Link>
    );
};

export default DocCard_component;
