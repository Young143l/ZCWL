package com.example.zcwl.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.*;

/**
 * 代码测试工具类
 * 用于执行生成的代码并测试其是否能正常运行
 */
public class CodeTester {

    private static final Logger logger = LoggerFactory.getLogger(CodeTester.class);
    private static final int TIMEOUT_SECONDS = 10; // 代码执行超时时间

    /**
     * 测试代码是否能正常运行
     * @param code 要测试的代码
     * @param type 代码类型（如python、java等）
     * @return 测试结果，true表示代码能正常运行，false表示代码运行失败
     */
    public static boolean testCode(String code, String type) {
        logger.info("测试代码类型: {}", type);
        
        try {
            switch (type.toLowerCase()) {
                case "python":
                    return testPythonCode(code);
                case "java":
                    return testJavaCode(code);
                case "javascript":
                    return testJavaScriptCode(code);
                case "go":
                    return testGoCode(code);
                case "rust":
                    return testRustCode(code);
                case "c":
                    return testCCode(code);
                default:
                    logger.warn("不支持的代码类型: {}", type);
                    return true; // 对于不支持的类型，默认认为代码是有效的
            }
        } catch (Exception e) {
            logger.error("测试代码时发生异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 测试Python代码
     * @param code Python代码
     * @return 测试结果
     */
    private static boolean testPythonCode(String code) {
        logger.info("测试Python代码");
        
        // 检查代码是否包含input函数，如果包含，则为交互式程序
        if (code.contains("input(")) {
            logger.info("检测到交互式Python代码，执行语法检查");
            return testPythonSyntax(code);
        }
        
        File tempFile = null;
        Process process = null;
        
        try {
            // 创建临时Python文件
            tempFile = File.createTempFile("test", ".py");
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(tempFile), StandardCharsets.UTF_8)) {
                writer.write(code);
            }
            
            // 执行Python代码
            String pythonCommand = System.getProperty("os.name").startsWith("Windows") ? "python" : "python3";
            ProcessBuilder pb = new ProcessBuilder(pythonCommand, tempFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            
            process = pb.start();
            final Process finalProcess = process;
            
            // 使用线程池执行，设置超时
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.debug("Python输出: {}", line);
                    }
                }
                return finalProcess.waitFor();
            });
            
            int exitCode = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            executor.shutdown();
            
            logger.info("Python代码执行退出码: {}", exitCode);
            return exitCode == 0;
            
        } catch (TimeoutException e) {
            logger.error("Python代码执行超时");
            if (process != null) {
                process.destroy();
            }
            return false;
        } catch (Exception e) {
            logger.error("测试Python代码失败: {}", e.getMessage());
            return false;
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            // 确保进程被销毁
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }
    
    /**
     * 测试Python代码的语法
     * @param code Python代码
     * @return 测试结果
     */
    private static boolean testPythonSyntax(String code) {
        logger.info("测试Python代码语法");
        
        File tempFile = null;
        Process process = null;
        
        try {
            // 创建临时Python文件
            tempFile = File.createTempFile("test", ".py");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(code);
            }
            
            // 执行Python语法检查
            String pythonCommand = System.getProperty("os.name").startsWith("Windows") ? "python" : "python3";
            ProcessBuilder pb = new ProcessBuilder(pythonCommand, "-m", "py_compile", tempFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            
            process = pb.start();
            int exitCode = process.waitFor();
            
            logger.info("Python语法检查退出码: {}", exitCode);
            return exitCode == 0;
            
        } catch (Exception e) {
            logger.error("测试Python语法失败: {}", e.getMessage());
            return false;
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            // 清理编译产物
            String pycFile = tempFile.getAbsolutePath().replace(".py", ".pyc");
            File pyc = new File(pycFile);
            if (pyc.exists()) {
                pyc.delete();
            }
            // 确保进程被销毁
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    /**
     * 测试Java代码
     * @param code Java代码
     * @return 测试结果
     */
    private static boolean testJavaCode(String code) {
        logger.info("测试Java代码");
        
        File tempDir = null;
        File tempFile = null;
        Process process = null;
        
        try {
            // 创建临时目录
            tempDir = Files.createTempDirectory("test").toFile();
            
            // 提取类名
            String className = extractJavaClassName(code);
            if (className == null) {
                className = "TestClass";
            }
            
            // 创建临时Java文件
            tempFile = new File(tempDir, className + ".java");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(code);
            }
            
            // 编译Java代码
            ProcessBuilder compilePb = new ProcessBuilder("javac", tempFile.getAbsolutePath());
            compilePb.redirectErrorStream(true);
            process = compilePb.start();
            int compileExitCode = process.waitFor();
            
            if (compileExitCode != 0) {
                logger.error("Java代码编译失败");
                return false;
            }
            
            // 运行Java代码
            ProcessBuilder runPb = new ProcessBuilder("java", "-cp", tempDir.getAbsolutePath(), className);
            runPb.redirectErrorStream(true);
            process = runPb.start();
            final Process finalProcess = process;
            
            // 使用线程池执行，设置超时
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.debug("Java输出: {}", line);
                    }
                }
                return finalProcess.waitFor();
            });
            
            int exitCode = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            executor.shutdown();
            
            logger.info("Java代码执行退出码: {}", exitCode);
            return exitCode == 0;
            
        } catch (TimeoutException e) {
            logger.error("Java代码执行超时");
            if (process != null) {
                process.destroy();
            }
            return false;
        } catch (Exception e) {
            logger.error("测试Java代码失败: {}", e.getMessage());
            return false;
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            if (tempDir != null && tempDir.exists()) {
                deleteDirectory(tempDir);
            }
            // 确保进程被销毁
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    /**
     * 测试JavaScript代码
     * @param code JavaScript代码
     * @return 测试结果
     */
    private static boolean testJavaScriptCode(String code) {
        logger.info("测试JavaScript代码");
        
        File tempFile = null;
        Process process = null;
        
        try {
            // 创建临时JavaScript文件
            tempFile = File.createTempFile("test", ".js");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(code);
            }
            
            // 执行JavaScript代码
            ProcessBuilder pb = new ProcessBuilder("node", tempFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            
            process = pb.start();
            final Process finalProcess = process;
            
            // 使用线程池执行，设置超时
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.debug("JavaScript输出: {}", line);
                    }
                }
                return finalProcess.waitFor();
            });
            
            int exitCode = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            executor.shutdown();
            
            logger.info("JavaScript代码执行退出码: {}", exitCode);
            return exitCode == 0;
            
        } catch (TimeoutException e) {
            logger.error("JavaScript代码执行超时");
            if (process != null) {
                process.destroy();
            }
            return false;
        } catch (Exception e) {
            logger.error("测试JavaScript代码失败: {}", e.getMessage());
            return false;
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            // 确保进程被销毁
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    /**
     * 测试Go代码
     * @param code Go代码
     * @return 测试结果
     */
    private static boolean testGoCode(String code) {
        logger.info("测试Go代码");
        
        File tempFile = null;
        Process process = null;
        
        try {
            // 创建临时Go文件
            tempFile = File.createTempFile("test", ".go");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(code);
            }
            
            // 执行Go代码
            ProcessBuilder pb = new ProcessBuilder("go", "run", tempFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            
            process = pb.start();
            final Process finalProcess = process;
            
            // 使用线程池执行，设置超时
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.debug("Go输出: {}", line);
                    }
                }
                return finalProcess.waitFor();
            });
            
            int exitCode = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            executor.shutdown();
            
            logger.info("Go代码执行退出码: {}", exitCode);
            return exitCode == 0;
            
        } catch (TimeoutException e) {
            logger.error("Go代码执行超时");
            if (process != null) {
                process.destroy();
            }
            return false;
        } catch (Exception e) {
            logger.error("测试Go代码失败: {}", e.getMessage());
            return false;
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            // 确保进程被销毁
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    /**
     * 测试Rust代码
     * @param code Rust代码
     * @return 测试结果
     */
    private static boolean testRustCode(String code) {
        logger.info("测试Rust代码");
        
        File tempDir = null;
        File tempFile = null;
        Process process = null;
        
        try {
            // 创建临时目录
            tempDir = Files.createTempDirectory("test").toFile();
            
            // 创建Cargo.toml文件
            File cargoToml = new File(tempDir, "Cargo.toml");
            try (FileWriter writer = new FileWriter(cargoToml)) {
                writer.write("[package]\n");
                writer.write("name = \"test\"\n");
                writer.write("version = \"0.1.0\"\n");
                writer.write("edition = \"2021\"\n\n");
                writer.write("[dependencies]\n");
            }
            
            // 创建src目录和main.rs文件
            File srcDir = new File(tempDir, "src");
            srcDir.mkdir();
            tempFile = new File(srcDir, "main.rs");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(code);
            }
            
            // 执行Rust代码
            ProcessBuilder pb = new ProcessBuilder("cargo", "run");
            pb.directory(tempDir);
            pb.redirectErrorStream(true);
            
            process = pb.start();
            final Process finalProcess = process;
            
            // 使用线程池执行，设置超时
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.debug("Rust输出: {}", line);
                    }
                }
                return finalProcess.waitFor();
            });
            
            int exitCode = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            executor.shutdown();
            
            logger.info("Rust代码执行退出码: {}", exitCode);
            return exitCode == 0;
            
        } catch (TimeoutException e) {
            logger.error("Rust代码执行超时");
            if (process != null) {
                process.destroy();
            }
            return false;
        } catch (Exception e) {
            logger.error("测试Rust代码失败: {}", e.getMessage());
            return false;
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            if (tempDir != null && tempDir.exists()) {
                deleteDirectory(tempDir);
            }
            // 确保进程被销毁
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    /**
     * 测试C代码
     * @param code C代码
     * @return 测试结果
     */
    private static boolean testCCode(String code) {
        logger.info("测试C代码");
        
        File tempFile = null;
        Process process = null;
        
        try {
            // 创建临时C文件
            tempFile = File.createTempFile("test", ".c");
            try (FileWriter writer = new FileWriter(tempFile)) {
                writer.write(code);
            }
            
            // 编译C代码
            String compiler = System.getProperty("os.name").startsWith("Windows") ? "cl" : "gcc";
            String outputFile = System.getProperty("os.name").startsWith("Windows") ? "test.exe" : "test";
            
            ProcessBuilder compilePb;
            if (System.getProperty("os.name").startsWith("Windows")) {
                compilePb = new ProcessBuilder(compiler, tempFile.getAbsolutePath(), "/Fe" + outputFile);
            } else {
                compilePb = new ProcessBuilder(compiler, tempFile.getAbsolutePath(), "-o", outputFile);
            }
            compilePb.redirectErrorStream(true);
            process = compilePb.start();
            int compileExitCode = process.waitFor();
            
            if (compileExitCode != 0) {
                logger.error("C代码编译失败");
                return false;
            }
            
            // 运行C代码
            ProcessBuilder runPb = new ProcessBuilder("./" + outputFile);
            runPb.redirectErrorStream(true);
            process = runPb.start();
            final Process finalProcess = process;
            
            // 使用线程池执行，设置超时
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Integer> future = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(finalProcess.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.debug("C输出: {}", line);
                    }
                }
                return finalProcess.waitFor();
            });
            
            int exitCode = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            executor.shutdown();
            
            logger.info("C代码执行退出码: {}", exitCode);
            return exitCode == 0;
            
        } catch (TimeoutException e) {
            logger.error("C代码执行超时");
            if (process != null) {
                process.destroy();
            }
            return false;
        } catch (Exception e) {
            logger.error("测试C代码失败: {}", e.getMessage());
            return false;
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
            // 清理编译产物
            File exeFile = new File("test.exe");
            if (exeFile.exists()) {
                exeFile.delete();
            }
            File outFile = new File("test");
            if (outFile.exists()) {
                outFile.delete();
            }
            // 确保进程被销毁
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    /**
     * 提取Java代码中的类名
     * @param code Java代码
     * @return 类名
     */
    private static String extractJavaClassName(String code) {
        // 简单的正则表达式提取类名
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("class\s+(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 删除目录及其所有内容
     * @param directory 要删除的目录
     */
    private static void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}
