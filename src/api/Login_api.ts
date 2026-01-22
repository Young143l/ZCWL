interface LoginResponse{
    userName:string,
    userId:string,
    token:string
}

const login = async (
    userId: string,
    password: string,
    setLogin: (userName: string, userId: string, token: string) => void,
) => {
    return fetch(import.meta.env.VITE_BACK_END, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            userId: userId,
            password: password,
        }),
    })
        .then((res:Response) => {
            if (!res.ok) {
                throw new Error(`HTTP error! status: ${res.status}`);
            }
            return res.json();
        })
        .then((json:LoginResponse) => {
            if (json && json.userName && json.userId && json.token) {
                setLogin(json.userName, json.userId, json.token);
                return { success: true, userName: json.userName }; // 返回用户名
            } else {
                throw new Error("Login failed: invalid response data");
            }
        })
        .catch((error:string|any) => {
            console.error("Login error:", error);
            return { success: false ,userName:''};
        });
};

export default login;
