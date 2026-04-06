import {
    BellOutlined,
    CloseOutlined,
    LoadingOutlined,
    ReloadOutlined,
} from "@ant-design/icons";
import { Badge, Button, Divider, Empty, message, Popover } from "antd";
import {
    useCallback,
    useEffect,
    useState,
    type Dispatch,
    type FC,
    type SetStateAction,
} from "react";
import { getUserInfo } from "../api/User_api";
import useLogin from "../status/Login_status";
import { useNavigate } from "react-router-dom";
import useIsDark from "../status/IsDark_status";
import { getNotifications, readNotification } from "../api/Notification_api";

export interface NotificationType {
    nId: string;
    dId: string;
    cId: string;
    from: string;
    time: string;
    content: string;
}

const Notification: FC<{
    content: string;
    from: string;
    dId: string;
    cId: string;
    nId: string;
    setNotifications: Dispatch<SetStateAction<NotificationType[]>>;
}> = ({ content, from, dId, cId, nId, setNotifications }) => {
    const nav = useNavigate();
    const { token } = useLogin();
    const [uName, setUName] = useState<string>(from);
    const [reading, setReading] = useState<boolean>(false);
    const [messageApi, contextHolder] = message.useMessage();
    useEffect(() => {
        getUserInfo(from, token).then((res) => {
            if (res.success) {
                setUName(res.data?.name as string);
            }
        });
    }, [from, token, setUName]);
    const { isDark } = useIsDark();

    return (
        <>
            {contextHolder}
            <div
                className={`flex items-center justify-between  max-w-full gap-1`}
                onClick={() => {
                    nav("/document/" + dId + "/" + cId);
                }}
            >
                <div className="flex flex-col gap-1  max-w-[calc(100%-32px)]">
                    <div className="flex pr-8">
                        <span
                            className={`text-sm truncate font-bold ${!isDark ? "text-gray-700" : "text-gray-300"}`}
                        >
                            {uName}
                        </span>
                        <span
                            className={`text-sm shrink-0 font-bold ${!isDark ? "text-gray-700" : "text-gray-300"}`}
                        >
                            回复了你
                        </span>
                    </div>
                    <span
                        className={`text-sm  ${!isDark ? "text-gray-600" : "text-gray-400"} break-all line-clamp-2`}
                    >
                        {content}
                    </span>
                </div>
                <Button
                    icon={
                        reading ? <LoadingOutlined spin /> : <CloseOutlined />
                    }
                    type="text"
                    className="shrink-0"
                    onClick={(e) => {
                        e.stopPropagation();
                        setReading(true);
                        readNotification(nId, token).then((res) => {
                            if (res.success) {
                                setNotifications((prev) =>
                                    prev.filter((i) => {
                                        return i.nId != nId;
                                    }),
                                );
                            } else {
                                messageApi.error({
                                    content: "已读失败",
                                });
                            }
                        });
                    }}
                />
            </div>
            <Divider size="small" />
        </>
    );
};

const NotificationButton_components: FC = () => {
    const [notifications, setNotifications] = useState<NotificationType[]>([]);
    const { userId, token } = useLogin();
    const { isDark } = useIsDark();

    const getNf = useCallback(() => {
        getNotifications(userId, token).then((res) => {
            if (res.success) {
                setNotifications(res.data?.notifications as NotificationType[]);
            }
        });
    }, [token, userId]);
    useEffect(() => {
        getNf();
    }, [getNf]);

    return (
        <>
            <Popover
                content={
                    <div className="w-50">
                        {notifications.length === 0 ? (
                            <>
                                <Empty description={<div>无通知</div>} />
                            </>
                        ) : (
                            <>
                                <Divider size="small" />

                                {notifications.map((i) => (
                                    <Notification
                                        key={i.nId}
                                        content={i.content}
                                        from={i.from}
                                        cId={i.cId}
                                        dId={i.dId}
                                        nId={i.nId}
                                        setNotifications={setNotifications}
                                    />
                                ))}
                            </>
                        )}
                    </div>
                }
                title={
                    <div className="flex justify-between items-center">
                        <span>未读通知</span>
                        <Button
                            color="primary"
                            icon={<ReloadOutlined />}
                            variant="text"
                            onClick={() => {
                                getNf();
                            }}
                        />
                    </div>
                }
                trigger="click"
            >
                <Badge count={notifications.length}>
                    <Button
                        icon={<BellOutlined />}
                        className={
                            isDark ? "bg-[#14141475]!" : "bg-[#F8F8F875]!"
                        }
                    />
                </Badge>
            </Popover>
        </>
    );
};

export default NotificationButton_components;
