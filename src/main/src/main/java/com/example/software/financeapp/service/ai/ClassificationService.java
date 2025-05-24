package com.example.software.financeapp.service.ai;

import com.example.software.financeapp.model.entity.Category;
import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.service.ApiService;
import com.example.software.financeapp.service.MockDataService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分类服务 - 管理交易自动分类和学习
 */
public class ClassificationService {

    // 分类器实例
    private final TransactionClassifier classifier;

    // API服务
    private final ApiService apiService;

    // 分类历史记录 - 用于分析和学习
    private final Map<Long, TransactionClassifier.ClassificationResult> classificationHistory;

    // 用户反馈数据 - 用于改进分类准确性
    private final Map<String, Integer> keywordStrengthMap;

    /**
     * 构造函数
     * @param apiService API服务
     */
    public ClassificationService(ApiService apiService) {
        this.apiService = apiService;
        this.classifier = new TransactionClassifier();
        this.classificationHistory = new HashMap<>();
        this.keywordStrengthMap = new HashMap<>();
    }

    /**
     * 对新交易进行自动分类
     * @param transaction 需要分类的交易
     * @return 分类后的交易
     */
    public Transaction classifyTransaction(Transaction transaction) {
        // 如果交易已经有确认的类别，不进行自动分类
        if (transaction.isCategoryConfirmed() && transaction.getCategory() != null) {
            return transaction;
        }

        // 使用分类器对交易进行分类
        TransactionClassifier.ClassificationResult result = classifier.classifyTransaction(transaction);

        // 保存分类历史
        classificationHistory.put(transaction.getId(), result);

        // 设置分类结果
        transaction.setCategory(result.getCategory());
        // 标记为AI分类，未经用户确认
        transaction.setCategoryConfirmed(false);

        // 记录分类信息（可用于调试和改进）
        System.out.println("自动分类交易 ID=" + transaction.getId() +
                ", 分配类别: " + result.getCategory().getName() +
                ", 置信度: " + result.getConfidence() +
                ", 原因: " + result.getReason());

        return transaction;
    }

    /**
     * 批量对交易进行分类
     * @param transactions 交易列表
     * @return 分类后的交易列表
     */
    public List<Transaction> classifyTransactions(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            if (transaction.getCategory() == null || !transaction.isCategoryConfirmed()) {
                classifyTransaction(transaction);
            }
        }
        return transactions;
    }

    /**
     * 处理用户对分类的反馈（用户确认或修改类别）
     * @param transactionId 交易ID
     * @param categoryId 用户选择的类别ID
     * @return 更新后的交易
     */
    public Transaction processCategoryFeedback(Long transactionId, Long categoryId) throws IOException {
        // 获取交易
        Transaction transaction = MockDataService.findTransactionById(transactionId);
        if (transaction == null) {
            throw new IllegalArgumentException("交易不存在: ID=" + transactionId);
        }

        // 获取用户选择的类别
        Category category = MockDataService.findCategoryById(categoryId);
        if (category == null) {
            throw new IllegalArgumentException("类别不存在: ID=" + categoryId);
        }

        // 获取原始分类结果
        TransactionClassifier.ClassificationResult originalResult = classificationHistory.get(transactionId);

        // 如果原始分类与用户选择不同，记录这次学习
        if (originalResult != null &&
                (originalResult.getCategory() == null ||
                        !originalResult.getCategory().getId().equals(categoryId))) {
            // 这里可以实现更复杂的学习逻辑，例如调整关键词权重
            learnFromFeedback(transaction, category, originalResult.getCategory());
        }

        // 更新交易类别
        transaction.setCategory(category);
        transaction.setCategoryConfirmed(true);

        // 保存更新
        return apiService.updateTransaction(transaction);
    }

    /**
     * 从用户反馈中学习（简化版）
     * @param transaction 交易
     * @param userCategory 用户选择的类别
     * @param aiCategory AI推荐的类别
     */
    private void learnFromFeedback(Transaction transaction, Category userCategory, Category aiCategory) {
        // 提取交易内容关键信息
        String content = (transaction.getMerchant() + " " + transaction.getDescription()).toLowerCase();

        // 为用户选择的类别增强关键词权重
        if (userCategory.getKeywords() != null) {
            String[] keywords = userCategory.getKeywords().split(",");
            for (String keyword : keywords) {
                String key = userCategory.getId() + ":" + keyword.trim();
                keywordStrengthMap.put(key, keywordStrengthMap.getOrDefault(key, 0) + 1);
            }
        }

        // 打印学习信息
        System.out.println("从用户反馈学习: 交易 ID=" + transaction.getId() +
                ", AI推荐: " + (aiCategory != null ? aiCategory.getName() : "无") +
                ", 用户选择: " + userCategory.getName());

        // 这里是简化版实现，实际应用中可以：
        // 1. 保存学习结果到数据库
        // 2. 定期重新训练模型
        // 3. 实现更复杂的算法，例如贝叶斯分类器或者机器学习模型
    }

    /**
     * 获取特定交易的分类置信度
     * @param transactionId 交易ID
     * @return 置信度，如果未找到则返回0
     */
    public double getClassificationConfidence(Long transactionId) {
        TransactionClassifier.ClassificationResult result = classificationHistory.get(transactionId);
        return result != null ? result.getConfidence() : 0;
    }

    /**
     * 获取交易分类原因
     * @param transactionId 交易ID
     * @return 分类原因
     */
    public String getClassificationReason(Long transactionId) {
        TransactionClassifier.ClassificationResult result = classificationHistory.get(transactionId);
        return result != null ? result.getReason() : "未分类";
    }

    /**
     * 重新训练分类器
     */
    public void retrainClassifier() {
        // 在实际应用中，可以根据用户反馈和历史数据重新训练分类器
        // 例如调整关键词权重、更新分类规则等
        System.out.println("重新训练分类器 (模拟)");

        // 打印当前学习到的关键词权重
        System.out.println("当前关键词学习情况:");
        for (Map.Entry<String, Integer> entry : keywordStrengthMap.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }
}