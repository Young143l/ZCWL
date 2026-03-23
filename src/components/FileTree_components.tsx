import { useEffect, useMemo, useRef, useState, type FC } from "react";
import {
    theme,
    Tree,
    type TreeDataNode,
    type GetProps,
    Button,
    message,
    Popconfirm,
} from "antd";
import { delProject } from "../api/Project_api";
import useLogin from "../status/Login_status";
const { DirectoryTree } = Tree;
import { DeleteOutlined } from "@ant-design/icons";
import { useNavigate } from "react-router-dom";
type DirectoryTreeProps = GetProps<typeof Tree.DirectoryTree>;

export interface Folder {
    name: string;
    files: string[];
    folders: Folder[];
}
export interface Dir {
    files: string[];
    folders: Folder[];
    name: string;
}

export interface FTProps {
    dir: Dir;
    onSelect: DirectoryTreeProps["onSelect"];
    name: string;
    id: string;
}

const buildTree = (dir: Dir, path: string) => {
    const res: TreeDataNode[] = [];
    (dir.folders || []).forEach((i: Folder) => {
        if(i.name===".git") return;
        res.push({
            title: i.name,
            key: path + i.name,
            isLeaf: false,
            children: buildTree(i, path + i.name + "/"),
        });
    });
    (dir.files || []).forEach((i: string) => {
        res.push({
            title: i,
            key: path + i,
            isLeaf: true,
        });
    });
    return res;
};

const FileTree_components: FC<FTProps> = ({ dir, onSelect, name, id }) => {
    const tree: TreeDataNode[] = useMemo(() => buildTree(dir, "/"), [dir]);
    const siderDiv = useRef<HTMLDivElement>(null);
    const [dirHeight, setDirHeight] = useState<number>(0);
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const { token } = useLogin();
    const [messageApi, contextHolder] = message.useMessage();
    const nav = useNavigate();
    useEffect(() => {
        const updateHeight = () => {
            if (siderDiv.current) {
                setDirHeight(siderDiv.current.clientHeight);
            }
        };

        updateHeight();

        window.addEventListener("resize", updateHeight);

        return () => {
            window.removeEventListener("resize", updateHeight);
        };
    }, []);

    const handleDelete = () => {
        return new Promise((resolve) => {
            setTimeout(() => {
                delProject(id, token).then((res) => {
                    if (res.ok) {
                        messageApi.success("删除成功");
                        setTimeout(() => {
                            nav("/project");
                        }, 500);
                        resolve(null);
                    } else {
                        messageApi.error("删除失败");
                        resolve(null);
                    }
                });
            }, 1000);
        });
    };

    return (
        <>
            {contextHolder}
            <div className="p-1 h-full">
                <div
                    className=" border-2 rounded-2xl p-1 overflow-hidden h-full flex flex-col"
                    style={{
                        borderColor: pColor,
                    }}
                >
                    <div
                        className="flex justify-between items-center border-b-2 p-0.5"
                        style={{
                            borderColor: pColor,
                        }}
                    >
                        <div className="pl-2 text-xl font-mono font-bold truncate  shrink ">
                            {name}
                        </div>
                        <Popconfirm
                            title="删除此项目"
                            description={`您确定要删除${name}吗？`}
                            onConfirm={handleDelete}
                            okText="删除"
                            cancelText="取消"
                        >
                            <Button
                                color="primary"
                                icon={<DeleteOutlined />}
                                variant="text"
                            />
                        </Popconfirm>
                    </div>
                    <div ref={siderDiv} className="h-full">
                        <DirectoryTree
                            onSelect={onSelect}
                            height={dirHeight - 12}
                            treeData={tree as TreeDataNode[]}
                        />
                    </div>
                </div>
            </div>
        </>
    );
};
export default FileTree_components;
