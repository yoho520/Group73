package com.example.software.financeapp.service;

import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 欺诈检测服务 - 负责识别可疑交易并提供预警
 */
public class FraudDetectionService {

    // 可疑交易检测阈值
    private static final BigDecimal UNUSUAL_AMOUNT_THRESHOLD = new BigDecimal("1000.00");
    private static final BigDecimal DUPLICATE_AMOUNT_THRESHOLD = new BigDecimal("1.00"); // 金额差异允许范围
    private static final int DUPLICATE_TIME_WINDOW_MINUTES = 120; // 短时间内(2小时)重复交易窗口
    private static final int UNUSUAL_LOCATION_TIME_WINDOW_MINUTES = 60; // 短时间内不同地点交易窗口
    private static final String[] SUSPICIOUS_MERCHANT_KEYWORDS = {
            "未知", "海外", "赌", "博彩", "游戏充值", "未认证", "投资"
    };

    /**
     * 检测可疑交易
     * @param transactions 待检测的交易列表(按时间排序)
     * @return 可疑交易及其原因
     */
    public Map<Transaction, List<String>> detectSuspiciousTransactions(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Transaction, List<String>> suspiciousTransactions = new HashMap<>();

        // 排序交易列表(按时间从新到旧)
        List<Transaction> sortedTransactions = new ArrayList<>(transactions);
        sortedTransactions.sort(Comparator.comparing(Transaction::getTransactionDate).reversed());

        // 检测异常金额
        detectUnusualAmounts(sortedTransactions, suspiciousTransactions);

        // 检测短时间内重复交易
        detectDuplicateTransactions(sortedTransactions, suspiciousTransactions);

        // 检测短时间内不同地点的交易
        detectUnusualLocations(sortedTransactions, suspiciousTransactions);

        // 检测可疑商家
        detectSuspiciousMerchants(sortedTransactions, suspiciousTransactions);

        return suspiciousTransactions;
    }

    /**
     * 检测异常金额交易
     */
    private void detectUnusualAmounts(List<Transaction> transactions,
                                      Map<Transaction, List<String>> suspiciousTransactions) {
        for (Transaction transaction : transactions) {
            // 只检测支出类交易
            if (transaction.getType() != TransactionType.EXPENSE) {
                continue;
            }

            if (transaction.getAmount().compareTo(UNUSUAL_AMOUNT_THRESHOLD) > 0) {
                String reason = String.format("异常大额交易: ¥%.2f", transaction.getAmount());
                addSuspiciousTransaction(suspiciousTransactions, transaction, reason);
            }
        }
    }

    /**
     * 检测短时间内重复交易
     */
    private void detectDuplicateTransactions(List<Transaction> transactions,
                                             Map<Transaction, List<String>> suspiciousTransactions) {
        // 只考虑最近的交易
        if (transactions.size() < 2) return;

        // 用于暂存已处理的交易，避免重复报告
        Set<Long> processedTransactions = new HashSet<>();

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t1 = transactions.get(i);

            if (processedTransactions.contains(t1.getId())) {
                continue;
            }

            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction t2 = transactions.get(j);

                // 检查两笔交易是否在时间窗口内
                long minutesBetween = ChronoUnit.MINUTES.between(
                        t2.getTransactionDate(),
                        t1.getTransactionDate());

                if (minutesBetween > DUPLICATE_TIME_WINDOW_MINUTES) {
                    // 超出时间窗口，不再继续检查
                    break;
                }

                // 检查商家名称是否相同
                boolean sameMerchant = Objects.equals(
                        t1.getDescription(),
                        t2.getDescription());

                // 检查金额是否相似
                BigDecimal amountDiff = t1.getAmount().subtract(t2.getAmount()).abs();
                boolean similarAmount = amountDiff.compareTo(DUPLICATE_AMOUNT_THRESHOLD) <= 0;

                if (sameMerchant && similarAmount) {
                    String reason = String.format(
                            "短时间内重复交易: %s, ¥%.2f, 间隔%d分钟",
                            t1.getDescription(),
                            t1.getAmount(),
                            minutesBetween);

                    addSuspiciousTransaction(suspiciousTransactions, t1, reason);
                    addSuspiciousTransaction(suspiciousTransactions, t2, reason);

                    processedTransactions.add(t1.getId());
                    processedTransactions.add(t2.getId());
                }
            }
        }
    }

    /**
     * 检测短时间内不同地点的交易 (地理位置不可能)
     */
    /**
     * 检测短时间内不同商家的交易 (替代地理位置检测)
     */
    private void detectUnusualLocations(List<Transaction> transactions,
                                        Map<Transaction, List<String>> suspiciousTransactions) {
        // 检查是否有商家信息可用
        long merchantsAvailable = transactions.stream()
                .filter(t -> t.getMerchant() != null && !t.getMerchant().isEmpty())
                .count();

        if (merchantsAvailable < 2) {
            // 不足两个有商家信息的交易，无法进行检测
            return;
        }

        // 按商家分组并检测时间异常
        Map<String, List<Transaction>> transactionsByMerchant = transactions.stream()
                .filter(t -> t.getMerchant() != null && !t.getMerchant().isEmpty())
                .collect(Collectors.groupingBy(Transaction::getMerchant));

        List<Transaction> merchantTransactions = transactions.stream()
                .filter(t -> t.getMerchant() != null && !t.getMerchant().isEmpty())
                .sorted(Comparator.comparing(Transaction::getTransactionDate))
                .collect(Collectors.toList());

        for (int i = 0; i < merchantTransactions.size() - 1; i++) {
            Transaction t1 = merchantTransactions.get(i);
            Transaction t2 = merchantTransactions.get(i + 1);

            // 检查是否不同商家
            if (!t1.getMerchant().equals(t2.getMerchant())) {
                // 计算时间间隔
                long minutesBetween = Math.abs(ChronoUnit.MINUTES.between(
                        t1.getTransactionDate(),
                        t2.getTransactionDate()));

                // 判断时间间隔是否不合理
                if (minutesBetween < UNUSUAL_LOCATION_TIME_WINDOW_MINUTES) {
                    String reason = String.format(
                            "商家异常: %d分钟内在不同商家(%s和%s)有交易记录",
                            minutesBetween,
                            t1.getMerchant(),
                            t2.getMerchant());

                    addSuspiciousTransaction(suspiciousTransactions, t1, reason);
                    addSuspiciousTransaction(suspiciousTransactions, t2, reason);
                }
            }
        }
    }

    /**
     * 检测可疑商家
     */
    private void detectSuspiciousMerchants(List<Transaction> transactions,
                                           Map<Transaction, List<String>> suspiciousTransactions) {
        for (Transaction transaction : transactions) {
            String description = transaction.getDescription().toLowerCase();

            for (String keyword : SUSPICIOUS_MERCHANT_KEYWORDS) {
                if (description.contains(keyword.toLowerCase())) {
                    String reason = String.format("可疑商家: %s", transaction.getDescription());
                    addSuspiciousTransaction(suspiciousTransactions, transaction, reason);
                    break;
                }
            }
        }
    }

    /**
     * 向结果映射中添加可疑交易
     */
    private void addSuspiciousTransaction(Map<Transaction, List<String>> suspiciousTransactions,
                                          Transaction transaction,
                                          String reason) {
        suspiciousTransactions.computeIfAbsent(transaction, k -> new ArrayList<>())
                .add(reason);
    }

    /**
     * 分析交易的风险等级
     * @param transaction 交易记录
     * @param reasons 可疑原因列表
     * @return 风险等级(1-5, 5为最高风险)
     */
    public int calculateRiskLevel(Transaction transaction, List<String> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            return 1; // 无风险
        }

        int baseRisk = 2; // 基础风险等级

        // 根据可疑原因数量增加风险等级
        baseRisk += Math.min(2, reasons.size());

        // 特定高风险情况
        for (String reason : reasons) {
            if (reason.contains("异常大额") && transaction.getAmount().compareTo(new BigDecimal("5000")) > 0) {
                baseRisk += 1;
            }
            if (reason.contains("可疑商家") && reason.toLowerCase().contains("海外")) {
                baseRisk += 1;
            }
        }

        // 限制最高风险等级为5
        return Math.min(5, baseRisk);
    }

    /**
     * 生成风险等级描述
     * @param riskLevel 风险等级(1-5)
     * @return 风险描述
     */
    public String getRiskLevelDescription(int riskLevel) {
        switch (riskLevel) {
            case 1: return "安全";
            case 2: return "低风险";
            case 3: return "中风险";
            case 4: return "高风险";
            case 5: return "极高风险";
            default: return "未知风险";
        }
    }
}