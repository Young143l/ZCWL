import { createInterface } from "readline";
import { readDir, readFile } from "./FileReader.js";
import { dbServer } from "./dbService.js";

const rl = createInterface({
    input: process.stdin,
    output: process.stdout,
});

const askQuestion = (question: string): Promise<string> => {
    return new Promise((resolve) => {
        rl.question(question, (answer) => {
            resolve(answer);
        });
    });
};

const parseFileName = (
    fileName: string,
): { c_id: string; title: string } | null => {
    const match = fileName.match(/^(\d+)\.(.+)\.md$/);
    if (match && match[1] && match[2]) {
        return {
            c_id: match[1], // 章节号
            title: match[2], // 文档名（标题）
        };
    }
    return null;
};

const main = async () => {
    try {
        const doc_name = await askQuestion("请输入文章名：");
        const doc_sum = await askQuestion("请输入文章概述：");
        const doc_icon = await askQuestion("请输入文章图标链接：");
        const dir = await askQuestion("请输入文章本地目录地址：");

        rl.close();

        const db = new dbServer();

        const docId = await db.newDoc(doc_name, doc_sum, doc_icon);
        if (!docId) {
            console.error("文档插入失败");
            return;
        }
        console.log(`文档已创建，ID: ${docId}`);

        const files = await readDir(dir);
        if (!files || files.length === 0) {
            console.log("目录为空或读取失败");
            return;
        }

        console.log("读取到的文件列表：", files);

        // 处理每个文件
        for (const fileName of files) {
            const filePath = `${dir}/${fileName}`;
            const parsedResult = parseFileName(fileName);
            if (!parsedResult) {
                console.log(`跳过文件 ${fileName}：文件名格式不正确`);
                continue;
            }
            const { c_id, title } = parsedResult;

            const content = await readFile(filePath);
            if (!content) {
                console.log(`跳过文件 ${fileName}：无法读取内容`);
                continue;
            }

            const success = await db.addContent(docId, c_id, title, content);
            if (success) {
                console.log(`文件 ${fileName} 已添加到文档中`);
            } else {
                console.log(`文件 ${fileName} 添加失败`);
            }
        }

        console.log("所有文件处理完成！");
    } catch (error) {
        console.error("发生错误：", error);
    }
};

main();
