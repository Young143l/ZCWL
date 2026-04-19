export const getNewChat = async (u_id: string, token: string) => {
    try {
        const res = await fetch(import.meta.env.VITE_BACK_END + "/aichatdoc", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
                uId: u_id,
            }),
        });
        if (!res.ok) {
            throw new Error("");
        }
        const json = await res.json();
        return { ok: true, message: "ok", id: json.id };
    } catch (e) {
        return { ok: false, message: e, id: "json.id" };
    }
};

export const getAsk = async (
    u_id: string,
    id: string,
    ask: string,
    token: string,
    img?: string,
) => {
    try {
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/aichatdoc/${id}`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    ask: ask,
                    uId: u_id,
                    img: img || "",
                }),
            },
        );

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const reader = res.body?.getReader();
        return { ok: true, ans: reader };
    } catch (e) {
        console.log(e);
        return { ok: false, ans: null };
    }
};

// 通过对话ID获取聊天记录
export const getChatById = async (id: string, token: string) => {
    try {
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/aichatdoc/${id}`,
            {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            },
        );
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json = await res.json();
        return {
            ok: true,
            data: {
                id: json.id,
                chat: json.chat || [],
            },
        };
    } catch (e) {
        console.log(e);
        return { ok: false, data: null };
    }
};

// 通过用户ID获取聊天记录
export const getChatByUserId = async (u_id: string, token: string) => {
    try {
        const res = await fetch(
            import.meta.env.VITE_BACK_END + `/aichatdoc?u_id=${u_id}`,
            {
                method: "GET",
                headers: {
                    Authorization: `Bearer ${token}`,
                },
            },
        );
        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json = await res.json();
        return {
            ok: true,
            data: {
                id: json.id,
                u_id: json.u_id,
                chat: json.chat || [],
            },
        };
    } catch (e) {
        console.log(e);
        return { ok: false, data: null };
    }
};
