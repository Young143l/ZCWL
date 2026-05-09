import { useEffect, useMemo, useState, type FC } from "react";
import { flushSync } from "react-dom";
import Icon from "@ant-design/icons";
import useLogin from "../status/Login_status";
import MessageIcon from "../../public/message.svg?react";
import TimeIcon from "../../public/time.svg?react";
import WebIcon from "../../public/web.svg?react";
import FileIcon from "../../public/file.svg?react";
import { cdn, isCdn } from "../config/cdn";
import useIsDark from "../status/IsDark_status";

interface HS {
    AllCmd: string;
    title: string;
}

const aWords: string[] = [
    "The future is not something that just happens to us. We create it by what we do.",
    "Technology is best when it brings people together.",
    "Simplicity is the ultimate form of sophistication.",
    "The computer was born to solve problems that did not exist before.",
    "Code is like humor. When you have to explain it, it's bad.",
    "Perfection is achieved not when there is nothing more to add, but when there is nothing left to take away.",
    "It's not that I'm so smart, it's just that I stay with problems longer.",
    "The only way to do great work is to love what you do.",
    "In three words I can sum up everything I've learned about life: it goes on.",
    "Optimism is the faith that leads to achievement. Nothing can be done without hope and confidence.",
];

// 使用固定的随机索引，避免在渲染时调用 Math.random()
const randomWordIndex = Math.floor(Math.random() * 10);

const Hello_Sum_components: FC<HS> = ({ AllCmd, title }) => {
    const { userName, isLogin } = useLogin();
    const [cmd, setCmd] = useState<string>("");
    const [time, setTime] = useState<string>(() => {
        return new Date().toLocaleTimeString();
    });
    const aWord = aWords[randomWordIndex];
    const [show, setShow] = useState<boolean>(false);
    const {isDark}=useIsDark()
    useEffect(() => {
        // 每秒更新一次时间
        const timer = setInterval(() => {
            setTime(new Date().toLocaleTimeString());
        }, 1000);

        // 组件卸载时清除定时器
        return () => clearInterval(timer);
    }, []);
    const proFile = useMemo(() => {
        switch (title) {
            case "Home":
                return "Welcome to Cfww !";
            case "Document":
                return "Clean Docs,Clear Minds.";
            case "Project":
                return "Learning from project !";
            case "Code":
                return "Coding !";
        }
    }, [title]);

    useEffect(() => {
        // 重置 cmd 和 show 状态（使用 flushSync 确保同步更新）
        flushSync(() => {
            setCmd("");
            setShow(false);
        });

        const timers: number[] = [];

        for (let i = 0; i < AllCmd.length + 1; i++) {
            if (i == AllCmd.length) {
                const timer = window.setTimeout(
                    () => {
                        setShow(true);
                    },
                    180 * (i - 1) + 100,
                );

                timers.push(timer);
            } else {
                const timer = window.setTimeout(() => {
                    setCmd((prev) => prev + AllCmd[i]);
                }, 180 * i);

                timers.push(timer);
            }
        }

        return () => {
            timers.forEach((timer) => clearTimeout(timer));
        };
    }, [isLogin, AllCmd]);

    return (
        <div className="w-full h-[calc(100vh-64px-40px)] mb-8 rounded-xl select-none">
            <div className={`flex flex-col bg-[${isDark?"1E1E1E75":"#F8F8F875"}] rounded-xl border-4 border-${isDark?"black":"white"} overflow-hidden h-full backdrop-blur-sm`}>
                <div className={`bg-${isDark?"black":"white"} px-4 py-3 flex items-center`}>
                    <div className="w-3 h-3 rounded-full bg-red-500 mr-2"></div>
                    <div className="w-3 h-3 rounded-full bg-yellow-500 mr-2"></div>
                    <div className="w-3 h-3 rounded-full bg-green-500"></div>
                </div>
                <div className={`font-mono text-2xl p-4 ${isDark?"text-white":"text-black"}`}>
                    {`${isLogin ? userName : "Tourists"}@Cfww:~$ ` +
                        cmd +
                        ((cmd.length + (AllCmd.length % 2)) % 2 == 0
                            ? ""
                            : "|")}
                </div>
                <div
                    className={`flex-1 w-full flex justify-center items-center   transition-all duration-200 ease-linear overflow-hidden ${show ? "max-h-full" : "max-h-0"}`}
                >
                    <div className="flex flex-col md:flex-row gap-6 md:gap-8 items-center md:items-start w-full max-w-5xl p-2">
                        <div className="w-full md:w-auto flex justify-center md:justify-start shrink-0">
                            <video
                                className={`w-48 h-48 md:w-64 md:h-64 object-cover rounded-xl  border ${isDark?"border-black":"border-white"}`}
                                // controls
                                autoPlay
                                loop
                                muted
                                poster={(isCdn?cdn:"../../public/")+`${title.toLowerCase()}.jpg`}
                            >
                                <source src={(isCdn?cdn:"../../public/")+`${title.toLowerCase()}.mp4`} type="video/mp4" />
                            </video>
                        </div>

                        <div className="flex-1 min-w-0 space-y-3 w-full ">
                            <div className="flex items-center gap-2 break-all">
                                <Icon
                                    component={WebIcon}
                                    className="text-xl md:text-[30px] shrink-0"
                                />
                                <span
                                    className="whitespace-pre font-bold shrink-0 text-xl md:text-[30px]"
                                    style={{
                                        color: "#8FD1EF",
                                    }}
                                >
                                    {"Title      >"}
                                </span>
                                <span className="font-bold text-gray-500 truncate text-xl md:text-[30px]">
                                    {title}
                                </span>
                            </div>
                            <div className="flex items-center gap-2 break-all">
                                <Icon
                                    component={FileIcon}
                                    className="text-xl md:text-[30px] shrink-0"
                                />
                                <span
                                    className="whitespace-pre font-bold shrink-0 text-xl md:text-[30px]"
                                    style={{
                                        color: "#C2DCB1",
                                    }}
                                >
                                    {"Profile   >"}
                                </span>
                                <span className="font-bold text-gray-500 truncate text-xl md:text-[30px]">
                                    {proFile}
                                </span>
                            </div>

                            <div className="flex items-center gap-2 break-all">
                                <Icon
                                    component={TimeIcon}
                                    className="text-xl md:text-[30px] shrink-0"
                                />
                                <span
                                    className="whitespace-pre font-bold shrink-0 text-xl md:text-[30px]"
                                    style={{
                                        color: "#FFB6C1",
                                    }}
                                >
                                    {"Time      >"}
                                </span>
                                <span className="font-bold text-gray-500 text-xl md:text-[30px]">
                                    {time}
                                </span>
                            </div>
                            <div className="flex items-start gap-2 break-all">
                                <div className="shrink-0">
                                    <Icon
                                        component={MessageIcon}
                                        className="text-xl md:text-[30px] shrink-0"
                                    />
                                    <span
                                        className="whitespace-pre pl-2 font-bold  text-xl md:text-[30px]"
                                        style={{
                                            color: "#FF6095",
                                        }}
                                    >
                                        {"A Word >"}
                                    </span>
                                </div>
                                <span className="font-bold text-gray-500 leading-relaxed text-xl md:text-[30px]">
                                    {aWord}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Hello_Sum_components;
