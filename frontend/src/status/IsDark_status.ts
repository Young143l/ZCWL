import { create } from "zustand";
import { persist } from "zustand/middleware";

interface IsDarkState {
    isDark: boolean;
    toggleDark: () => void;
    setIsDark: (isDark: boolean) => void;
}

const useIsDark = create<IsDarkState>()(
    persist(
        (set) => ({
            isDark: false,
            toggleDark: () => {
                set((state) => ({
                    isDark: !state.isDark,
                }));
            },
            setIsDark: (isDark: boolean) => {
                set({ isDark });
            },
        }),
        {
            name: "isDarkMode",
            partialize: (state) => ({
                isDark: state.isDark,
            }),
        },
    ),
);

export default useIsDark;