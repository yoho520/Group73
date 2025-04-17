package com.example.software.financeapp.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 文件操作工具类
 */
public class FileUtil {

    /**
     * 将字符串写入文件
     * @param file 目标文件
     * @param content 内容
     * @throws IOException 写入异常
     */
    public static void writeStringToFile(File file, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write(content);
        }
    }

    /**
     * 从文件读取字符串
     * @param file 源文件
     * @return 文件内容
     * @throws IOException 读取异常
     */
    public static String readFileToString(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}