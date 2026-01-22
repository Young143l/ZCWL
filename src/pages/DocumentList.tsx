import { Layout } from "antd";
import DocCard_component from "../components/DocCard_components";
import Hello_Sum_component from "../components/Hello_Sum_components";
import { useEffect, useState } from "react";
const { Content } = Layout;
import { type DocInfo, getDocList } from "../api/Doc_api";

const DocumentList = () => {
    const [docList, setDocList] = useState<DocInfo[]>([]);

    useEffect(()=>{
        setDocList(getDocList());
    },[]);
    return (
        <Content className="flex flex-col justify-center items-center w-full">
            <Hello_Sum_component />
            <div className="w-full grid grid-cols-[repeat(auto-fit,minmax(260px,1fr))] gap-5 md:p-0 p-5">
                {docList.map((i) => {return(
                    <DocCard_component
                        key={i.id}
                        DocName={i.name}
                        DocImg={i.img}
                        DocSum={i.summary}
                    />
                )})}
            </div>
        </Content>
    );
};

export default DocumentList;
