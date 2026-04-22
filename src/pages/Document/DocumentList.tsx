import { Skeleton, Empty, theme, Input } from "antd";
import DocCard_component from "../../components/DocCard_components";
import Hello_Sum_component from "../../components/Hello_Sum_components";
import { useEffect, useState } from "react";
import { type DocInfo, getDocList } from "../../api/Doc_api";
import { SearchOutlined } from "@ant-design/icons";
import Template_Page from "../Template_Page";
import LearningPath_components from "../../components/LearningPath_components";
import useLogin from "../../status/Login_status";

const DocumentList = () => {
    const [docList, setDocList] = useState<DocInfo[]>([]);
    const [nowDocList, setNowDocList] = useState<DocInfo[]>([]);
    const [load, setLoad] = useState<boolean>(true);
    const mpColor = theme.useToken().token.colorPrimary;
    const { isLogin } = useLogin();

    useEffect(() => {
        const fetchData = async () => {
            // 获取文档列表
            const docRes = await getDocList();
            if (!docRes.ok) {
                setLoad(false);
                return;
            }

            const docs = (docRes as { ok: boolean; docList: DocInfo[] }).docList;
            setDocList(docs);
            setNowDocList(docs);
            setLoad(false);
        };

        fetchData();
    }, []);

    const handleSearch = () => {
        let time: number | null = null;

        return (e: React.ChangeEvent<HTMLInputElement>) => {
            if (time != null) clearTimeout(time);
            time = setTimeout(() => {
                setNowDocList(
                    docList?.filter(
                        (i) =>
                            i.name.includes(e.target.value) ||
                            i.summary.includes(e.target.value),
                    ) || [],
                );
            }, 260);
        };
    };

    return (
        <>
            <Hello_Sum_component AllCmd="Cfww --document" title="Document" />
            <Template_Page>
                <div className="flex justify-between p-2">
                    <h1
                        className={`text-xl border-l-4 pl-1 `}
                        style={{
                            borderColor: mpColor,
                        }}
                    >
                        学习文档
                    </h1>
                    <div className="max-w-64 ">
                        <Input
                            placeholder="Search..."
                            prefix={<SearchOutlined />}
                            onChange={handleSearch()}
                        />
                    </div>
                </div>
                {load ? (
                    <div className="w-full grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-4 gap-2 p-2">
                        <Skeleton.Node
                            active
                            style={{
                                height: 90,
                                width: "100%",
                                borderRadius: 12,
                            }}
                        />
                        <Skeleton.Node
                            active
                            style={{
                                height: 90,
                                width: "100%",
                                borderRadius: 12,
                            }}
                        />
                        <Skeleton.Node
                            active
                            style={{
                                height: 90,
                                width: "100%",
                                borderRadius: 12,
                            }}
                        />
                        <Skeleton.Node
                            active
                            style={{
                                height: 90,
                                width: "100%",
                                borderRadius: 12,
                            }}
                        />
                    </div>
                ) : nowDocList.length ? (
                    <div className="w-full grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-4 gap-2 p-2">
                        {nowDocList.map((i) => {
                            return (
                                <DocCard_component
                                    key={i.id}
                                    DocId={i.id}
                                    DocName={i.name}
                                    DocImg={i.img}
                                    DocSum={i.summary}
                                />
                            );
                        })}
                    </div>
                ) : (
                    <div className="mt-20">
                        <Empty image={Empty.PRESENTED_IMAGE_SIMPLE} />
                    </div>
                )}

                {/* 学习路径图 - 仅登录用户可见 */}
                {isLogin && (
                    <div className="mt-4">
                        <LearningPath_components />
                    </div>
                )}
            </Template_Page>
        </>
    );
};

export default DocumentList;
