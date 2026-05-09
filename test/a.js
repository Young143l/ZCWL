// a.js
import { readFileSync, writeFileSync } from 'fs';
import { fileURLToPath } from 'url';
import { dirname, join } from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// 读取 a.txt 文件内容
const txtFilePath = join(__dirname, 'a.txt');
const txtContent = readFileSync(txtFilePath, 'utf8');

// 解析 JSON
let code;
try {
    code = JSON.parse(txtContent);
} catch (error) {
    console.error("解析 JSON 失败:", error.message);
    process.exit(1); // 如果解析失败，退出程序
}

// 写入 HTML 文件
writeFileSync(join(__dirname, 'output.html'), code.html, 'utf8');
console.log("HTML文件已写入: output.html");

// 写入 CSS 文件
writeFileSync(join(__dirname, 'styles.css'), code.css, 'utf8');
console.log("CSS文件已写入: styles.css");

// 写入 JavaScript 文件
writeFileSync(join(__dirname, 'script.js'), code.javascript, 'utf8');
console.log("JavaScript文件已写入: script.js");

console.log("所有文件写入完成！");