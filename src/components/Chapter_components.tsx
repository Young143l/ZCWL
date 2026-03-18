import { type FC } from "react";
import { Button, theme } from "antd";
import { ReadOutlined } from "@ant-design/icons";
import { useNavigate, useLocation } from "react-router-dom";
interface Chapter {
    id: string;
    name: string;
}

const Chaper_componenents: FC<Chapter> = ({ id, name }) => {
    const nav = useNavigate();
    const location = useLocation();
    const pColor = theme.useToken().token.colorPrimaryBorder;

    return (
        <div className="w-full">
            <div className="px-6 py-4 flex items-center justify-between gap-4 hover:bg-gray-50 rounded-lg transition-colors duration-200">
                <div className="flex items-center gap-3 flex-1 min-w-0">
                    <span
                        className="text-lg font-bold whitespace-nowrap"
                        style={{
                            color: pColor,
                        }}
                    >
                        #{id}
                    </span>
                    <span className="text-gray-700 text-base font-medium truncate">
                        {name}
                    </span>
                </div>
                <Button
                    type="default"
                    size="middle"
                    icon={<ReadOutlined />}
                    className=" shrink-0"
                    onClick={() => {
                        nav(`${location.pathname}/${id}`);
                    }}
                >
                    阅读
                </Button>
            </div>
        </div>
    );
};

export default Chaper_componenents;
