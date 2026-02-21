import fs from "fs";
import { promisify } from "util";

const readdirAsync = promisify(fs.readdir);
const readFileAsync = promisify(fs.readFile);

export const readDir = async (dirPath: string): Promise<string[] | null> => {
    if (!dirPath) {
        console.log("错误：目录为空。");
        return null;
    }
    try {
        const files = await readdirAsync(dirPath);
        return files;
    } catch (err) {
        console.log("错误:" + err);
        return null;
    }
};

export const readFile = async (fileName: string): Promise<string | null> => {
    if (!fileName) {
        console.log("错误：文件名为空");
        return null;
    }
    try {
        const file: Buffer = await readFileAsync(fileName);
        return file.toString("utf-8");
    } catch (err) {
        console.log("错误：" + err);
        return null;
    }
};
