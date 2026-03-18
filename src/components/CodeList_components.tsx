import { Input, Skeleton, Empty, theme } from "antd";
import { useEffect, useState, type FC } from "react";
import { getCodeList, type CodeProject } from "../api/Code_api";
import CodeCard_components from "./CodeCard_components";
import useLogin from "../status/Login_status";
import { useNavigate } from "react-router-dom";
import { SearchOutlined } from "@ant-design/icons";


const CodeList_components: FC = () => {
    const [codeList, setCodeList] = useState<CodeProject[] | null>(null);
    const [nowCodeList, setNowCodeList] = useState<CodeProject[] | null>(null);
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const [waiting, setWaiting] = useState<boolean>(true);
    const { token, userId, isLogin } = useLogin();
    const nav = useNavigate();



    useEffect(() => {
        let time: number | null = null;
        if (!isLogin) {
            nav("/login");
            return;
        } else {
            getCodeList(token, userId).then((res) => {
                if (res.ok) {
                    setCodeList(
                        (
                            res as {
                                ok: boolean;
                                list: CodeProject[];
                            }
                        ).list,
                    );
                    setNowCodeList(
                        (
                            res as {
                                ok: boolean;
                                list: CodeProject[];
                            }
                        ).list,
                    );
                    time = setTimeout(() => {
                        setWaiting(false);
                    }, 300);
                }
            });
        }
        return () => {
            if (time != null) {
                clearTimeout(time);
            }
        };
    }, [token, userId, isLogin, nav]);

    const handleSearch = () => {
        let time: number | null = null;

        return (e: React.ChangeEvent<HTMLInputElement>) => {
            if (time != null) clearTimeout(time);
            time = setTimeout(() => {
                setNowCodeList(
                    codeList?.filter((i: CodeProject) =>
                        i.name.includes(e.target.value),
                    ) as CodeProject[],
                );
            }, 260);
        };
    };
    return (
        <div className="w-full p-2 flex flex-col gap-2.5">
            <div className="flex justify-between">
                <h1
                    className="text-xl border-l-4 pl-1 "
                    style={{
                        borderColor: pColor,
                    }}
                >
                    已创建项目列表
                </h1>
                <div className="max-w-64">
                    <Input
                        placeholder="Search..."
                        prefix={<SearchOutlined />}
                        onChange={handleSearch()}
                    />
                </div>
            </div>

            {waiting ? (
                <div className="w-full grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-4 gap-5 ">
                    <Skeleton.Node
                        active
                        style={{
                            height: 58,
                            width: "100%",
                            borderRadius: 12,
                        }}
                    />
                    <Skeleton.Node
                        active
                        style={{
                            height: 58,
                            width: "100%",
                            borderRadius: 12,
                        }}
                    />
                    <Skeleton.Node
                        active
                        style={{
                            height: 58,
                            width: "100%",
                            borderRadius: 12,
                        }}
                    />
                    <Skeleton.Node
                        active
                        style={{
                            height: 58,
                            width: "100%",
                            borderRadius: 12,
                        }}
                    />
                </div>
            ) : nowCodeList?.length ? (
                <div className="w-full grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-4 gap-5 ">
                    {nowCodeList?.map((i: CodeProject) => (
                        <CodeCard_components
                            type={i.type}
                            name={i.name}
                            id={i.id}
                            key={i.type + i.id}
                        />
                    ))}
                </div>
            ) : (
                <div className="mt-20">
                    <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
                </div>
            )}
        </div>
    );
};

export default CodeList_components;
