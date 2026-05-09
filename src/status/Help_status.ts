import { create } from "zustand";
import { persist } from "zustand/middleware";

interface Help {
    isOpen: boolean;
    setIsOpen: (i: boolean) => void;
    first: boolean;
    setFirst: (i: boolean) => void;
}

const useHelp = create<Help>()(
    persist(
        (set) => ({
            isOpen: false,
            first: true,
            setIsOpen: (i) => {
                set((prev) => ({
                    ...prev,
                    isOpen: i,
                }));
            },
            setFirst: (i) => {
                set((prev) => ({
                    ...prev,
                    first: i,
                }));
            },
        }),
        {
            name: "Help",
            partialize: (state) => ({
                first: state.first,
            }),
        },
    ),
);

export default useHelp;
