package com.example.software.financeapp.util;

import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.entity.User;
import com.example.software.financeapp.model.enums.TransactionType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CSV文件解析器
 * 支持解析支付宝、微信支付、银行账单等CSV文件格式
 */
public class CSVParser {

    // 支持的CSV文件来源类型
    public enum CSVSourceType {
        ALIPAY,   // 支付宝
        WECHAT,   // 微信支付
        BANK,     // 银行对账单
        CUSTOM    // 自定义格式
    }

    // CSV文件列索引映射
    private Map<String, Integer> columnMapping;

    // 日期时间格式
    private DateTimeFormatter dateFormatter;

    // CSV源类型
    private CSVSourceType sourceType;

    // 用户
    private User user;

    /**
     * 构造函数
     * @param sourceType CSV文件源类型
     * @param user 当前用户
     */
    public CSVParser(CSVSourceType sourceType, User user) {
        this.sourceType = sourceType;
        this.user = user;
        this.columnMapping = new HashMap<>();

        // 根据源类型初始化解析器配置
        initParserConfig();
    }

    /**
     * 初始化解析器配置
     */
    private void initParserConfig() {
        switch (sourceType) {
            case ALIPAY:
                initAlipayConfig();
                break;
            case WECHAT:
                initWechatConfig();
                break;
            case BANK:
                initBankConfig();
                break;
            case CUSTOM:
                // 自定义配置需要手动设置
                break;
        }
    }

    /**
     * 初始化支付宝CSV配置
     */
    private void initAlipayConfig() {
        // 支付宝账单列映射（调整为实际字段顺序）
        columnMapping.put("transactionId", 0);   // 交易号（第1列）
        columnMapping.put("date", 1);            // 交易时间（第2列）
        columnMapping.put("type", 2);            // 交易类型（第3列）
        columnMapping.put("merchant", 3);        // 交易对方（第4列）
        columnMapping.put("description", 4);     // 商品说明（第5列）
        columnMapping.put("income_expense", 5);  // 收/支（第6列）
        columnMapping.put("amount", 6);          // 金额（第7列）
        columnMapping.put("paymentMethod", 7);   // 收付款方式（第8列）
        columnMapping.put("status", 8);          // 交易状态（第9列）
        columnMapping.put("remark", 9);          // 备注（第10列）

        // 不使用严格的日期时间解析，而是使用灵活的解析方法
    }

    /**
     * 初始化微信支付CSV配置
     */
    private void initWechatConfig() {
        // 微信支付账单列映射
        columnMapping.put("date", 0);          // 交易时间
        columnMapping.put("merchant", 1);      // 商户名称
        columnMapping.put("type", 2);          // 交易类型
        columnMapping.put("description", 3);   // 商品说明
        columnMapping.put("amount", 4);        // 金额

        // 不使用严格的日期时间解析，而是使用灵活的解析方法
    }

    /**
     * 初始化银行CSV配置
     */
    private void initBankConfig() {
        // 银行账单列映射 (示例，不同银行可能不同)
        columnMapping.put("date", 0);          // 交易日期
        columnMapping.put("description", 1);   // 交易描述
        columnMapping.put("expense", 2);       // 支出金额
        columnMapping.put("income", 3);        // 收入金额
        columnMapping.put("balance", 4);       // 余额

        // 不使用严格的日期时间解析，而是使用灵活的解析方法
    }

    /**
     * 解析CSV文件
     * @param file CSV文件
     * @return 解析后的交易列表
     * @throws Exception 解析异常
     */
    public List<Transaction> parseFile(File file) throws Exception {
        List<Transaction> transactions = new ArrayList<>();

        // 尝试不同的字符集，优先使用GBK，因为CSV文件经常以GBK编码保存
        Charset[] charsets = {Charset.forName("GBK"), StandardCharsets.UTF_8, Charset.forName("GB18030")};

        for (Charset charset : charsets) {
            transactions.clear();
            boolean success = true;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), charset))) {

                String line;
                boolean isFirstLine = true;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;

                    // 跳过CSV头行
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }

                    try {
                        // 解析CSV行为字段数组
                        String[] fields = parseCsvLine(line);

                        // 根据源类型解析交易记录
                        Transaction transaction = null;
                        switch (sourceType) {
                            case ALIPAY:
                                transaction = parseAlipayTransaction(fields);
                                break;
                            case WECHAT:
                                transaction = parseWechatTransaction(fields);
                                break;
                            case BANK:
                                transaction = parseBankTransaction(fields);
                                break;
                            default:
                                // 自定义格式，需要实现
                                break;
                        }

                        if (transaction != null) {
                            transactions.add(transaction);
                            System.out.println("成功解析第" + lineNumber + "行交易记录");
                        }
                    } catch (Exception e) {
                        System.err.println("解析第" + lineNumber + "行时出错: " + line);
                        System.err.println("错误信息: " + e.getMessage());
                        success = false;
                        break;
                    }
                }

                if (success && !transactions.isEmpty()) {
                    System.out.println("成功使用" + charset.name() + "编码解析 " + transactions.size() + " 条交易记录");
                    return transactions;
                }
            } catch (Exception e) {
                System.err.println("使用" + charset.name() + "编码解析文件时出错: " + e.getMessage());
            }
        }

        // 如果所有字符集都尝试失败
        if (transactions.isEmpty()) {
            System.err.println("无法解析CSV文件，请检查文件格式和编码");
        }

        return transactions;
    }

    /**
     * 解析单行CSV数据
     * @param line CSV行文本
     * @return 字段数组
     */
    private String[] parseCsvLine(String line) {
        // 使用逗号分隔
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // 引号处理
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                // 字段分隔符
                fields.add(field.toString().trim());
                field = new StringBuilder();
            } else {
                // 字段内容
                field.append(c);
            }
        }

        // 添加最后一个字段
        fields.add(field.toString().trim());

        return fields.toArray(new String[0]);
    }

    /**
     * 灵活解析日期时间字符串
     * @param dateStr 日期字符串
     * @return 解析后的LocalDateTime
     */
    private LocalDateTime parseDateTime(String dateStr) {
        // 处理空或null值
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return LocalDateTime.now();
        }

        dateStr = dateStr.trim();

        // 常见日期格式
        String[] patterns = {
                "yyyy/M/d HH:mm",
                "yyyy/M/d H:mm",
                "yyyy/MM/dd HH:mm",
                "yyyy/MM/dd H:mm",
                "yyyy-M-d HH:mm",
                "yyyy-M-d H:mm",
                "yyyy-MM-dd HH:mm",
                "yyyy-MM-dd H:mm",
                "yyyy/M/d",
                "yyyy/MM/dd",
                "yyyy-M-d",
                "yyyy-MM-dd"
        };

        // 尝试使用各种格式解析
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

                // 如果格式不包含时间，添加默认时间
                if (!pattern.contains("H")) {
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    return LocalDateTime.of(date, LocalTime.of(0, 0));
                } else {
                    return LocalDateTime.parse(dateStr, formatter);
                }
            } catch (DateTimeParseException e) {
                // 继续尝试下一个格式
            }
        }

        // 尝试使用正则表达式提取日期时间
        try {
            Pattern datePattern = Pattern.compile("(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})(?:\\s+(\\d{1,2}):(\\d{1,2})(?::(\\d{1,2}))?)?");
            Matcher matcher = datePattern.matcher(dateStr);

            if (matcher.find()) {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));

                int hour = 0, minute = 0, second = 0;
                if (matcher.group(4) != null) {
                    hour = Integer.parseInt(matcher.group(4));
                    minute = Integer.parseInt(matcher.group(5));
                    if (matcher.group(6) != null) {
                        second = Integer.parseInt(matcher.group(6));
                    }
                }

                return LocalDateTime.of(year, month, day, hour, minute, second);
            }
        } catch (Exception e) {
            // 如果正则匹配失败，记录错误但不抛出
            System.err.println("通过正则提取日期失败: " + e.getMessage());
        }

        // 所有方法都失败，返回当前时间
        System.err.println("无法解析日期: " + dateStr + "，使用当前时间");
        return LocalDateTime.now();
    }

    /**
     * 解析支付宝交易行
     * @param fields 字段数组
     * @return 交易对象
     */
    private Transaction parseAlipayTransaction(String[] fields) {
        try {
            // 检查字段数量是否足够
            if (fields.length <= Math.max(
                    columnMapping.get("date"),
                    Math.max(columnMapping.get("amount"),
                            Math.max(columnMapping.get("merchant"),
                                    columnMapping.get("description"))))) {
                System.err.println("字段数量不足，期望至少" +
                        (Math.max(columnMapping.get("date"),
                                Math.max(columnMapping.get("amount"),
                                        Math.max(columnMapping.get("merchant"),
                                                columnMapping.get("description"))) + 1) +
                                "列，实际只有" + fields.length + "列"));
                return null;
            }

            // 获取交易日期
            String dateStr = fields[columnMapping.get("date")];
            LocalDateTime transactionDate = parseDateTime(dateStr);

            // 获取交易金额和类型
            String amountStr = fields[columnMapping.get("amount")];
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr.replaceAll("[^\\d.-]", ""));
            } catch (NumberFormatException e) {
                System.err.println("金额解析错误: " + amountStr);
                return null;
            }

            // 判断交易类型
            TransactionType type = TransactionType.EXPENSE;

            // 首先尝试从收/支列判断
            if (columnMapping.containsKey("income_expense")) {
                int index = columnMapping.get("income_expense");
                if (index < fields.length) {
                    String incomeExpense = fields[index].trim();
                    if ("收入".equals(incomeExpense)) {
                        type = TransactionType.INCOME;
                    }
                }
            }

            // 如果金额是负数，则为支出
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                type = TransactionType.EXPENSE;
                amount = amount.abs(); // 取绝对值
            } else if (amount.compareTo(BigDecimal.ZERO) > 0) {
                // 如果没有明确的类型指示，且金额为正，默认为收入
                if (!columnMapping.containsKey("income_expense")) {
                    type = TransactionType.INCOME;
                }
            }

            // 获取商家和描述
            String merchant = fields[columnMapping.get("merchant")];
            String description = fields[columnMapping.get("description")];

            // 如果有备注，添加到描述
            if (columnMapping.containsKey("remark") &&
                    columnMapping.get("remark") < fields.length &&
                    !fields[columnMapping.get("remark")].isEmpty()) {
                description += " (" + fields[columnMapping.get("remark")] + ")";
            }

            // 创建交易对象
            return Transaction.builder()
                    .amount(amount)
                    .type(type)
                    .description(description)
                    .transactionDate(transactionDate)
                    .merchant(merchant)
                    .source("支付宝")
                    .user(user)
                    .rawData(String.join(",", fields))
                    .build();
        } catch (Exception e) {
            System.err.println("解析支付宝交易时出现未处理异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析微信支付交易行
     * @param fields 字段数组
     * @return 交易对象
     */
    private Transaction parseWechatTransaction(String[] fields) {
        try {
            // 检查字段数量是否足够
            int maxIndex = 0;
            for (Integer index : columnMapping.values()) {
                if (index > maxIndex) {
                    maxIndex = index;
                }
            }

            if (fields.length <= maxIndex) {
                System.err.println("字段数量不足，期望至少" + (maxIndex + 1) + "列，实际只有" + fields.length + "列");
                return null;
            }

            // 获取交易日期
            String dateStr = fields[columnMapping.get("date")];
            LocalDateTime transactionDate = parseDateTime(dateStr);

            // 获取交易金额和类型
            String amountStr = fields[columnMapping.get("amount")];
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr.replaceAll("[^\\d.-]", ""));
            } catch (NumberFormatException e) {
                System.err.println("金额解析错误: " + amountStr);
                return null;
            }

            // 判断交易类型 (微信账单中，支出通常标记为"-"，收入为"+")
            TransactionType type = amountStr.contains("-")
                    ? TransactionType.EXPENSE
                    : TransactionType.INCOME;

            // 取金额绝对值
            amount = amount.abs();

            // 获取商家和描述
            String merchant = fields[columnMapping.get("merchant")];
            String description = fields[columnMapping.get("description")];

            // 创建交易对象
            return Transaction.builder()
                    .amount(amount)
                    .type(type)
                    .description(description)
                    .transactionDate(transactionDate)
                    .merchant(merchant)
                    .source("微信支付")
                    .user(user)
                    .rawData(String.join(",", fields))
                    .build();
        } catch (Exception e) {
            System.err.println("解析微信支付交易时出现未处理异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 解析银行交易行
     * @param fields 字段数组
     * @return 交易对象
     */
    private Transaction parseBankTransaction(String[] fields) {
        try {
            // 检查字段数量是否足够
            int maxIndex = 0;
            for (Integer index : columnMapping.values()) {
                if (index > maxIndex) {
                    maxIndex = index;
                }
            }

            if (fields.length <= maxIndex) {
                System.err.println("字段数量不足，期望至少" + (maxIndex + 1) + "列，实际只有" + fields.length + "列");
                return null;
            }

            // 获取交易日期
            String dateStr = fields[columnMapping.get("date")];
            LocalDateTime transactionDate = parseDateTime(dateStr);

            // 获取交易描述
            String description = fields[columnMapping.get("description")];

            // 获取支出和收入金额
            String expenseStr = fields[columnMapping.get("expense")];
            String incomeStr = fields[columnMapping.get("income")];

            BigDecimal amount;
            TransactionType type;

            // 判断交易类型和金额
            if (!expenseStr.isEmpty() && !expenseStr.equals("0") && !expenseStr.equals("0.00")) {
                // 支出交易
                try {
                    amount = new BigDecimal(expenseStr.replaceAll("[^\\d.]", ""));
                    type = TransactionType.EXPENSE;
                } catch (NumberFormatException e) {
                    System.err.println("支出金额解析错误: " + expenseStr);
                    return null;
                }
            } else if (!incomeStr.isEmpty() && !incomeStr.equals("0") && !incomeStr.equals("0.00")) {
                // 收入交易
                try {
                    amount = new BigDecimal(incomeStr.replaceAll("[^\\d.]", ""));
                    type = TransactionType.INCOME;
                } catch (NumberFormatException e) {
                    System.err.println("收入金额解析错误: " + incomeStr);
                    return null;
                }
            } else {
                // 无效交易
                return null;
            }

            // 提取商家信息 (银行账单通常没有明确的商家字段，从描述中推断)
            String merchant = extractMerchantFromDescription(description);

            // 创建交易对象
            return Transaction.builder()
                    .amount(amount)
                    .type(type)
                    .description(description)
                    .transactionDate(transactionDate)
                    .merchant(merchant)
                    .source("银行")
                    .user(user)
                    .rawData(String.join(",", fields))
                    .build();
        } catch (Exception e) {
            System.err.println("解析银行交易时出现未处理异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从描述中提取商家信息
     * @param description 交易描述
     * @return 提取的商家名称
     */
    private String extractMerchantFromDescription(String description) {
        // 简单实现：取描述的前20个字符作为商家名称
        // 实际应用中可能需要更复杂的规则或NLP方法
        int maxLength = Math.min(description.length(), 20);
        return description.substring(0, maxLength).trim();
    }

    /**
     * 设置自定义列映射
     * @param mapping 列名到索引的映射
     */
    public void setColumnMapping(Map<String, Integer> mapping) {
        this.columnMapping = mapping;
    }

    /**
     * 设置日期格式
     * @param pattern 日期格式模式
     */
    public void setDateFormat(String pattern) {
        this.dateFormatter = DateTimeFormatter.ofPattern(pattern);
    }
}