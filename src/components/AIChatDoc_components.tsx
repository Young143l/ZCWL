import { Input, Button } from "antd";
import { useState, type FC } from "react";
import { SendOutlined, ClearOutlined } from "@ant-design/icons";
const { TextArea } = Input;

const AIChatDoc_components: FC = () => {
    const [inputValue, setInputValue] = useState<string>("");

    return (
        <>
            <div className="mb-2 p-2  min-h-60 max-h-100 rounded-[5px] border border-gray-300"></div>
            <TextArea
                rows={3}
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
            />
            <div className="w-full pt-1 flex justify-end gap-1">
                <Button
                    onClick={() => {
                        setInputValue("");
                    }}
                >
                    <ClearOutlined />
                    清空
                </Button>
                <Button>
                    <SendOutlined />
                    发送
                </Button>
            </div>
        </>
    );
};
export default AIChatDoc_components;
