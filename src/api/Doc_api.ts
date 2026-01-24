export interface DocInfo {
    id: string;
    name: string;
    summary: string;
    img: string;
}
export interface DocDir {
    id: string;
    name: string;
}

export interface DocContent {
    d_id: string;
    c_id: string;
    title: string;
    content: string;
}

export const getDoc: (d_id: string) => Promise<{
    ok: boolean;
    docInfo: DocInfo | undefined;
    docDir: DocDir[];
}> = async (d_id: string) => {
    try {
        const res: Response = await fetch(
            import.meta.env.VITE_BACK_END + `/doc/${d_id}`,
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
        const json: { docInfo: DocInfo; docDir: DocDir[] } = await res.json();
        if (json) {
            return { ok: true, docInfo: json.docInfo,docDir: json.docDir};
        }
        throw Error("Response Error!");
    } catch (e) {
        return { ok: false, docInfo: undefined,docDir:[] };
    }
};

export const getDocList: () => Promise<{
    ok: boolean;
    docList: DocInfo[];
}> = async () => {
    try {
        const res: Response = await fetch(
            import.meta.env.VITE_BACK_END + "/doc",
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
        const json:{docList:DocInfo[]} = await res.json();
        return { ok: true, docList: json.docList };
    } catch (e) {
        console.error("获取文档列表失败:", e);
        return { ok: false, docList: [] };
    }
};

// export const getDocDirectory: (
//     d_id: string,
// ) => Promise<{ ok: boolean; docDire: DocDir[] }> = async (d_id: string) => {
//     try {
//         const res: Response = await fetch(
//             import.meta.env.VITE_BACK_END + `/doc/${d_id}`,
//             {
//                 method: "GET",
//                 headers: {
//                     "Content-Type": "application/json",
//                 },
//             },
//         );
//         if (!res.ok) {
//             throw new Error(`HTTP error! status: ${res.status}`);
//         }
//         const json: { docInfo: DocInfo; docDir: DocDir[] } = await res.json();
//         if (json) {
//             return { ok: true, docDire: json.docDir };
//         }
//         throw Error("Response Error!");
//     } catch (e) {
//         return { ok: false, docDire: [] };
//     }
// };

export const getDocContent: (
    d_id: string,
    c_id: string,
) => Promise<{
    ok: boolean;
    docContent: DocContent | undefined;
}> = async (d_id: string, c_id: string) => {
    try {
        const res: Response = await fetch(
            import.meta.env.VITE_BACK_END + `/doc/${d_id}/${c_id}`,
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
        const json: DocContent = await res.json();
        if (json) {
            return { ok: true, docContent: json };
        }
        throw Error("Response Error!");
    } catch (e) {
        return { ok: false, docContent: undefined };
    }
};
