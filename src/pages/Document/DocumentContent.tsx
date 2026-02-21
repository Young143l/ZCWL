import { useEffect, useState, type FC } from "react";
import { Link, useParams } from "react-router-dom";
import Template_Page from "../Template_Page";
import ReactMarkdown from "react-markdown";
import { Menu, type MenuProps } from "antd";
import {
    getDocContent,
    getDoc,
    type DocInfo,
    type DocContent,
} from "../../api/Doc_api";
import DocBreadcrumb_components from "../../components/DocBreadcrumb_components";
type MenuItem = Required<MenuProps>["items"][number];
import "github-markdown-css/github-markdown.css";
import remarkGfm from "remark-gfm";

const DocumentContent: FC = () => {
    const { d_id, c_id } = useParams();
    const [items, setItems] = useState<MenuItem[]>([]);
    const [docContent, setDocContent] = useState<DocContent | undefined>(
        undefined,
    );
    const [docInfo, setdocInfo] = useState<DocInfo | undefined>(undefined);
    useEffect(() => {
        getDoc(d_id as string).then((res) => {
            if (res.ok) {
                setdocInfo(res.docInfo);
                setItems(
                    res.docDir.map((i) => {
                        return {
                            key: i.id,
                            label: (
                                <Link to={`/document/${d_id}/${i.id}`}>
                                    {i.name}
                                </Link>
                            ),
                        } as MenuItem;
                    }),
                );
            }
        });
        getDocContent(d_id as string, c_id as string).then((res) => {
            if (res.ok) {
                setDocContent(res.docContent);
            }
        });
    }, [d_id, c_id]);

    return (
        <>
            <DocBreadcrumb_components
                d_id={d_id as string}
                d_name={docInfo?.name as string}
                c_id={c_id}
                c_name={docContent?.title as string}
            />
            <Template_Page
                children={
                    <div className="markdown-body p-3">
                        <ReactMarkdown remarkPlugins={[remarkGfm]}>
                            {docContent?.content}
                        </ReactMarkdown>
                    </div>
                }
                sider={
                    <Menu
                        defaultSelectedKeys={[c_id as string]}
                        mode="inline"
                        items={items}
                    />
                }
            />
        </>
    );
};
export default DocumentContent;
