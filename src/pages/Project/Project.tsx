import { useEffect, useState, type FC } from "react";
import Template_Page from "../Template_Page";
import FileTree_components, {
    type Dir,
} from "../../components/FileTree_components";
import { Splitter, Tree, type GetProps } from "antd";
import ProjectCodeShow_components from "../../components/ProjectCodeShow_components";
import { useParams } from "react-router-dom";
import { getProject, getProjectFile } from "../../api/Project_api";
import useLogin from "../../status/Login_status";
type DirectoryTreeProps = GetProps<typeof Tree.DirectoryTree>;

const testDir: Dir = {
    files: ["readme.md", "package.json"],
    floders: [
        {
            name: "src",
            files: ["index.ts", "utils.ts"],
            floders: [
                {
                    name: "components",
                    files: ["Button.tsx", "Input.tsx"],
                    floders: [
                        {
                            name: "common",
                            files: ["Modal.tsx"],
                            floders: [],
                        },
                    ],
                },
                {
                    name: "pages",
                    files: ["Home.tsx", "About.tsx"],
                    floders: [
                        {
                            name: "api",
                            files: ["user.ts", "product.ts"],
                            floders: [],
                        },
                    ],
                },
            ],
        },
        {
            name: "public",
            files: ["index.html", "favicon.ico"],
            floders: [
                {
                    name: "assets",
                    files: ["logo.svg", "style.css"],
                    floders: [],
                },
            ],
        },
        {
            name: "docs",
            files: ["getting-started.md", "api-reference.md"],
            floders: [
                {
                    name: "guides",
                    files: ["setup-guide.md", "deployment.md"],
                    floders: [
                        {
                            name: "advanced",
                            files: ["performance-tuning.md"],
                            floders: [],
                        },
                    ],
                },
            ],
        },
    ],
};
const Project: FC = () => {
    const { p_id } = useParams();
    const { token } = useLogin();
    const [dir, setDir] = useState<Dir>({
        floders: [],
        files: [],
    });
    const [name, setName] = useState<string>("");
    const [filePath, setFilePath] = useState<string | "NOFILE">("NOFILE");
    const [code, setCode] = useState<string>("");
    const onSelect: DirectoryTreeProps["onSelect"] = (key, info) => {
        if (info.node.isLeaf) {
            setFilePath(key[0] as string);
        }
    };
    useEffect(() => {
        getProject(p_id as string, token).then((res) => {
            if (res.ok) {
                setDir(res.dir);
                setName(res.name);
            }
        });
    }, [p_id, token]);

    useEffect(() => {
        if (filePath !== "NOFILE") {
            getProjectFile(p_id as string, filePath, token).then((res) => {
                if (res.ok) {
                    setCode(res.file);
                }
            });
        }
    }, [filePath, p_id, token]);

    return (
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
                            dir={testDir}
                            onSelect={onSelect}
                            name={name}
                        />
                    </Splitter.Panel>
                    <Splitter.Panel>
                        <ProjectCodeShow_components
                            filePath={filePath}
                            code={code}
                            setFilePath={setFilePath}
                        />
                    </Splitter.Panel>
                </Splitter>
            </div>
        </Template_Page>
    );
};
export default Project;
