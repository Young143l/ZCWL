import { Breadcrumb, ConfigProvider, type BreadcrumbProps } from "antd";
import { useMemo, type FC } from "react";
import { Link } from "react-router-dom";
import { BookOutlined } from "@ant-design/icons";

interface Doc {
    d_id: string;
    d_name: string;
    c_id?: string;
    c_name?: string;
}

type Items = BreadcrumbProps["items"];

const DocBreadcrumb_components: FC<Doc> = ({ d_id, d_name, c_name, c_id }) => {
    const items: Items = useMemo(() => {
        const items: Items = [
            {
                title: (
                    <Link to={`/document/`}>
                        <BookOutlined />
                        Document
                    </Link>
                ),
            },
        ];
        if (c_id && c_name) {
            items.push(
                {
                    title: <Link to={`/document/${d_id}`}>{d_name}</Link>,
                },
                {
                    title: <p>{c_name}</p>,
                },
            );
        } else {
            items.push({
                title: <p>{d_name}</p>,
            });
        }
        return items;
    }, [d_id, d_name, c_id, c_name]);

    return (
        <ConfigProvider
            theme={{
                components: {
                    Breadcrumb: {
                        lastItemColor: "#000000",
                    },
                },
            }}
        >
            <div className="ml-1 mb-1.5">
                <Breadcrumb items={items} />
            </div>
        </ConfigProvider>
    );
};

export default DocBreadcrumb_components;
