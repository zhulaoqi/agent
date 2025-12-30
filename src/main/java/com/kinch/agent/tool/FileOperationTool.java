package com.kinch.agent.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.function.Function;

/**
 * 文件操作工具
 * 支持读取、写入、列表等操作
 *
 * @author kinch
 * @date 2025-12-29
 */
@Slf4j
@Component
public class FileOperationTool implements Function<String, String> {

    private static final String WORKSPACE = System.getProperty("user.dir") + "/workspace/";

    @Override
    public String apply(String input) {
        log.info("文件操作工具 - 输入: {}", input);

        try {
            // 解析输入（格式：操作|路径[|内容]）
            String[] parts = input.split("\\|", 3);
            if (parts.length < 2) {
                return "❌ 参数格式错误，应为：操作|路径[|内容]";
            }

            String operation = parts[0].trim().toLowerCase();
            String path = parts[1].trim();
            String content = parts.length > 2 ? parts[2].trim() : null;

            // 安全检查：限制在workspace目录内
            File file = new File(WORKSPACE + path);
            String canonicalPath = file.getCanonicalPath();
            if (!canonicalPath.startsWith(new File(WORKSPACE).getCanonicalPath())) {
                return "❌ 安全限制：只能操作workspace目录内的文件";
            }

            switch (operation) {
                case "read":
                    return readFile(file);
                case "write":
                    return writeFile(file, content);
                case "list":
                    return listFiles(file);
                case "exists":
                    return file.exists() ? "✅ 文件存在" : "❌ 文件不存在";
                default:
                    return "❌ 不支持的操作：" + operation;
            }

        } catch (Exception e) {
            log.error("文件操作失败", e);
            return "❌ 文件操作失败: " + e.getMessage();
        }
    }

    private String readFile(File file) throws Exception {
        if (!file.exists()) {
            return "❌ 文件不存在";
        }
        String content = new String(Files.readAllBytes(file.toPath()));
        return "✅ 文件读取成功\n\n" + content;
    }

    private String writeFile(File file, String content) throws Exception {
        if (content == null) {
            return "❌ 缺少写入内容";
        }
        file.getParentFile().mkdirs();
        Files.write(file.toPath(), content.getBytes());
        return "✅ 文件写入成功";
    }

    private String listFiles(File dir) {
        if (!dir.exists()) {
            return "❌ 目录不存在";
        }
        if (!dir.isDirectory()) {
            return "❌ 不是目录";
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return "目录为空";
        }

        StringBuilder sb = new StringBuilder("目录内容：\n\n");
        for (File file : files) {
            sb.append(file.isDirectory() ? "📁 " : "📄 ")
              .append(file.getName())
              .append("\n");
        }
        return sb.toString();
    }
}

