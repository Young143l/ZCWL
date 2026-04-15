// RAG 搜索接口 - 获取溯源信息
export interface RAGSearchResult {
    id: number;
    content: string;
    distance: number;
    d_id:string;
    c_id:string;
}

export const ragSearch = async (query: string, token: string): Promise<RAGSearchResult[]> => {
    try {
        const res = await fetch(import.meta.env.VITE_BACK_END + "/rag/search", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({
                query: query,
            }),
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const data = await res.json();
        // 只返回前5个结果
        return (data as RAGSearchResult[]).slice(0, 5);
    } catch (e) {
        console.error("RAG search error:", e);
        return [];
    }
};
