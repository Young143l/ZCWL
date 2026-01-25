import { Button } from "antd";
import { Link } from "react-router-dom";
import type { FC } from "react";
import Template_Page from "./Template_Page";
const NotFound: FC = () => {
    return (
        <Template_Page>
            <div className="flex flex-col items-center justify-center min-h-screen">
                <div className="text-center">
                    <h1 className="text-6xl font-bold text-gray-800 mb-4">
                        404
                    </h1>
                    <h2 className="text-2xl font-semibold text-gray-600 mb-6">
                        页面未找到
                    </h2>
                    <p className="text-gray-500 mb-8">
                        抱歉，您访问的页面似乎不存在
                    </p>
                    <Link to={"/"}>
                        <Button
                            type="primary"
                            size="large"
                            className="bg-blue-500 hover:bg-blue-600"
                        >
                            返回主页
                        </Button>
                    </Link>
                </div>
            </div>
        </Template_Page>
    );
};

export default NotFound;
