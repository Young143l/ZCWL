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

    // 计算单元格大小（响应式）
    const getCellSize = () => {
        if (typeof window !== 'undefined' && window.innerWidth < 640) {
            return 10; // 小屏 10px
        }
        return 14;
    };

    const cellSize = getCellSize();
    const gap = Math.max(2, Math.min(4, cellSize < 12 ? 2 : 4)); // 间距随单元格自适应
    const rowHeight = cellSize + gap; // 每行占用的总高度（行高 = 单元格 + 间距）
    const weekWidth = cellSize + gap; // 每周总宽度

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
        // 窄屏使用简短标签（如 "1" 代替 "1月"），宽屏使用完整标签
        const isNarrow = typeof window !== 'undefined' && window.innerWidth < 640;
        const monthNames = isNarrow
            ? ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12']
            : ['1月', '2月', '3月', '4月', '5月', '6月', '7月', '8月', '9月', '10月', '11月', '12月'];

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
                <div className="flex justify-between items-center flex-wrap gap-1">
                    <span>学习热力图</span>
                    <span className="text-xs sm:text-sm text-gray-500 font-normal">
                        过去一年的学习活跃度
                    </span>
                </div>
            }
            className="w-full"
        >
            {/* 统计信息 */}
            <div className="flex flex-wrap gap-3 sm:gap-6 mb-4 text-xs sm:text-sm">
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
            <div className="overflow-x-auto pb-2 -mx-2 px-2">
                <div className="inline-block">
                    {/* 月份标签 + 格子区域 flex row */}
                    <div className="flex">
                        {/* 星期标签列 - 使用与网格行一致的高度 */}
                        <div
                            className="flex flex-col items-center mr-1 sm:mr-2 text-[10px] sm:text-xs text-gray-500 shrink-0 justify-start pt-0"
                            style={{ paddingTop: '18px' /* 与月份标签行高度对齐 */ }}
                        >
                            {/* 周一：第0行，高度 = rowHeight（包含间隙） */}
                            <span
                                style={{
                                    height: `${rowHeight}px`,
                                    lineHeight: `${rowHeight}px`,
                                    display: 'block',
                                }}
                            >
                                一
                            </span>
                            {/* 周三：第2行 */}
                            <span
                                style={{
                                    height: `${rowHeight}px`,
                                    lineHeight: `${rowHeight}px`,
                                    display: 'block',
                                }}
                            >
                                三
                            </span>
                            {/* 周五：第4行，最后一行不需要底部间隙，但统一使用 rowHeight 保持对齐到单元格中心 */}
                            <span
                                style={{
                                    height: `${rowHeight}px`,
                                    lineHeight: `${rowHeight}px`,
                                    display: 'block',
                                }}
                            >
                                五
                            </span>
                        </div>

                        {/* 右侧区域：月份标签 + 格子 */}
                        <div className="flex flex-col">
                            {/* 月份标签 - 每个标签宽度 = weekWidth，与网格列对齐 */}
                            <div className="flex gap-1.5" style={{ height: '18px', marginBottom: '2px' }}>
                                {monthLabels.map((m) => (
                                    <div
                                        key={m.index}
                                        className="text-[10px] sm:text-xs text-gray-500 whitespace-nowrap"
                                        style={{
                                            width: `${weekWidth}px`,
                                            overflow: 'visible',
                                        }}
                                    >
                                        {m.label}
                                    </div>
                                ))}
                            </div>

                            {/* 热力格子 - 每列使用 gap 布局 */}
                            <div className="flex">
                                {weeks.map((week, weekIndex) => (
                                    <div
                                        key={weekIndex}
                                        className="flex flex-col"
                                        style={{
                                            gap: `${gap}px`,
                                            marginRight: weekIndex < weeks.length - 1 ? `${gap}px` : '0',
                                        }}
                                    >
                                        {week.map((day, dayIndex) => (
                                            <Tooltip
                                                key={`${weekIndex}-${dayIndex}`}
                                                title={`${day.date}: ${getLevelText(day.level)} (${day.count}次)`}
                                            >
                                                <div
                                                    className="rounded-sm cursor-pointer transition-all hover:ring-2 hover:ring-blue-400 shrink-0"
                                                    style={{
                                                        width: `${cellSize}px`,
                                                        height: `${cellSize}px`,
                                                        backgroundColor: getColor(day.level),
                                                    }}
                                                />
                                            </Tooltip>
                                        ))}
                                    </div>
                                ))}
                            </div>
                        </div>
                    </div>

                    {/* 图例 */}
                    <div className="flex items-center gap-1 sm:gap-2 mt-3 text-[10px] sm:text-xs text-gray-500">
                        <span>少</span>
                        {[0, 1, 2, 3, 4].map((level) => (
                            <div
                                key={level}
                                className="rounded-sm"
                                style={{
                                    width: `${cellSize}px`,
                                    height: `${cellSize}px`,
                                    backgroundColor: getColor(level),
                                }}
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
