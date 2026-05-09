import { useState, useEffect, useRef, useCallback } from "react";
import { startLearning, updateProgress, endLearning } from "../api/Learning_api";

interface LearningSession {
    docId: number;
    chapterId: number | null;
    startTime: number;
    lastUpdateTime: number;
}

const useLearning = (token: string | null) => {
    const [currentSession, setCurrentSession] = useState<LearningSession | null>(null);
    const [isLearning, setIsLearning] = useState(false);
    const [sessionDuration, setSessionDuration] = useState(0);
    
    const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
    const sessionRef = useRef<LearningSession | null>(null);

    useEffect(() => {
        sessionRef.current = currentSession;
    }, [currentSession]);

    const beginLearning = useCallback(async (docId: number, chapterId: number | null) => {
        if (!token) return;

        try {
            const res = await startLearning(docId, chapterId, token);
            if (res.success) {
                const now = Date.now();
                const newSession = {
                    docId,
                    chapterId,
                    startTime: now,
                    lastUpdateTime: now,
                };
                setCurrentSession(newSession);
                sessionRef.current = newSession;
                setIsLearning(true);
                setSessionDuration(0);
            }
        } catch (error) {
            console.error("Error starting learning:", error);
        }
    }, [token]);

    const updateLearningProgress = useCallback(async (progress: number, position: number) => {
        if (!token || !sessionRef.current) return;

        try {
            await updateProgress(
                sessionRef.current.docId,
                sessionRef.current.chapterId,
                progress,
                position,
                token
            );
        } catch (error) {
            console.error("Error updating progress:", error);
        }
    }, [token]);

    const finishLearning = useCallback(async () => {
        if (!token || !sessionRef.current) return;

        try {
            await endLearning(
                sessionRef.current.docId,
                sessionRef.current.chapterId,
                token
            );
        } catch (error) {
            console.error("Error ending learning:", error);
        }

        setCurrentSession(null);
        sessionRef.current = null;
        setIsLearning(false);
        setSessionDuration(0);
    }, [token]);

    useEffect(() => {
        if (isLearning && currentSession) {
            timerRef.current = setInterval(() => {
                setSessionDuration(Math.floor((Date.now() - currentSession.startTime) / 1000));
            }, 1000);
        }

        return () => {
            if (timerRef.current) {
                clearInterval(timerRef.current);
            }
        };
    }, [isLearning, currentSession]);

    useEffect(() => {
        return () => {
            if (sessionRef.current && token) {
                endLearning(sessionRef.current.docId, sessionRef.current.chapterId, token);
            }
        };
    }, [token]);

    return {
        isLearning,
        sessionDuration,
        currentSession,
        beginLearning,
        updateLearningProgress,
        finishLearning,
    };
};

export default useLearning;
