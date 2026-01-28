import { create } from "zustand";
import { persist } from "zustand/middleware";

export interface AIChatBody {
    id: string;
    ask: string;
    ans: string;
    over: boolean;
}

interface AIChatDocStatus {
    have: boolean;
    id: string;
    chat: AIChatBody[];
    addChat:(i: AIChatBody)=>void;
    newChat: (id: string) =>void;
    clear: () => void;
    setAns:(id:string,ans:string)=>void;
}

const useAIChatDoc = create<AIChatDocStatus>()(
    persist(
        (set) => ({
            have: false,
            id: "",
            chat: [],
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
                }));
            },
            clear: () => {
                set((s) => ({
                    ...s,
                    have: false,
                    id: "",
                    chat: [],
                }));
            },
            setAns:(id:string,ans:string)=>{
                set((s) => ({
                    ...s,
                    chat: s.chat.map((item, index) => 
                        index === s.chat.length - 1 
                            ? {...item, id, ans,over:true} 
                            : item
                    ),
                }));
            }
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
