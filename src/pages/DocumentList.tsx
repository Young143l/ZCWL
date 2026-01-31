import { Divider, Skeleton, Empty, Card } from "antd";
import DocCard_component from "../components/DocCard_components";
import Hello_Sum_component from "../components/Hello_Sum_components";
import { useEffect, useState } from "react";
import { type DocInfo, getDocList } from "../api/Doc_api";
import Template_Page from "./Template_Page";
const DocumentList = () => {
    const [docList, setDocList] = useState<DocInfo[]>([]);
    const [load, setLoad] = useState<boolean>(true);

    useEffect(() => {
        getDocList().then((res: { ok: boolean; docList: DocInfo[] }) => {
            if (res.ok) {
                setDocList(res.docList);
                setLoad(false);
            }
        });
    }, []);

    return (
        <>
            <Hello_Sum_component AllCmd="Cfww --document" />
            <Template_Page>
                <Divider titlePlacement="start">学习文档</Divider>
                {load ? (
                    <div className="w-full grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-4 gap-5 p-5">
                        <Card className="h-23" size="small">
                            <Card.Meta
                                avatar={
                                    <Skeleton.Avatar
                                        size={64}
                                        shape="square"
                                        active
                                    />
                                }
                                title={
                                    <Skeleton.Input
                                        style={{ width: 100 }}
                                        active
                                        size="small"
                                    />
                                }
                                description={
                                    <div className="pt-3">
                                        <Skeleton
                                            paragraph={{ rows: 0 }}
                                            active
                                        />
                                    </div>
                                }
                            />
                        </Card>
                        <Card className="h-23" size="small">
                            <Card.Meta
                                avatar={
                                    <Skeleton.Avatar
                                        size={64}
                                        shape="square"
                                        active
                                    />
                                }
                                title={
                                    <Skeleton.Input
                                        style={{ width: 100 }}
                                        active
                                        size="small"
                                    />
                                }
                                description={
                                    <div className="pt-3">
                                        <Skeleton
                                            paragraph={{ rows: 0 }}
                                            active
                                        />
                                    </div>
                                }
                            />
                        </Card>
                        <Card className="h-23" size="small">
                            <Card.Meta
                                avatar={
                                    <Skeleton.Avatar
                                        size={64}
                                        shape="square"
                                        active
                                    />
                                }
                                title={
                                    <Skeleton.Input
                                        style={{ width: 100 }}
                                        active
                                        size="small"
                                    />
                                }
                                description={
                                    <div className="pt-3">
                                        <Skeleton
                                            paragraph={{ rows: 0 }}
                                            active
                                        />
                                    </div>
                                }
                            />
                        </Card>
                        <Card className="h-23" size="small">
                            <Card.Meta
                                avatar={
                                    <Skeleton.Avatar
                                        size={64}
                                        shape="square"
                                        active
                                    />
                                }
                                title={
                                    <Skeleton.Input
                                        style={{ width: 100 }}
                                        active
                                        size="small"
                                    />
                                }
                                description={
                                    <div className="pt-3">
                                        <Skeleton
                                            paragraph={{ rows: 0 }}
                                            active
                                        />
                                    </div>
                                }
                            />
                        </Card>
                    </div>
                ) : docList.length ? (
                    <div className="w-full grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-4 gap-5 p-5">
                        {docList.map((i) => {
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
                    <Empty />
                )}
            </Template_Page>
        </>
    );
};

export default DocumentList;
