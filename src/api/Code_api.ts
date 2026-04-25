import type { CP } from "../pages/Code/CodeCP";
import { type SF, type code } from "../pages/Code/CodeSF";

export interface CodeProject {
    name: string;
    id: string;
    type: CodeType;
}

export type CodeType = "sf" | "cp_py"|"cp";
export type CPType = "python";

export const getCodeList: (
    token: string,
    uId?: string,
) => Promise<
    { ok: boolean; message: unknown } | { ok: boolean; list: CodeProject[] }
> = async (token: string, uId?: string) => {
    try {
        const uIds: string = `?uId=${uId}`;
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/code${uId ? uIds : ""}`,
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
        const json: { uId?: string; list: CodeProject[] } = await res.json();
        return { ok: true, list: json.list };
    } catch (e) {
        return { ok: false, message: e };
    }
};

export const newCodeSF: (
    message: string,
    uId: string,
    name: string,
    token: string,
) => Promise<
    { ok: boolean; message: unknown } | { ok: boolean; sfId: string }
> = async (message: string, uId: string, name: string, token: string) => {
    try {
        const res = await fetch(import.meta.env.VITE_BACK_END + "/code/sf", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
                uId: uId,
                projectName: name,
                message: message,
            }),
        });
        // console.log(res)
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json = await res.json();
        // console.log(json)
        return { ok: true, sfId: json.sfId };
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};

export const getCodeSF = async (token: string, sfId: string) => {
    try {
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/code/sf/${sfId}`,
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
        const json: SF = await res.json();
        return { ok: true, sf: json };
    } catch (e) {
        return { ok: false, message: e };
    }
};

export const askCodeSF: (
    token: string,
    code: code,
    selectId: string[],
    message: string,
    sfId: string,
) => Promise<
    { ok: boolean; message: unknown } | { ok: boolean; code: code }
> = async (
    token: string,
    code: code,
    selectId: string[],
    message: string,
    sfId: string,
) => {
    try {
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/code/sf/${sfId}`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    code: code,
                    message: message,
                    selectId: selectId,
                }),
            },
        );
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: { sfId: string; code: code } = await res.json();
        return { ok: true, code: json.code };
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};

export const delCodeSF: (
    sfId: string,
    token: string,
) => Promise<{ ok: boolean } | { ok: boolean; message: unknown }> = async (
    sfId: string,
    token: string,
) => {
    try {
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/code/sf/delete/${sfId}`,
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
        if (json.id == sfId) {
            return { ok: true };
        } else {
            throw new Error("Delete error!");
        }
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};

export const newCodeCP: (
    uId: string,
    type: CPType,
    message: string,
    name: string,
    token: string,
) => Promise<
    { ok: false; message: unknown } | { ok: true; cpId: string }
> = async (
    uId: string,
    type: CPType,
    message: string,
    name: string,
    token: string,
) => {
    try {
        const res = await fetch(import.meta.env.VITE_BACK_END + "/code/cp", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
                uId: uId,
                type: type,
                projectName: name,
                message: message,
            }),
        });
        if (!res.ok) {
            const json = await res.json()
            console.log(json)
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: { cpId: string; code: string; type: string } =
            await res.json();
        return { ok: true, cpId: json.cpId };
    } catch (e) {
        return { ok: false, message: e };
    }
};

export const getCodeCP = async (token: string, cpId: string) => {
    try {
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/code/cp/${cpId}`,
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
        const json: CP = await res.json();
        // console.log(json)
        return { ok: true, cp: json };
    } catch (e) {
        return { ok: false, message: e };
    }
};

export const askCodeCP: (
    token: string,
    code: string,
    message: string,
    cpId: string,
) => Promise<
    { ok: boolean; message: unknown } | { ok: boolean; code: string }
> = async (
    token: string,
    code: string,
    message: string,
    cpId: string,
) => {
    try {
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/code/cp/${cpId}`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    code: code,
                    message: message,
                }),
            },
        );
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: { cpId: string; code: string } = await res.json();
        return { ok: true, code: json.code };
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};

export const delCodeCP: (
    cpId: string,
    token: string,
) => Promise<{ ok: boolean } | { ok: boolean; message: unknown }> = async (
    cpId: string,
    token: string,
) => {
    try {
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/code/cp/delete/${cpId}`,
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
        if (json.id == cpId) {
            return { ok: true };
        } else {
            throw new Error("Delete error!");
        }
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};

/** ==================== AI 内联代码补全 API ==================== */

export interface CompletionRequest {
    language: string;
    prefixCode: string;
    suffixCode: string;
    currentLineContent: string;
    fullCode: string;
    cpId?: string;
    sfId?: string;
}

export interface CompletionResponse {
    completion: string;
    cached?: boolean;
    error?: string;
}

/**
 * CP 代码补全
 * 调用后端 AI 服务获取代码补全建议
 */
export const getCodeCompletion: (
    token: string,
    request: CompletionRequest,
    isSF?: boolean,
) => Promise<{ ok: boolean; completion: string } | { ok: boolean; error: string }> = async (
    token: string,
    request: CompletionRequest,
    isSF = false,
) => {
    try {
        const endpoint = isSF ? "/code/sf/completion" : "/code/cp/completion";
        const res = await fetch(
            import.meta.env.VITE_BACK_END + endpoint,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(request),
            },
        );
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: CompletionResponse = await res.json();
        if (json.error) {
            return { ok: false, error: json.error };
        }
        return { ok: true, completion: json.completion };
    } catch (e: unknown) {
        return { ok: false, error: String(e) };
    }
};
