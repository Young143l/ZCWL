import { Layout, Divider, Skeleton, Empty } from "antd";
import DocCard_component from "../components/DocCard_components";
import Hello_Sum_component from "../components/Hello_Sum_components";
import { useEffect, useState } from "react";
const { Content } = Layout;
import { type DocInfo, getDocList } from "../api/Doc_api";

const DocumentList = () => {
    const [docList, setDocList] = useState<DocInfo[]>([]);
    const [load, setLoad] = useState<boolean>(true);

    useEffect(() => {
        getDocList().then((res) => {
            if (res.ok) {
                setDocList(res.docList);
            }
            setTimeout(() => {
                setLoad(false);
            }, 500);
        });
    }, []);

    return (
        <Content className="flex flex-col justify-center items-center w-full">
            <Hello_Sum_component AllCmd="Cfww --document" />
                <div className="w-full bg-white p-2 rounded-xl">
                
                <Divider titlePlacement="start">学习文档</Divider>
                {load ? (
                    <Skeleton active />
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
                </div>
        </Content>
    );
};

export default DocumentList;
