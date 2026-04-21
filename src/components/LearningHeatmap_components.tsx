import { useEffect, useState, type FC } from "react";
import { getLearningHeatmap, type HeatmapData } from "../api/Learning_api";
import useLogin from "../status/Login_status";
import { Card, Spin, Tooltip } from "antd";
import useIsDark from "../status/IsDark_status";

const LearningHeatmap_components: FC = () => {
    const { token, isLogin } = useLogin();
    const { isDark } = useIsDark();
    const [heatmapData, setHeatmapData] = useState<HeatmapData[]>([]);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        if (!isLogin || !token) {
            // 使用 setTimeout 避免同步调用 setState
            const timer = setTimeout(() => {
                setLoading(false);
            }, 0);
            return () => clearTimeout(timer);
        }

        let cancelled = false;

        const fetchHeatmap = async () => {
            const res = await getLearningHeatmap(token);
            if (cancelled) return;
            if (res.success && res.data) {
                setHeatmapData(res.data);
            }
            setLoading(false);
        };

        fetchHeatmap();

        return () => {
            cancelled = true;
        };
    }, [token, isLogin]);

    // 按周分组数据
    const getWeeksData = () => {
        const weeks: HeatmapData[][] = [];
        const dataMap = new Map(heatmapData.map(d => [d.date, d]));

        // 获取最近365天的数据
        const today = new Date();
        const endDate = new Date(today);
        const startDate = new Date(today);
        startDate.setDate(startDate.getDate() - 364); // 364天前到今天共365天

        // 找到开始日期的那个周日（或者周一）
        // 让周一在第一行，周日在最后一行
        const firstDay = new Date(startDate);
        const dayOfWeek = firstDay.getDay(); // 0=周日, 1=周一, ...
        // 调整到最近的周一（如果今天是周日，则往前推6天到上周一）
        const daysToMonday = dayOfWeek === 0 ? 6 : dayOfWeek - 1;
        firstDay.setDate(firstDay.getDate() - daysToMonday);

        const currentDate = new Date(firstDay);

        while (currentDate <= endDate) {
            const week: HeatmapData[] = [];
            for (let i = 0; i < 7; i++) {
                const dateStr = currentDate.toISOString().split('T')[0];
                const dayData = dataMap.get(dateStr) || { date: dateStr, count: 0, level: 0 };
                week.push(dayData);
                currentDate.setDate(currentDate.getDate() + 1);
            }
            weeks.push(week);
        }

        return weeks;
    };

    const weeks = getWeeksData();

    const getColor = (level: number) => {
        if (isDark) {
            // 暗色主题
            switch (level) {
                case 0: return '#21262d';  // 无学习
                case 1: return '#0e4429';  // 轻度
                case 2: return '#006d32';  // 中等
                case 3: return '#26a641';  // 较高
                case 4: return '#39d353';  // 活跃
                default: return '#21262d';
            }
        } else {
            // 亮色主题
            switch (level) {
                case 0: return '#ebedf0';  // 无学习
                case 1: return '#9be9a8';  // 轻度
                case 2: return '#40c463';  // 中等
                case 3: return '#30a14e';  // 较高
                case 4: return '#216e39';  // 活跃
                default: return '#ebedf0';
            }
        }
    };

    const getLevelText = (level: number) => {
        switch (level) {
            case 0: return '无学习';
            case 1: return '轻度学习';
            case 2: return '中等学习';
            case 3: return '较高学习';
            case 4: return '活跃学习';
            default: return '无学习';
        }
    };

    const getMonthLabels = () => {
        const months: { index: number; label: string }[] = [];
        const monthNames = ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];

        weeks.forEach((week, weekIndex) => {
            // 取每周的中间那天来判断月份
            const middleDay = week[3]; // 周四
            const date = new Date(middleDay.date);
            const month = date.getMonth();

            // 检查是否是该月的第一个周
            const prevWeek = weekIndex > 0 ? weeks[weekIndex - 1] : null;
            const prevMonth = prevWeek ? new Date(prevWeek[3].date).getMonth() : -1;

            if (month !== prevMonth) {
                months.push({ index: weekIndex, label: monthNames[month] });
            }
        });

        return months;
    };

    const calculateStats = () => {
        const totalDays = heatmapData.length;
        const activeDays = heatmapData.filter(d => d.count > 0).length;
        const maxLevel = Math.max(...heatmapData.map(d => d.level), 0);
        const totalCount = heatmapData.reduce((sum, d) => sum + d.count, 0);

        return { totalDays, activeDays, maxLevel, totalCount };
    };

    const stats = calculateStats();
    const monthLabels = getMonthLabels();

    if (loading) {
        return (
            <Card title="学习热力图" className="w-full">
                <div className="flex justify-center items-center h-48">
                    <Spin />
                </div>
            </Card>
        );
    }

    return (
        <Card
            title={
                <div className="flex justify-between items-center">
                    <span>学习热力图</span>
                    <span className="text-sm text-gray-500 font-normal">
                        过去一年的学习活跃度
                    </span>
                </div>
            }
            className="w-full"
        >
            {/* 统计信息 */}
            <div className="flex gap-6 mb-4 text-sm">
                <div>
                    <span className="text-gray-500">活跃天数: </span>
                    <span className="font-semibold">{stats.activeDays}</span>
                </div>
                <div>
                    <span className="text-gray-500">总学习次数: </span>
                    <span className="font-semibold">{stats.totalCount}</span>
                </div>
            </div>

            {/* 热力图 */}
            <div className="overflow-x-auto pb-2">
                <div className="inline-block min-w-full">
                    {/* 月份标签 */}
                    <div className="flex mb-2" style={{ marginLeft: '32px', height: '20px' }}>
                        {monthLabels.map((m) => (
                            <div
                                key={m.index}
                                className="text-xs text-gray-500 absolute"
                                style={{
                                    left: `${m.index * 18 + 32}px`, // 18px是每周的宽度(14+4间隙)
                                    whiteSpace: 'nowrap',
                                }}
                            >
                                {m.label}
                            </div>
                        ))}
                    </div>

                    <div className="flex" style={{ marginTop: '24px' }}>
                        {/* 星期标签 */}
                        <div className="flex flex-col justify-around mr-2 text-xs text-gray-500" style={{ height: '112px' }}>
                            <span style={{ height: '14px', lineHeight: '14px' }}>一</span>
                            <span style={{ height: '14px', lineHeight: '14px' }}>三</span>
                            <span style={{ height: '14px', lineHeight: '14px' }}>五</span>
                        </div>

                        {/* 热力格子 */}
                        <div className="flex gap-1">
                            {weeks.map((week, weekIndex) => (
                                <div key={weekIndex} className="flex flex-col gap-1">
                                    {week.map((day, dayIndex) => (
                                        <Tooltip
                                            key={`${weekIndex}-${dayIndex}`}
                                            title={`${day.date}: ${getLevelText(day.level)} (${day.count}次)`}
                                        >
                                            <div
                                                className="w-3.5 h-3.5 rounded-sm cursor-pointer transition-all hover:ring-2 hover:ring-blue-400"
                                                style={{
                                                    backgroundColor: getColor(day.level),
                                                }}
                                            />
                                        </Tooltip>
                                    ))}
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* 图例 */}
                    <div className="flex items-center gap-2 mt-4 text-xs text-gray-500">
                        <span>少</span>
                        {[0, 1, 2, 3, 4].map((level) => (
                            <div
                                key={level}
                                className="w-3.5 h-3.5 rounded-sm"
                                style={{ backgroundColor: getColor(level) }}
                            />
                        ))}
                        <span>多</span>
                    </div>
                </div>
            </div>
        </Card>
    );
};

export default LearningHeatmap_components;
