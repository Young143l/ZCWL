import type { FC } from "react";
import { Modal, Spin, theme } from "antd";
import { CheckCircleFilled, ExclamationCircleFilled, LoadingOutlined } from "@ant-design/icons";

interface LoadingProps {
    isOpen: boolean;
    loading: boolean;
    success: boolean;
}

const LoadingWindow_components: FC<LoadingProps> = ({
    isOpen,
    loading,
    success,
}) => {

    const mainColor = theme.useToken().token.colorPrimary
    return (
        <Modal footer={null} open={isOpen} closable={false}>
            <div className="flex gap-2 items-center">
                {loading ? (
                    <>
                        <Spin
                            indicator={<LoadingOutlined spin />}
                            size="large"
                        ></Spin>
                        <div
                            className="font-bold text-xl h-full"
                            style={{
                                color: mainColor,
                            }}
                        >
                            正在等待AI返回结果...
                        </div>
                    </>
                ) : success ? (
                    <>
                        <CheckCircleFilled
                            style={{
                                fontSize: 32,
                                color: mainColor,
                            }}
                        />
                        <div
                            className="font-bold text-xl h-full"
                            style={{
                                color: mainColor,
                            }}
                        >
                            加载成功
                        </div>
                    </>
                ) : (
                    <>
                        <ExclamationCircleFilled 
                            style={{
                                fontSize: 32,
                                color: "red",
                            }}
                        />
                        <div
                            className="font-bold text-xl h-full"
                            style={{
                                color: "red",
                            }}
                        >
                            加载失败
                        </div>
                    </>
                )}
            </div>
        </Modal>
    );
};
export default LoadingWindow_components;
