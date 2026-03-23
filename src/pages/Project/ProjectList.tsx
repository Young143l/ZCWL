import Template_Page from "../Template_Page";
import Hello_Sum_components from "../../components/Hello_Sum_components";
import { type FC, useEffect, useState } from "react";
import { SearchOutlined } from "@ant-design/icons";
import { Input, theme, Skeleton, Empty, Button, Modal, message } from "antd";
import { getProjectList, newProject } from "../../api/Project_api";
import useLogin from "../../status/Login_status";
import { useNavigate } from "react-router-dom";
import { PlusOutlined, GithubOutlined } from "@ant-design/icons";
import LoadingWindow_components from "../../components/PullLoadingWindow_components";

interface ProjectItem {
    id: string;
    projectName: string;
}

const ProjectList: FC = () => {
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const { token, userId, isLogin } = useLogin();
    const nav = useNavigate();
    const [projectList, setProjectList] = useState<ProjectItem[]>([]);
    const [nowProjectList, setNowProjectList] = useState<ProjectItem[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false);
    const [newProjectName, setNewProjectName] = useState<string>("");
    const [newProjectUrl, setNewProjectUrl] = useState<string>("");
    const [isCreating, setIsCreating] = useState<boolean>(false);
    const [createSuccess, setCreateSuccess] = useState<boolean>(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState<boolean>(false);
    const [messageApi, contextHolder] = message.useMessage();

    useEffect(() => {
        let time: number | null = null;
        if (!isLogin) {
            nav("/login");
            return;
        }
        getProjectList(userId, token).then((res) => {
            if (res.ok) {
                console.log(res);
                const list = (res as { ok: boolean; list: ProjectItem[] }).list;
                setProjectList(list);
                setNowProjectList(list);
            }
            time = setTimeout(() => {
                setLoading(false);
            }, 300);
        });
        return () => {
            if (time != null) {
                clearTimeout(time);
            }
        };
    }, [isLogin, nav, token, userId]);

    const fetchProjectList = () => {
        setLoading(true);
        getProjectList(userId, token).then((res) => {
            if (res.ok) {
                const list = (res as { ok: boolean; list: ProjectItem[] }).list;
                setProjectList(list);
                setNowProjectList(list);
            }
            setLoading(false);
        });
    };

    const handleSearch = () => {
        let time: number | null = null;
        return (e: React.ChangeEvent<HTMLInputElement>) => {
            if (time != null) clearTimeout(time);
            time = setTimeout(() => {
                setNowProjectList(
                    projectList.filter((i: ProjectItem) =>
                        i.projectName.includes(e.target.value),
                    ),
                );
            }, 260);
        };
    };

    const handleCreateProject = () => {
        if (!newProjectName.trim() || !newProjectUrl.trim()) {
            messageApi.warning("请填写完整信息");
            return;
        }
        setIsCreateModalOpen(true);
        setIsCreating(true);
        setCreateSuccess(false);

        newProject(userId, newProjectName, newProjectUrl, token).then((res) => {
            if (res.ok) {
                setIsCreating(false);
                setCreateSuccess(true);
                const pid = res.id;
                setTimeout(() => {
                    setIsCreateModalOpen(false);
                    setIsModalOpen(false);
                    setNewProjectName("");
                    setNewProjectUrl("");
                    fetchProjectList();
                    console.log(pid);
                    nav("/project/" + pid);
                }, 800);
            } else {
                setIsCreating(false);
                setCreateSuccess(false);
                messageApi.error("创建失败");
                setTimeout(() => {
                    setIsCreateModalOpen(false);
                }, 1500);
            }
        });
    };

    return (
        <>
            {contextHolder}
            <Hello_Sum_components AllCmd="Cfww --project" title="Project" />
            <Template_Page>
                <div className="w-full p-2 flex flex-col gap-2.5">
                    {/* 创建项目弹窗 */}
                    <Modal
                        title="拉取新仓库"
                        open={isModalOpen}
                        onOk={handleCreateProject}
                        onCancel={() => {
                            setIsModalOpen(false);
                            setNewProjectName("");
                            setNewProjectUrl("");
                        }}
                        okText="创建"
                        cancelText="取消"
                    >
                        <div className="flex flex-col gap-4 mt-4">
                            <div>
                                <label className="block text-sm font-medium mb-1">
                                    项目名称
                                </label>
                                <Input
                                    placeholder="请输入项目名称"
                                    value={newProjectName}
                                    onChange={(e) =>
                                        setNewProjectName(e.target.value)
                                    }
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium mb-1">
                                    GitHub 仓库地址
                                </label>
                                <Input
                                    placeholder="https://github.com/username/repo.git"
                                    value={newProjectUrl}
                                    onChange={(e) =>
                                        setNewProjectUrl(e.target.value)
                                    }
                                />
                            </div>
                        </div>
                    </Modal>

                    {/* 加载弹窗 */}
                    <LoadingWindow_components
                        isOpen={isCreateModalOpen}
                        loading={isCreating}
                        success={createSuccess}
                    />

                    {/* 创建新项目区域 */}
                    <div className="flex flex-col gap-2.5">
                        <h1
                            className="text-xl border-l-4 pl-1"
                            style={{
                                borderColor: pColor,
                            }}
                        >
                            拉取新仓库
                        </h1>
                        <div
                            className="border-2 w-full rounded-xl p-4 flex items-center justify-between"
                            style={{
                                borderColor: pColor,
                            }}
                        >
                            <div className="flex items-center gap-2 text-gray-500">
                                <GithubOutlined />
                                <span>从 GitHub 拉取代码仓库到本地</span>
                            </div>
                            <Button
                                // type="primary"
                                icon={<PlusOutlined />}
                                onClick={() => setIsModalOpen(true)}
                            >
                                拉取仓库
                            </Button>
                        </div>
                    </div>

                    {/* 项目列表区域 */}
                    <div className="flex justify-between">
                        <h1
                            className="text-xl border-l-4 pl-1"
                            style={{
                                borderColor: pColor,
                            }}
                        >
                            已拉取仓库列表
                        </h1>
                        <div className="max-w-64">
                            <Input
                                placeholder="Search..."
                                prefix={<SearchOutlined />}
                                onChange={handleSearch()}
                            />
                        </div>
                    </div>

                    {loading ? (
                        <div className="w-full grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-4 gap-5">
                            <Skeleton.Node
                                active
                                style={{
                                    height: 58,
                                    width: "100%",
                                    borderRadius: 12,
                                }}
                            />
                            <Skeleton.Node
                                active
                                style={{
                                    height: 58,
                                    width: "100%",
                                    borderRadius: 12,
                                }}
                            />
                            <Skeleton.Node
                                active
                                style={{
                                    height: 58,
                                    width: "100%",
                                    borderRadius: 12,
                                }}
                            />
                            <Skeleton.Node
                                active
                                style={{
                                    height: 58,
                                    width: "100%",
                                    borderRadius: 12,
                                }}
                            />
                        </div>
                    ) : nowProjectList.length ? (
                        <div className="w-full grid grid-cols-1 sm:grid-cols-2 md:grid-cols-2 lg:grid-cols-4 xl:grid-cols-4 gap-5">
                            {nowProjectList.map((project) => (
                                <div
                                    key={project.id}
                                    className="rounded-xl bg-gray-50 hover:bg-gray-200 border-2 p-2 flex items-center justify-between group cursor-pointer"
                                    style={{
                                        borderColor: pColor,
                                    }}
                                    onClick={() =>
                                        nav(`/project/${project.id}`)
                                    }
                                >
                                    <div className="flex items-center gap-2.5 overflow-hidden">
                                        <GithubOutlined className="text-xl shrink-0" />
                                        <div className="font-bold text-lg overflow-hidden text-ellipsis whitespace-nowrap">
                                            {project.projectName}
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="mt-20">
                            <Empty
                                image={Empty.PRESENTED_IMAGE_SIMPLE}
                                description="暂无仓库"
                            />
                        </div>
                    )}
                </div>
            </Template_Page>
        </>
    );
};

export default ProjectList;
