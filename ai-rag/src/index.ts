import { DocProcessor } from "./docProcessor";

// 主函数
async function main() {
    const processor = new DocProcessor();

    try {
        await processor.processAll();
        console.log("\n✅ 文档处理完成！");
    } catch (error) {
        console.error("\n❌ 文档处理失败:", error);
        process.exit(1);
    } finally {
        await processor.close();
    }
}

// 如果直接运行此脚本
if (require.main === module) {
    main();
}
