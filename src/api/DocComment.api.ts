import type { Comment } from "../components/DocComment_compents";

export interface CommentRequest {
    uId: string;
    email: string;
    content: string;
    fa: string; // 父评论id，-1则表示无
}

export const getComments: (
    d_id: string,
    c_id: string,
) => Promise<
    { ok: false; message: unknown } | { ok: true; comments: Comment[] }
> = async (d_id: string, c_id: string) => {
    try {
        const res: Response = await fetch(
            import.meta.env.VITE_BACK_END + `/doc/comment/${d_id}/${c_id}`,
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
        const json: { comments: Comment[] } = await res.json();
        if (json) {
            console.log(json);

            return { ok: true, comments: json.comments };
        }
        throw Error("Response Error!");
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};

export const postComment: (
    d_id: string,
    c_id: string,
    comment: CommentRequest,
    token: string,
) => Promise<
    { ok: false; message: unknown } | { ok: true; id: string }
> = async (
    d_id: string,
    c_id: string,
    comment: CommentRequest,
    token: string,
) => {
    try {
        const res: Response = await fetch(
            import.meta.env.VITE_BACK_END + `/doc/comment/${d_id}/${c_id}`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify(comment),
            },
        );
        if (!res.ok) {
            // const json = await res.json();
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: { id: string } = await res.json();
        if (json) {
            return { ok: true, id: json.id };
        }
        throw Error("Response Error!");
    } catch (e: unknown) {
        return { ok: false, message: e };
    }
};
