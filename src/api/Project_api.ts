import type { Dir } from "../components/FileTree_components";
import type { CodeSnap } from "../components/ProjectCodeShow_components";

export const getProjectList: (
    uId: string,
    token: string,
) => Promise<
    | { ok: true; list: { projectName: string; id: string }[] }
    | { ok: false; message: unknown }
> = async (uId: string, token: string) => {
    try {
        const uIds: string = `/?uId=${uId}`;
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/project${uId ? uIds : ""}`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            },
        );
        if (!res.ok) {
            // const json =await res.json();
            // // console.log(json)
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: { projects: { projectName: string; id: string }[] } =
            await res.json();
        console.log(json);

        return { ok: true, list: json.projects };
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};

export const newProject: (
    uId: string,
    name: string,
    url: string,
    token: string,
) => Promise<{ ok: true; id: string } | { ok: false }> = async (
    uId: string,
    name: string,
    url: string,
    token: string,
) => {
    try {
        const res = await fetch(import.meta.env.VITE_BACK_END + "/project", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
                uId: uId,
                projectName: name,
                url: url,
            }),
        });
        if (!res.ok) {
            const json = await res.json();
            console.log(json);
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: { id: string } = await res.json();
        return { ok: true, id: json.id };
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};

export const getProject: (
    id: string,
    token: string,
) => Promise<
    { ok: false; message: unknown } | { ok: true; dir: Dir; name: string }
> = async (id: string, token: string) => {
    try {
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/project/${id}`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            },
        );
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: { id: string; name: string; path: Dir } = await res.json();
        return { ok: true, dir: json.path, name: json.name };
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};

export const getProjectFile: (
    id: string,
    fileName: string,
    token: string,
) => Promise<
    { ok: false; message: unknown } | { ok: true; file: string }
> = async (id: string, fileName: string, token: string) => {
    try {
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/project/${id}`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    fileName: fileName,
                }),
            },
        );
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: { file: string; fileName: string } = await res.json();
        const fileres = await fetch(json.file);
        if (!fileres.ok) {
            throw new Error(`HTTP error! status: ${fileres.status}`);
        }
        const file = await fileres.text();
        // console.log(file);
        return { ok: true, file: file };
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};

export const delProject: (
    id: string,
    token: string,
) => Promise<{ ok: true } | { ok: false; message: unknown }> = async (
    id: string,
    token: string,
) => {
    try {
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/project/delet/${id}`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
            },
        );
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: { id: string } = await res.json();
        if (json.id == id) return { ok: true };
        else throw new Error("delete error!");
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};

export const askProject: (
    id: string,
    ask: string,
    codeSnap: { fileName: string; lineStart: number; lineEnd: number }[],
) => Promise<
    { ok: true; ans: string } | { ok: false; message: unknown }
> = async (id: string, ask: string, codeSnap: CodeSnap[]) => {
    try {
        const res = await fetch(
            import.meta.env.VITE_MCP_SERVER + `/ask/${id}`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    // Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    ask: ask,
                    codeSnap: codeSnap,
                }),
            },
        );
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: { ans: string } = await res.json();
        return { ok: true, ans: json.ans };
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};

export const askProjectStream: (
    id: string,
    ask: string,
    codeSnap: { fileName: string; lineStart: number; lineEnd: number }[],
    onChunk: (text: string) => void,
) => Promise<{ ok: true } | { ok: false; message: unknown }> = async (
    id: string,
    ask: string,
    codeSnap: { fileName: string; lineStart: number; lineEnd: number }[],
    onChunk: (text: string) => void,
) => {
    try {
        const res = await fetch(
            `${import.meta.env.VITE_MCP_SERVER}/ask/${encodeURIComponent(id)}/stream`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    ask: ask,
                    codeSnap: codeSnap,
                }),
            },
        );
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        if (!res.body) {
            throw new Error("Response body is null");
        }
        const reader = res.body.getReader();
        const decoder = new TextDecoder();

        const read = () => {
            reader.read().then(({ done, value }) => {
                if (done) return;

                const chunk = decoder.decode(value, { stream: true });
                const lines = chunk.split("\n");

                for (const line of lines) {
                    if (line.startsWith("data: ")) {
                        try {
                            const data = JSON.parse(line.slice(6));
                            if (data.text && data.text.trim()) {
                                onChunk(data.text);
                            }
                            if (data.error) {
                                throw new Error(data.error);
                            }
                        } catch {
                            // 忽略解析错误
                        }
                    }
                }

                read();
            }).catch((e: Error) => {
                throw e;
            });
        };

        read();
        return { ok: true };
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};

export const getProjectDoc: (
    id: string,
) => Promise<
    { ok: true; doc: string } | { ok: false; message: unknown }
> = async (id: string) => {
    try {
        if (!id || id === "") {
            throw new Error("ID is error!");
        }

        const res = await fetch(
            import.meta.env.VITE_MCP_SERVER + `/doc/${id}`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                },
            },
        );
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: { doc: string } = await res.json();
        return { ok: true, doc: json.doc };
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};
