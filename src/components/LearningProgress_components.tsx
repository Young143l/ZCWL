import { type FC } from "react";
import { Progress, Typography } from "antd";

interface LearningProgressProps {
    progress: number;
    status: "learning" | "completed";
}

const LearningProgress: FC<LearningProgressProps> = ({ progress, status }) => {
    return (
        <div className="learning-progress py-1">
            <div className="flex justify-between items-center mb-1">
                <Typography.Text className="text-sm">
                    学习进度
                </Typography.Text>
                <Typography.Text className="text-sm" strong>
                    {progress}% {status === "completed" ? "(已完成)" : "(学习中)"}
                </Typography.Text>
            </div>
            <Progress 
                percent={progress} 
                status={status === "completed" ? "success" : "active"}
                strokeColor={{
                    '0%': '#108ee9',
                    '100%': '#87d068',
                }}
                size="small"
                showInfo={false}
            />
        </div>
    );
};

export default LearningProgress;
