import { useEffect, useState, type FC } from "react";
import Template_Page from "../Template_Page";
import FileTree_components, {
    type Dir,
} from "../../components/FileTree_components";
import { Splitter, Tree, type GetProps, Spin } from "antd";
import ProjectCodeShow_components from "../../components/ProjectCodeShow_components";
import { useParams } from "react-router-dom";
import { getProject, getProjectFile } from "../../api/Project_api";
import useLogin from "../../status/Login_status";
import { LoadingOutlined } from "@ant-design/icons";
type DirectoryTreeProps = GetProps<typeof Tree.DirectoryTree>;

const Project: FC = () => {
    const { p_id } = useParams();
    const { token } = useLogin();
    const [dir, setDir] = useState<Dir>({
        folders: [],
        files: [],
        name: "",
    });
    const [name, setName] = useState<string>("");
    const [filePath, setFilePath] = useState<string | "NOFILE">("NOFILE");
    const [code, setCode] = useState<string>("");
    const [pageLoading, setPageLoading] = useState<boolean>(true);
    const [fileLoading, setFileLoading] = useState<boolean>(false);
    const onSelect: DirectoryTreeProps["onSelect"] = (key, info) => {
        if (info.node.isLeaf) {
            setFilePath(key[0] as string);
        }
    };
    useEffect(() => {
        const loadProject = async () => {
            setPageLoading(true);
            const res = await getProject(p_id as string, token);
            if (res.ok) {
                setDir(res.dir);
                setName(res.name);
                setTimeout(() => {
                    setPageLoading(false);
                }, 500);
            }
        };
        loadProject();
    }, [p_id, token]);

    useEffect(() => {
        const loadFile = async () => {
            if (filePath !== "NOFILE") {
                setCode("");
                setFileLoading(true);
                const res = await getProjectFile(
                    p_id as string,
                    filePath,
                    token,
                );
                if (res.ok) {
                    setCode(res.file);
                    setTimeout(() => {
                        setFileLoading(false);
                    }, 300);
                }
            }
        };
        loadFile();
    }, [filePath, p_id, token]);

    return (
        <>
            <Spin
                indicator={<LoadingOutlined spin />}
                spinning={pageLoading}
                size="large"
                fullscreen
            />
            <Template_Page>
                <div className=" h-[calc(100vh-95px)]">
                    <Splitter>
                        <Splitter.Panel
                            defaultSize="20%"
                            min="15%"
                            max="50%"
                            collapsible={{
                                start: false,
                                end: true,
                            }}
                        >
                            <FileTree_components
                                id={p_id as string}
                                dir={dir}
                                onSelect={onSelect}
                                name={name}
                            />
                        </Splitter.Panel>
                        <Splitter.Panel>
                            <ProjectCodeShow_components
                                filePath={filePath}
                                code={code}
                                setFilePath={setFilePath}
                                loading={fileLoading}
                                pName={name}
                            />
                        </Splitter.Panel>
                    </Splitter>
                </div>
            </Template_Page>
        </>
    );
};
export default Project;
