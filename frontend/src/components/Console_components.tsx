import { theme, Button, Tooltip } from "antd";
import { useEffect, useRef, useState, type FC } from "react";
import {
    PlayCircleOutlined,
    StopOutlined,
    ClearOutlined,
} from "@ant-design/icons";
import useIsDark from "../status/IsDark_status";

interface ConsoleProps {
    cpId: string;
    token: string;
    code: string;
}

interface WebSocketMessage {
    done: boolean;
    message?: string;
    input?: string;
}

const Console_components: FC<ConsoleProps> = ({ cpId, token, code }) => {
    const pColor = theme.useToken().token.colorPrimaryBorder;
    const { isDark } = useIsDark();
    const [output, setOutput] = useState<string[]>([]);
    const [input, setInput] = useState<string>("");
    const [isRunning, setIsRunning] = useState<boolean>(false);
    const [isConnected, setIsConnected] = useState<boolean>(false);
    const wsRef = useRef<WebSocket | null>(null);
    const outputEndRef = useRef<HTMLDivElement>(null);
    const outputContainerRef = useRef<HTMLDivElement>(null);

    // 建立 WebSocket 连接
    const connectWebSocket = () => {
        if (wsRef.current) {
            wsRef.current.close();
        }

        // 开发环境直接使用本地服务器，不走 /api 代理
        const wsBaseUrl = import.meta.env.DEV
            ? `ws://${window.location.host}`
            : "wss://cfww.young143.top";
        const wsUrl = `${wsBaseUrl}/ws/code/cp/${cpId}?token=${token}`;
        const ws = new WebSocket(wsUrl);

        ws.onopen = () => {
            setIsConnected(true);
            // 发送代码开始运行
            const message: WebSocketMessage = {
                done: false,
                input: code,
            };
            ws.send(JSON.stringify(message));
        };

        ws.onmessage = (event) => {
            const data: WebSocketMessage = JSON.parse(event.data);
            if (data.done) {
                setOutput((prev) => [
                    ...prev,
                    `\n[运行结束] ${data.message || ""}`,
                ]);
                setIsRunning(false);
                ws.close();
            } else {
                setOutput((prev) => [...prev, data.message || ""]);
            }
        };

        ws.onerror = (error) => {
            console.error("WebSocket error:", error);
            setOutput((prev) => [...prev, "[错误] WebSocket 连接失败"]);
            setIsRunning(false);
            setIsConnected(false);
        };

        ws.onclose = () => {
            setIsConnected(false);
            setIsRunning(false);
            wsRef.current = null;
        };

        wsRef.current = ws;
    };

    // 开始运行代码
    const handleRun = () => {
        if (!code.trim()) {
            setOutput((prev) => [...prev, "[提示] 代码为空"]);
            return;
        }
        setIsRunning(true);
        setOutput((prev) => [...prev, "[开始运行代码...]"]);
        connectWebSocket();
    };

    // 停止运行
    const handleStop = () => {
        if (wsRef.current && isRunning && isConnected) {
            if (wsRef.current.readyState === WebSocket.OPEN) {
                const message: WebSocketMessage = {
                    done: true,
                };
                wsRef.current.send(JSON.stringify(message));
            }
            wsRef.current.close();
            setOutput((prev) => [...prev, "\n[已手动停止]"]);
        }
        setIsRunning(false);
    };

    // 清空控制台
    const handleClear = () => {
        setOutput([]);
    };

    // 组件卸载时关闭 WebSocket
    useEffect(() => {
        return () => {
            if (wsRef.current) {
                wsRef.current.close();
            }
        };
    }, []);

    // 自动滚动到底部（仅在容器内部滚动）
    useEffect(() => {
        if (outputContainerRef.current && outputEndRef.current) {
            outputContainerRef.current.scrollTo({
                top: outputContainerRef.current.scrollHeight,
                behavior: "smooth",
            });
        }
    }, [output]);

    // 发送输入
    const handleSendInput = () => {
        if (input.trim() && wsRef.current && isRunning && isConnected) {
            if (wsRef.current.readyState === WebSocket.OPEN) {
                const message: WebSocketMessage = {
                    done: false,
                    input: input,
                };
                wsRef.current.send(JSON.stringify(message));
                setOutput((prev) => [...prev, `> ${input}`]);
                setInput("");
            } else {
                setOutput((prev) => [...prev, "[错误] WebSocket 未就绪"]);
            }
        }
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (e.key === "Enter") {
            handleSendInput();
        }
    };

    return (
        <div className="w-full h-full p-1">
            <div
                className="w-full h-full rounded-2xl border-2 flex flex-col overflow-hidden"
                style={{
                    borderColor: pColor,
                }}
            >
                {/* 工具栏 */}
                <div
                    className="flex items-center justify-between px-4 py-2 border-b"
                    style={{ borderColor: pColor }}
                >
                    <div className="font-medium">控制台</div>
                    <div className="flex gap-2">
                        <Tooltip title="运行代码">
                            <Button
                                type="primary"
                                icon={<PlayCircleOutlined />}
                                onClick={handleRun}
                                disabled={isRunning}
                                size="small"
                            >
                                运行
                            </Button>
                        </Tooltip>
                        <Tooltip title="停止运行">
                            <Button
                                danger
                                icon={<StopOutlined />}
                                onClick={handleStop}
                                disabled={!isRunning}
                                size="small"
                            >
                                停止
                            </Button>
                        </Tooltip>
                        <Tooltip title="清空控制台">
                            <Button
                                icon={<ClearOutlined />}
                                onClick={handleClear}
                                size="small"
                            />
                        </Tooltip>
                    </div>
                </div>

                {/* 输出区域 */}
                <div
                    ref={outputContainerRef}
                    className={`flex-1 overflow-auto p-4 ${isDark ? "bg-black" : "bg-white"}`}
                >
                    {output.length === 0 ? (
                        <div className="text-gray-500 text-center mt-8">
                            点击"运行"按钮开始执行代码
                        </div>
                    ) : (
                        <div className="font-mono text-sm whitespace-pre-wrap">
                            {output.map((line, index) => (
                                <div
                                    key={index}
                                    className={
                                        isDark
                                            ? "text-green-400"
                                            : "text-green-600"
                                    }
                                >
                                    {line}
                                </div>
                            ))}
                            <div ref={outputEndRef} />
                        </div>
                    )}
                </div>

                {/* 输入区域 */}
                {isRunning && (
                    <div className="px-4 py-2 border-t flex gap-2">
                        <div className="font-bold text-sm">{">"}</div>
                        <input
                            type="text"
                            className="outline-0 w-full bg-transparent"
                            placeholder="输入内容并回车发送..."
                            value={input}
                            onChange={(e) => setInput(e.target.value)}
                            onKeyDown={handleKeyDown}
                            disabled={!isRunning}
                        />
                    </div>
                )}

                {/* 状态栏 */}
                <div
                    className="px-4 py-1 text-xs border-t flex justify-between"
                    style={{ borderColor: pColor }}
                >
                    <span
                        className={
                            isConnected ? "text-green-500" : "text-gray-500"
                        }
                    >
                        {isConnected ? "● 已连接" : "○ 未连接"}
                    </span>
                    <span className="text-gray-500">
                        {isRunning ? "运行中..." : "就绪"}
                    </span>
                </div>
            </div>
        </div>
    );
};

export default Console_components;
