import { create } from "zustand";
import { persist } from "zustand/middleware";

export interface AIChatBody {
    id: string;
    ask: string;
    ans: string;
    over: boolean;
}

interface AIChatDocStatus {
    isAIChatOpen: boolean;
    setIsAIChatOpen: (i: boolean) => void;
    have: boolean;
    id: string;
    chat: AIChatBody[];
    code: string | null;
    setCode: (code: string | null) => void;
    addChat: (i: AIChatBody) => void;
    newChat: (id: string) => void;
    clear: () => void;
    setAns: (id: string, ans: string) => void;
}

const useAIChatDoc = create<AIChatDocStatus>()(
    persist(
        (set) => ({
            isAIChatOpen: false,
            have: false,
            id: "",
            chat: [],
            code: null,
            setIsAIChatOpen: (i: boolean) => {
                set((s) => ({
                    ...s,
                    isAIChatOpen: i,
                }));
            },
            setCode: (code: string | null) => {
                set((s) => ({
                    ...s,
                    code: code,
                }));
            },
            addChat: (i: AIChatBody) => {
                set((s) => ({
                    ...s,
                    chat: [...s.chat, i],
                }));
            },
            newChat: (id: string) => {
                set((s) => ({
                    ...s,
                    have: true,
                    id: id,
                    chat: [],
                    code: null,
                }));
            },
            clear: () => {
                set((s) => ({
                    ...s,
                    have: false,
                    id: "",
                    chat: [],
                    code: null,
                }));
            },
            setAns: (id: string, ans: string) => {
                set((s) => ({
                    ...s,
                    chat: s.chat.map((item, index) =>
                        index === s.chat.length - 1
                            ? { ...item, id, ans, over: true }
                            : item,
                    ),
                }));
            },
        }),
        {
            name: "AIChatDocStatus",
            partialize: (state) => ({
                have: state.have,
                id: state.id,
                chat: state.chat,
            }),
        },
    ),
);

export default useAIChatDoc;
