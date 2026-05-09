import { create } from "zustand";
import { persist } from "zustand/middleware";

interface LoginState {
    isLogin: boolean;
    userName: string;
    userId: string;
    token: string;
    avatar:string;
    email:string;
    setLoginStatus: (userName: string, userId: string, token: string,avatar:string,email:string) => void;
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
            email:"",
            setLoginStatus: (userName, userId, token,avatar,email) => {
                set((state) => ({
                    ...state,
                    userName: userName,
                    token: token,
                    userId: userId,
                    avatar:avatar,
                    email:email,
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
                    email:"",
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
                avatar:state.avatar,
                email:state.email
            }),
        },
    ),
);

export default useLogin;
