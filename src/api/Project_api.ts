import type { Dir } from "../components/FileTree_components";

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
