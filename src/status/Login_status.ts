import { create } from "zustand";
import { persist } from "zustand/middleware";

interface LoginState {
    isLogin: boolean;
    userName: string;
    userId: string;
    token: string;
    avatar:string;
    setLoginStatus: (userName: string, userId: string, token: string,avatar:string) => void;
    clearLoginStatus: () => void;
}

const useLogin = create<LoginState>()(
    persist(
        (set) => ({
            isLogin: false,
            userName: "",
            userId: "",
            token: "",
            avatar:"",
            setLoginStatus: (userName, userId, token,avatar) => {
                set((state) => ({
                    ...state,
                    userName: userName,
                    token: token,
                    userId: userId,
                    avatar:avatar,
                    isLogin: true,
                }));
            },
            clearLoginStatus: () => {
                set((state) => ({
                    ...state,
                    token: "",
                    userName: "",
                    userId: "",
                    avatar:"",
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
                avatar:state.avatar
            }),
        },
    ),
);

export default useLogin;
