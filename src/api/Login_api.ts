interface LoginResponse {
    avatar:string,
    userName: string;
    userId: string;
    token: string;
}

const login: (
    userId: string,
    password: string,
    setLogin: (userName: string, userId: string, token: string,avatar:string) => void,
) => Promise<{ success: boolean; userName: string }> = async (
    userId: string,
    password: string,
    setLogin: (userName: string, userId: string, token: string,avatar:string) => void,
) => {
    try {
        const res: Response = await fetch(import.meta.env.VITE_BACK_END+"/users/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                userId: userId,
                password: password,
            }),
        });

        if (!res.ok) {
            throw new Error(`HTTP error! status: ${res.status}`);
        }
        const json: LoginResponse = await res.json();
        if (json && json.userName && json.userId && json.token) {
            setLogin(json.userName, json.userId, json.token,json.avatar);
            console.log(json);
            return { success: true, userName: json.userName };
        } else {
            throw new Error("Login failed: invalid response data");
        }
    } catch (error: unknown) {
        console.error("Login error:", error);
        return { success: false, userName: "" };
    }
};

export default login;
