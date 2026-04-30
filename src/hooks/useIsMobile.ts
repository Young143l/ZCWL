import { useState, useEffect } from "react";

const MOBILE_BREAKPOINT = 768;

/**
 * Hook to detect if the current viewport is mobile-sized
 * @returns boolean - true if viewport width is less than 768px
 */
const useIsMobile = () => {
    const [isMobile, setIsMobile] = useState<boolean>(() => {
        if (typeof window === "undefined") return false;
        return window.innerWidth < MOBILE_BREAKPOINT;
    });

    useEffect(() => {
        const handleResize = () => {
            setIsMobile(window.innerWidth < MOBILE_BREAKPOINT);
        };

        // Set initial value
        handleResize();

        // Add event listener with passive option for better scroll performance
        window.addEventListener("resize", handleResize, { passive: true });

        return () => {
            window.removeEventListener("resize", handleResize);
        };
    }, []);

    return isMobile;
};

export const MOBILE_BREAKPOINT_WIDTH = MOBILE_BREAKPOINT;

export default useIsMobile;
