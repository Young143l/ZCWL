import { useEffect, useState, type FC } from "react";
import { useParams } from "react-router-dom";
import {
    type DocDir,
    type DocInfo,
    getDocDirectory,
    getDocInfo,
} from "../api/Doc_api";
import { Content } from "antd/es/layout/layout";
import Chaper_componenents from "../components/Chapter_components";

const DocumentDirectory: FC = () => {
    const { d_id } = useParams();
    const [docDirectory, setDocDirectory] = useState<DocDir[]>([]);
    const [docInfo, setdocInfo] = useState<DocInfo | undefined>(undefined);
    useEffect(() => {
        getDocDirectory(d_id as string).then((res) => {
            if (res.ok) {
                setDocDirectory(res.docDire);
            }
        });
    }, [d_id]);

    return (
        <Content className="flex flex-col justify-center items-center w-full">
            <div className="w-full bg-white p-2 rounded-xl">
            {docDirectory.map((i) => (
                <Chaper_componenents id={i.id} name={i.name} />
            ))}
            </div>
        </Content>
    );
};

export default DocumentDirectory;
