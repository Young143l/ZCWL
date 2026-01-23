import { type FC } from "react";
import { Divider, Button } from "antd";
import { ReadOutlined } from "@ant-design/icons";
import { useNavigate ,useLocation} from "react-router-dom";
interface Chapter {
    id: string;
    name: string;
}

const Chaper_componenents: FC<Chapter> = ({id,name}) => {
    const nav = useNavigate();
    const location = useLocation()
    return (
        <div className="w-full">
            <Divider />
            <div className="px-6 py-4">
                <div className="text-gray-700 text-xl mb-2 font-semibold">
                    #第{id}章
                </div>
                <p className="text-gray-900 text-base">
                    {name}
                </p>
            </div>
            <div className="px-6 pt-4 pb-2 flex justify-end">
                <Button 
                    type="primary" 
                    shape="round" 
                    size="middle"
                    icon={<ReadOutlined />}
                    className="bg-blue-500 hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50"
                    onClick={()=>{nav(`${location.pathname}/${id}`)}}
                >
                    阅读
                </Button>
            </div>
        </div>
    );
};

export default Chaper_componenents;