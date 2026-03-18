import { Md5 } from "ts-md5";
interface LoginResponse {
    userName: string;
    userId: string;
    token: string;
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
    ) => void,
) => Promise<{ success: boolean; userName: string }> = async (
    userId: string,
    password: string,
    setLogin: (
        userName: string,
        userId: string,
        token: string,
        avatar: string,
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
