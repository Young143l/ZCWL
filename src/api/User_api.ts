import { Md5 } from "ts-md5";

interface LoginResponse {
    userName: string;
    userId: string;
    token: string;
    email: string;
}

interface GetUserInfoResponse {
    name: string;
    email: string;
}

export const login: (
    userId: string,
    password: string,
    setLogin: (
        userName: string,
        userId: string,
        token: string,
        avatar: string,
        email:string
    ) => void,
) => Promise<{ success: boolean; userName: string }> = async (
    userId: string,
    password: string,
    setLogin: (
        userName: string,
        userId: string,
        token: string,
        avatar: string,
        email:string
    ) => void,
) => {
    try {
        const res: Response = await fetch(
            import.meta.env.VITE_BACK_END + "/users/login",
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    userId: userId,
                    password: password,
                }),
            },
        );

        if (!res.ok) {
            // const json = await res.json();
            // console.log(json)
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: LoginResponse = await res.json();
        console.log(json);
        if (json && json.userName && json.userId && json.token) {
            setLogin(
                json.userName,
                json.userId,
                json.token,
                `https://cravatar.cn/avatar/${Md5.hashStr(json.email)}`,
                json.email
            );
            // console.log(json);
            return { success: true, userName: json.userName };
        } else {
            throw new Error("Login failed: invalid response data");
        }
    } catch (error: unknown) {
        console.error("Login error:", error);
        return { success: false, userName: "" };
    }
};

export const SigninUser = async (
    name: string,
    mail: string,
    password: string,
) => {
    try {
        const res = await fetch(import.meta.env.VITE_BACK_END + "/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                userName: name,
                email: mail,
                password: password,
            }),
        });
        // console.log(res)
        if (!res.ok) {
            // const json = await res.json();
            // console.log(json)
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json = await res.json();
        if (json) {
            return { success: true };
        } else {
            throw new Error("Signin failed: invalid response data");
        }
    } catch (error: unknown) {
        console.error("Login error:", error);
        return { success: false, message: error };
    }
};

export const updateUserInfo = async (
    userId: string,
    token: string,
    name: string,
    email: string
): Promise<{ success: boolean; data?: { name: string; email: string }; message?: string }> => {
    try {
        const res: Response = await fetch(
            `${import.meta.env.VITE_BACK_END}/users/${userId}`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                },
                body: JSON.stringify({
                    name: name,
                    email: email,
                }),
            }
        );

        if (!res.ok) {
            const errorData = await res.json().catch(() => ({}));
            throw new Error(errorData.message || `HTTP error! status: ${res.status}`);
        }

        const json = await res.json();
        return { success: true, data: json };
    } catch (error: unknown) {
        console.error("Update user info error:", error);
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, message: errorMessage };
    }
};

export const updatePassword = async (
    userId: string,
    token: string,
    password: string,
    newPassword: string
): Promise<{ success: boolean; message?: string }> => {
    try {
        const res: Response = await fetch(
            `${import.meta.env.VITE_BACK_END}/users/password/${userId}`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}`,
                },
                body: JSON.stringify({
                    password: password,
                    newPassword: newPassword,
                }),
            }
        );

        const json = await res.json();

        if (!res.ok) {
            // 失败时返回后端提供的错误消息
            return { success: false, message: json.message || `HTTP error! status: ${res.status}` };
        }

        // 成功时根据文档返回格式，虽然文档示例有点奇怪（返回原密码），这里主要关注 success 状态
        return { success: true };
    } catch (error: unknown) {
        console.error("Update password error:", error);
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, message: errorMessage };
    }
};

/**
 * 获取用户信息
 * @param userId - 用户ID
 * @param token - 认证令牌
 * @returns 包含用户信息的Promise
 */
export const getUserInfo = async (
    userId: string,
    token: string
): Promise<{ success: boolean; data?: GetUserInfoResponse; message?: string }> => {
    try {
        const res: Response = await fetch(
            `${import.meta.env.VITE_BACK_END}/users/${userId}`,
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

        const json: GetUserInfoResponse = await res.json();
        return { success: true, data: json };
    } catch (error: unknown) {
        console.error("Get user info error:", error);
        const errorMessage = error instanceof Error ? error.message : "Unknown error";
        return { success: false, message: errorMessage };
    }
};
