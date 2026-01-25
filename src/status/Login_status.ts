import { create } from "zustand";
import { persist } from "zustand/middleware";

interface LoginState {
    isLogin: boolean;
    userName: string;
    userId: string;
    token: string;
    setLoginStatus: (userName: string, userId: string, token: string) => void;
    clearLoginStatus: () => void;
}

const useLogin = create<LoginState>()(
    persist(
        (set) => ({
            isLogin: false,
            userName: "",
            userId: "",
            token: "",
            setLoginStatus: (userName, userId, token) => {
                set((state) => ({
                    ...state,
                    userName: userName,
                    token: token,
                    userId: userId,
                    isLogin: true,
                }));
            },
            clearLoginStatus: () => {
                set((state) => ({
                    ...state,
                    token: "",
                    userName: "",
                    userId: "",
                    isLogin: false,
                }));
            },
        }),
        {
            name: "loginStatus",
            partialize: (state) => ({
                isLogin: state.isLogin,
                userName: state.userName,
                userId: state.userId,
                token: state.token,
            }),
        },
    ),
);

export default useLogin;
