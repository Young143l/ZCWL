import { useEffect, useState, type FC } from "react";
import { useParams } from "react-router-dom";
import { type DocDir, type DocInfo, getDoc } from "../../api/Doc_api";
import Chaper_componenents from "../../components/Chapter_components";
import DocInfoCard_components from "../../components/DocInfoCard_components";
import { Skeleton } from "antd";
import Template_Page from "../Template_Page";
import DocBreadcrumb_components from "../../components/DocBreadcrumb_components";
import useIsDark from "../../status/IsDark_status";

// 骨架屏组件
const DocDirectorySkeleton: FC = () => {
    const { isDark } = useIsDark();
    return (
        <div className="space-y-4">
            {/* 文档信息卡片骨架 */}
            <div
                className={`bg-${isDark ? "black" : "white"} rounded-xl border ${isDark?"border-gray-700":"border-gray-200"} overflow-hidden p-5`}
            >
                <div className="flex gap-4 h-full">
                    {/* 图片骨架 */}
                    <Skeleton.Image
                        active
                        className="shrink-0 w-24! h-24! sm:w-32! sm:h-32! rounded-lg!"
                    />
                    {/* 文字内容骨架 */}
                    <div className="flex-1 min-w-0 flex flex-col justify-start space-y-3">
                        <Skeleton.Input
                            active
                            size="large"
                            className="w-3/4!"
                        />
                        <Skeleton
                            paragraph={{ rows: 2 }}
                            active
                            className="mt-2!"
                        />
                    </div>
                </div>
            </div>
            {/* 章节列表骨架 */}
            <div
                className={`bg-${isDark ? "black" : "white"} rounded-xl border ${isDark?"border-gray-700":"border-gray-200"} overflow-hidden`}
            >
                {[1, 2, 3, 4, 5].map((i) => (
                    <div
                        key={i}
                        className={`px-6 py-4 flex items-center justify-between gap-4 border-b border-${isDark?"gray-700":"gray-200"} last:border-b-0`}
                    >
                        <div className="flex items-center gap-3 flex-1 min-w-0">
                            <Skeleton.Input
                                active
                                size="small"
                                className="w-12!"
                            />
                            <Skeleton.Input
                                active
                                size="small"
                                className="w-1/2!"
                            />
                        </div>
                        <Skeleton.Button
                            active
                            size="small"
                            className="w-20!"
                        />
                    </div>
                ))}
            </div>
        </div>
    );
};
const DocumentDirectory: FC = () => {
    const { d_id } = useParams();
    const [docDirectory, setDocDirectory] = useState<DocDir[]>([]);
    const [docInfo, setdocInfo] = useState<DocInfo | undefined>(undefined);

    const [load, setLoad] = useState<boolean>(true);
    useEffect(() => {
        getDoc(d_id as string).then((res) => {
            if (res.ok) {
                setDocDirectory(
                    (
                        res as {
                            ok: boolean;
                            docInfo: DocInfo;
                            docDir: DocDir[];
                        }
                    ).docDir,
                );
                setdocInfo(
                    (
                        res as {
                            ok: boolean;
                            docInfo: DocInfo;
                            docDir: DocDir[];
                        }
                    ).docInfo,
                );
                setTimeout(() => {
                    setLoad(false);
                }, 280);
            }
        });
    }, [d_id]);

    return (
        <>
            <DocBreadcrumb_components
                d_id={d_id as string}
                d_name={docInfo?.name as string}
            />
            <Template_Page>
                {load ? (
                    <DocDirectorySkeleton />
                ) : (
                    <>
                        {docInfo && (
                            <DocInfoCard_components
                                name={docInfo.name}
                                id={docInfo.id}
                                summary={docInfo.summary}
                                img={docInfo.img}
                            />
                        )}
                        {docDirectory.map((i) => (
                            <Chaper_componenents id={i.id} name={i.name} />
                        ))}
                    </>
                )}
            </Template_Page>
        </>
    );
};

export default DocumentDirectory;
