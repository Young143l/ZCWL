import { Card, Avatar } from "antd";
import { Link } from "react-router-dom";
const { Meta } = Card;

interface DocInfo{
    DocName:string,
    DocImg:string,
    DocSum:string
}

const DocCard_component = ({ DocName, DocImg, DocSum }:DocInfo) => {
    return (
        <Link to={"/"}>
            <Card
                className="hover:shadow-lg hover:shadow-sky-300 transition-shadow duration-300 max-w-80"
                size="small"
            >
                <Meta
                    title={DocName}
                    description={DocSum}
                    avatar={<Avatar size={64} shape="square" src={DocImg} />}
                />
            </Card>
        </Link>
    );
};

export default DocCard_component;
