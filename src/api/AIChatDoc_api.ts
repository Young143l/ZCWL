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
                }),
            },
        );

        if (!res.ok) {
            throw new Error("");
        }

        const reader = res.body?.getReader();
        return { ok: true, ans: reader };
    } catch (e) {
        console.log(e);
        return { ok: false, ans: null };
    }
};
