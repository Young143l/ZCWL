import { useEffect, useState, type FC } from "react";
import useLogin from "../status/Login_status";
interface HS {
    AllCmd: string;
}

const Hello_Sum_components: FC<HS> = ({ AllCmd }) => {
    const { userName, isLogin } = useLogin();

    const [cmd, setCmd] = useState<string>("");
    useEffect(() => {
        setCmd("");

        const timers: number[] = [];

        for (let i = 0; i < AllCmd.length; i++) {
            const timer = window.setTimeout(() => {
                setCmd((prev) => prev + AllCmd[i]);
            }, 240 * i);

            timers.push(timer);
        }

        return () => {
            timers.forEach((timer) => clearTimeout(timer));
        };
    }, [isLogin]);

    return (
        <div className="md:w-full w-[calc(100%-40px)] h-[calc(100vh-64px-42px)] mb-10  rounded-xl">
            <div className="bg-[#F8F8F875] rounded-xl border-2 border-white overflow-hidden h-full backdrop-blur-sm">
                <div className="bg-white px-4 py-3 flex items-center">
                    <div className="w-3 h-3 rounded-full bg-red-500 mr-2"></div>
                    <div className="w-3 h-3 rounded-full bg-yellow-500 mr-2"></div>
                    <div className="w-3 h-3 rounded-full bg-green-500"></div>
                </div>
                <div className="font-mono text-xl p-4">
                    {`${isLogin ? userName : "Tourists"}@Cfww:~$ ` +
                        cmd +
                        ((cmd.length + (AllCmd.length % 2)) % 2 == 0
                            ? ""
                            : "|")}
                </div>
            </div>
        </div>
    );
};

export default Hello_Sum_components;
