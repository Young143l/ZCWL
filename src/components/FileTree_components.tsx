import { useEffect, useMemo, useRef, useState, type FC } from "react";
import { theme, Tree, type TreeDataNode, type GetProps } from "antd";
const { DirectoryTree } = Tree;
type DirectoryTreeProps = GetProps<typeof Tree.DirectoryTree>;

export interface Floder {
    name: string;
    files: string[];
    floders: Floder[];
}
export interface Dir {
    files: string[];
    floders: Floder[];
}

export interface FTProps {
    dir: Dir;
    onSelect: DirectoryTreeProps["onSelect"];
    name: string;
}

const buildTree = (dir: Dir, path: string) => {
    const res: TreeDataNode[] = [];
    dir.floders.forEach((i: Floder) => {
        res.push({
            title: i.name,
            key: path + i.name,
            isLeaf: false,
            children: buildTree(i, path + i.name + "/"),
        });
    });
    dir.files.forEach((i: string) => {
        res.push({
            title: i,
            key: path + i,
            isLeaf: true,
        });
    });
    return res;
};

const FileTree_components: FC<FTProps> = ({ dir, onSelect, name }) => {
    const tree: TreeDataNode[] = useMemo(() => buildTree(dir, "/"), [dir]);
    const siderDiv = useRef<HTMLDivElement>(null);
    const [dirHeight, setDirHeight] = useState<number>(0);
    const pColor = theme.useToken().token.colorPrimaryBorder;

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

    return (
        <div className="p-1 h-full">
            <div
                
                className=" border-2 rounded-2xl p-1 overflow-hidden h-full flex flex-col"
                style={{
                    borderColor: pColor,
                }}
            >
                <div className="border-b border-gray-300 h-6 font-mono font-bold truncate pl-2 mb-0.5 shrink pr-2">{name}</div>
                <div ref={siderDiv} className="h-full">
                    <DirectoryTree
                        onSelect={onSelect}
                        height={dirHeight - 12}
                        treeData={tree as TreeDataNode[]}
                    />
                </div>
            </div>
        </div>
    );
};
export default FileTree_components;
