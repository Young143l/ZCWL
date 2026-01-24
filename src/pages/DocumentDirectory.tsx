import { useEffect, useState, type FC } from "react";
import { useParams } from "react-router-dom";
import { type DocDir, type DocInfo, getDoc } from "../api/Doc_api";
import Chaper_componenents from "../components/Chapter_components";
import DocInfoCard_components from "../components/DocInfoCard_components";
import { Skeleton } from "antd";
import Template_Page from "./Template_Page";
import DocBreadcrumb_components from "../components/DocBreadcrumb_components";
const DocumentDirectory: FC = () => {
    const { d_id } = useParams();
    const [docDirectory, setDocDirectory] = useState<DocDir[]>([]);
    const [docInfo, setdocInfo] = useState<DocInfo | undefined>(undefined);

    const [load, setLoad] = useState<boolean>(true);
    useEffect(() => {
        getDoc(d_id as string).then(
            (res: {
                ok: boolean;
                docInfo: DocInfo | undefined;
                docDir: DocDir[];
            }) => {
                if (res.ok) {
                    setDocDirectory(res.docDir);
                    setdocInfo(res.docInfo);
                    setLoad(false);
                }
            },
        );
    }, [d_id]);

    return (
        <>
        <DocBreadcrumb_components d_id={d_id as string} d_name={docInfo?.name as string}/>
        <Template_Page>
            {load ? (
                <Skeleton active />
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
