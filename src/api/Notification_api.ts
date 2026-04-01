// 通知接口

// 通知类型定义
export interface Notification {
    nId: string;      // 通知ID
    dId: string;      // 文档ID
    cId: string;      // 评论ID
    from: string;     // 回复用户
    time: string;     // 时间
    content: string;  // 内容
}

// 获取用户未读评论消息响应
export interface GetNotificationsResponse {
    uId: string;
    notifications: Notification[];
}

/**
 * 获取用户未读评论消息
 * @param uId - 用户ID
 * @param token - 认证令牌
 * @returns 包含未读通知列表的Promise
 */
export const getNotifications = async (
    uId: string,
    token: string
): Promise<{ success: boolean; data?: GetNotificationsResponse; message?: string }> => {
    try {
        const res: Response = await fetch(
            `${import.meta.env.VITE_BACK_END}/notification/?uId=${uId}`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                },
            }
        );

        if (!res.ok) {
            const errorData = await res.json().catch(() => ({}));
            throw new Error(errorData.message || `HTTP error! status: ${res.status}`);
        }

        const json: GetNotificationsResponse = await res.json();
        return { success: true, data: json };
    } catch (error: unknown) {
        console.error("Get notifications error:", error);
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, message: errorMessage };
    }
};

/**
 * 已读某通知
 * @param nId - 通知ID
 * @param token - 认证令牌
 * @returns 标记已读结果的Promise
 */
export const readNotification = async (
    nId: string,
    token: string
): Promise<{ success: boolean; message?: string }> => {
    try {
        const res: Response = await fetch(
            `${import.meta.env.VITE_BACK_END}/notification/${nId}`,
            {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                },
            }
        );

        if (!res.ok) {
            const errorData = await res.json().catch(() => ({}));
            throw new Error(errorData.message || `HTTP error! status: ${res.status}`);
        }

        // 成功时无json返回
        return { success: true };
    } catch (error: unknown) {
        console.error("Read notification error:", error);
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, message: errorMessage };
    }
};
