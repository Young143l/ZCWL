const BASE_URL = import.meta.env.VITE_BACK_END;

export interface QuizQuestion {
    question: string;
    options: Record<string, string>;
    answer: string;
    explanation: string;
}

export interface QuizGradeDetail {
    correct: boolean;
    userAnswer: string;
    correctAnswer: string;
    feedback: string;
}

export interface QuizGradeResult {
    ok: boolean;
    score: number;
    total: number;
    summary: string;
    details: QuizGradeDetail[];
    error?: string;
}

/**
 * 生成测验题目
 * @param docId 文档ID
 * @param chapterId 章节ID（可选）
 * @param questionCount 题目数量（可选，默认5）
 * @param token 用户token
 */
export const generateQuiz = async (
    docId: number,
    chapterId: number | null,
    questionCount: number,
    token: string
): Promise<{ ok: boolean; questions?: QuizQuestion[]; count?: number; error?: string }> => {
    try {
        const res = await fetch(`${BASE_URL}/quiz/generate`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({ docId, chapterId, questionCount }),
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const json = await res.json();
        return json;
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { ok: false, error: errorMessage };
    }
};

/**
 * 批改测验答案
 * @param docId 文档ID
 * @param chapterId 章节ID（可选）
 * @param questions 题目列表
 * @param answers 用户答案列表
 * @param token 用户token
 */
export const gradeQuiz = async (
    docId: number,
    chapterId: number | null,
    questions: QuizQuestion[],
    answers: string[],
    token: string
): Promise<QuizGradeResult> => {
    try {
        const res = await fetch(`${BASE_URL}/quiz/grade`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
            },
            body: JSON.stringify({ docId, chapterId, questions, answers }),
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }

        const json = await res.json();
        return json;
    } catch (error) {
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { ok: false, score: 0, total: 0, summary: "", details: [], error: errorMessage };
    }
};
