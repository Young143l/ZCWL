interface LearningRecord {
    id: number;
    docId: number;
    chapterId: number | null;
    progress: number;
    lastPosition: number;
    status: string;
    lastAccessTime: string;
    startTime: string;
    completeTime: string | null;
}

interface LearningStats {
    totalDocs: number;
    completedChapters: number;
    streakDays: number;
    totalStudyDays: number;
    lastStudyDate: string | null;
}

interface HeatmapData {
    date: string;
    count: number;
    level: number;
}

const BASE_URL = import.meta.env.VITE_BACK_END;

export const startLearning = async (
    docId: number,
    chapterId: number | null,
    token: string
): Promise<{ success: boolean; data?: LearningRecord; error?: string }> => {
    try {
        const res = await fetch(`${BASE_URL}/learning/start`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify({ docId, chapterId }),
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const data = await res.json();
        return { success: true, data };
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, error: errorMessage };
    }
};

export const updateProgress = async (
    docId: number,
    chapterId: number | null,
    progress: number,
    position: number,
    token: string
): Promise<{ success: boolean; data?: LearningRecord; error?: string }> => {
    try {
        const res = await fetch(`${BASE_URL}/learning/progress`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify({ docId, chapterId, progress, position }),
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const data = await res.json();
        return { success: true, data };
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, error: errorMessage };
    }
};

export const endLearning = async (
    docId: number,
    chapterId: number | null,
    token: string
): Promise<{ success: boolean; error?: string }> => {
    try {
        const res = await fetch(`${BASE_URL}/learning/end`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify({ docId, chapterId }),
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        return { success: true };
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, error: errorMessage };
    }
};

export const getLearningRecord = async (
    docId: number,
    chapterId: number | null,
    token: string
): Promise<{ success: boolean; data?: LearningRecord; error?: string }> => {
    try {
        let url = `${BASE_URL}/learning/record/${docId}`;
        if (chapterId !== null) {
            url += `/${chapterId}`;
        }

        const res = await fetch(url, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        if (res.status === 404) {
            return { success: true, data: undefined };
        }

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const data = await res.json();
        return { success: true, data };
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, error: errorMessage };
    }
};

export const getRecentLearning = async (
    token: string
): Promise<{ success: boolean; data?: LearningRecord[]; error?: string }> => {
    try {
        const res = await fetch(`${BASE_URL}/learning/recent`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const json = await res.json();
        return { success: true, data: json.records };
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, error: errorMessage };
    }
};

export const getLearningStats = async (
    token: string
): Promise<{ success: boolean; data?: LearningStats; error?: string }> => {
    try {
        const res = await fetch(`${BASE_URL}/learning/stats`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const data = await res.json();
        return { success: true, data };
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, error: errorMessage };
    }
};

export const getDocChapterProgress = async (
    docId: number,
    token: string
): Promise<{ success: boolean; data?: LearningRecord[]; error?: string }> => {
    try {
        const res = await fetch(`${BASE_URL}/learning/progress/${docId}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const json = await res.json();
        return { success: true, data: json.chapters };
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, error: errorMessage };
    }
};

export const getAllDocProgress = async (
    token: string
): Promise<{ success: boolean; data?: { docId: number; avgProgress: number; status: string; lastAccessTime: string }[]; error?: string }> => {
    try {
        const res = await fetch(`${BASE_URL}/learning/progress/all`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const json = await res.json();
        return { success: true, data: json.docs };
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, error: errorMessage };
    }
};

export const getDocsProgress = async (
    token: string
): Promise<{ success: boolean; data?: Record<number, number>; error?: string }> => {
    try {
        const res = await fetch(`${BASE_URL}/learning/progress/all`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const json = await res.json();
        // 将数组转换为以 docId 为键的映射
        const progressMap: Record<number, number> = {};
        if (json.docs && Array.isArray(json.docs)) {
            for (const doc of json.docs) {
                progressMap[doc.docId] = doc.avgProgress;
            }
        }
        return { success: true, data: progressMap };
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, error: errorMessage };
    }
};

export const getLearningHeatmap = async (
    token: string
): Promise<{ success: boolean; data?: HeatmapData[]; error?: string }> => {
    try {
        const res = await fetch(`${BASE_URL}/learning/heatmap`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            },
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const json = await res.json();
        return { success: true, data: json.heatmap };
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, error: errorMessage };
    }
};

export type { LearningRecord, LearningStats, HeatmapData };
