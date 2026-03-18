import type { Dir } from "../components/FileTree_components";

export const getProjectList: (
    uId: string,
    token: string,
) => Promise<
    | { ok: true; list: { name: string; id: string }[] }
    | { ok: false; message: unknown }
> = async (uId: string, token: string) => {
    try {
        const uIds: string = `/?uId=${uId}`;
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `project${uId ? uIds : ""}`,
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
        const json: { list: { name: string; id: string }[] } = await res.json();
        return { ok: true, list: json.list };
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
                name: name,
                url: url,
            }),
        });
        if (!res.ok) {
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
        return { ok: true, file: json.file };
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
