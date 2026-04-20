/**
 * 压缩图片到指定大小
 * @param base64 原始图片的 base64 字符串
 * @param maxSizeKB 目标最大大小（KB），默认 100KB
 * @returns 压缩后的 base64 字符串
 */
export async function compressImage(
    base64: string,
    maxSizeKB: number = 100
): Promise<string> {
    return new Promise((resolve, reject) => {
        const img = new Image();
        img.onload = () => {
            const canvas = document.createElement("canvas");
            const ctx = canvas.getContext("2d");

            if (!ctx) {
                reject(new Error("无法创建 canvas context"));
                return;
            }

            // 计算缩放比例，限制最大尺寸
            const maxDimension = 1920;
            let width = img.width;
            let height = img.height;

            if (width > maxDimension || height > maxDimension) {
                const ratio = Math.min(maxDimension / width, maxDimension / height);
                width = width * ratio;
                height = height * ratio;
            }

            canvas.width = width;
            canvas.height = height;

            // 绘制图片
            ctx.drawImage(img, 0, 0, width, height);

            // 二分法查找合适的质量参数
            let quality = 0.9;
            let minQuality = 0.1;
            let maxQuality = 0.9;
            let result = canvas.toDataURL("image/jpeg", quality);

            const maxSizeBytes = maxSizeKB * 1024;
            // 移除 data:image/jpeg;base64, 前缀来计算实际大小
            const getBase64Size = (str: string) =>
                Math.ceil((str.length - str.indexOf(",") - 1) * 0.75);

            let attempts = 0;
            const maxAttempts = 10;

            while (attempts < maxAttempts) {
                const currentSize = getBase64Size(result);

                if (currentSize <= maxSizeBytes) {
                    // 如果大小合适，尝试提高质量
                    if (maxQuality - minQuality < 0.1) {
                        break;
                    }
                    minQuality = quality;
                    quality = (quality + maxQuality) / 2;
                } else {
                    // 如果太大，降低质量
                    maxQuality = quality;
                    quality = (minQuality + quality) / 2;
                }

                result = canvas.toDataURL("image/jpeg", quality);
                attempts++;
            }

            // 如果仍然太大，缩小尺寸
            if (getBase64Size(result) > maxSizeBytes) {
                const scale = Math.sqrt(maxSizeBytes / getBase64Size(result));
                canvas.width = width * scale;
                canvas.height = height * scale;
                ctx.drawImage(img, 0, 0, canvas.width, canvas.height);
                result = canvas.toDataURL("image/jpeg", 0.7);
            }

            resolve(result);
        };

        img.onerror = () => {
            reject(new Error("图片加载失败"));
        };

        img.src = base64;
    });
}

/**
 * 检查并压缩图片（如果需要）
 * @param base64 原始图片的 base64 字符串
 * @param maxSizeKB 目标最大大小（KB），默认 100KB
 * @returns 压缩后的 base64 字符串
 */
export async function ensureCompressedImage(
    base64: string,
    maxSizeKB: number = 100
): Promise<string> {
    // 检查当前大小
    const base64Data = base64.split(",")[1] || base64;
    const currentSizeKB = Math.ceil((base64Data.length * 0.75) / 1024);

    if (currentSizeKB <= maxSizeKB) {
        return base64; // 已经足够小，不需要压缩
    }

    return compressImage(base64, maxSizeKB);
}