package com.example.software.financeapp.service.ai;

import com.example.software.financeapp.model.entity.Category;
import com.example.software.financeapp.model.entity.Transaction;
import com.example.software.financeapp.model.enums.CategoryType;
import com.example.software.financeapp.model.enums.TransactionType;
import com.example.software.financeapp.service.MockDataService;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 交易分类器 - 使用基于规则和关键词的方法自动对交易进行分类
 * 这是一个简化版的实现，在实际应用中可以替换为更复杂的机器学习模型
 */
public class TransactionClassifier {

    // 类别关键词映射
    private final Map<String, List<Pattern>> categoryKeywordPatterns = new HashMap<>();

    // 分类置信度阈值
    private static final double CONFIDENCE_THRESHOLD = 0.6;

    /**
     * 构造函数 - 初始化分类器
     */
    public TransactionClassifier() {
        initializeCategoryKeywords();
    }

    /**
     * 初始化类别关键词
     */
    private void initializeCategoryKeywords() {
        List<Category> categories = MockDataService.getMockCategories();

        for (Category category : categories) {
            String keywords = category.getKeywords();
            if (keywords != null && !keywords.isEmpty()) {
                List<Pattern> patterns = new ArrayList<>();
                String[] keywordArray = keywords.split(",");

                for (String keyword : keywordArray) {
                    String trimmedKeyword = keyword.trim().toLowerCase();
                    // 创建不区分大小写的正则表达式模式
                    Pattern pattern = Pattern.compile(Pattern.quote(trimmedKeyword), Pattern.CASE_INSENSITIVE);
                    patterns.add(pattern);
                }

                categoryKeywordPatterns.put(category.getId().toString(), patterns);
            }
        }
    }

    /**
     * 对交易进行分类
     * @param transaction 未分类的交易
     * @return 分类结果
     */
    public ClassificationResult classifyTransaction(Transaction transaction) {
        // 1. 如果交易已有确认的类别，直接返回
        if (transaction.isCategoryConfirmed() && transaction.getCategory() != null) {
            return new ClassificationResult(
                    transaction.getCategory(),
                    1.0,
                    "User confirmed category"
            );
        }

        // 2. 根据交易类型（收入/支出）筛选符合条件的类别
        List<Category> eligibleCategories = filterCategoriesByType(transaction.getType());

        // 3. 如果没有合适的类别，返回默认类别
        if (eligibleCategories.isEmpty()) {
            Category defaultCategory = getDefaultCategory(transaction.getType());
            return new ClassificationResult(
                    defaultCategory,
                    0.5,
                    "Default category assigned"
            );
        }

        // 4. 分析交易内容以确定最可能的类别
        return findBestMatchingCategory(transaction, eligibleCategories);
    }

    /**
     * 根据交易类型筛选类别
     * @param transactionType 交易类型
     * @return 符合类型的类别列表
     */
    private List<Category> filterCategoriesByType(TransactionType transactionType) {
        List<Category> categories = MockDataService.getMockCategories();
        List<Category> filteredCategories = new ArrayList<>();

        CategoryType categoryType = (transactionType == TransactionType.INCOME)
                ? CategoryType.INCOME : CategoryType.EXPENSE;

        for (Category category : categories) {
            if (category.getType() == categoryType) {
                filteredCategories.add(category);
            }
        }

        return filteredCategories;
    }

    /**
     * 获取默认类别
     * @param transactionType 交易类型
     * @return 默认类别
     */
    private Category getDefaultCategory(TransactionType transactionType) {
        List<Category> categories = MockDataService.getMockCategories();
        CategoryType categoryType = (transactionType == TransactionType.INCOME)
                ? CategoryType.INCOME : CategoryType.EXPENSE;

        // 查找系统默认类别
        for (Category category : categories) {
            if (category.getType() == categoryType && category.isSystemDefault()) {
                return category;
            }
        }

        // 如果没有系统默认类别，返回第一个匹配类型的类别
        for (Category category : categories) {
            if (category.getType() == categoryType) {
                return category;
            }
        }

        // 如果没有匹配类型的类别，创建一个新类别
        return Category.builder()
                .id(-1L)
                .name(transactionType == TransactionType.INCOME ? "其他收入" : "其他支出")
                .description("系统创建的默认类别")
                .type(categoryType)
                .systemDefault(true)
                .build();
    }

    /**
     * 查找最匹配的类别
     * @param transaction 交易
     * @param eligibleCategories 符合条件的类别列表
     * @return 分类结果
     */
    private ClassificationResult findBestMatchingCategory(Transaction transaction, List<Category> eligibleCategories) {
        // 合并交易商家和描述信息用于匹配
        String content = (transaction.getMerchant() + " " + transaction.getDescription()).toLowerCase();

        Category bestCategory = null;
        double highestConfidence = 0;
        String reason = "";

        // 对每个类别计算匹配度
        for (Category category : eligibleCategories) {
            List<Pattern> patterns = categoryKeywordPatterns.get(category.getId().toString());

            if (patterns != null && !patterns.isEmpty()) {
                int matchCount = 0;

                for (Pattern pattern : patterns) {
                    if (pattern.matcher(content).find()) {
                        matchCount++;
                    }
                }

                // 计算置信度
                double confidence = patterns.isEmpty() ? 0 : (double) matchCount / patterns.size();

                if (confidence > highestConfidence) {
                    highestConfidence = confidence;
                    bestCategory = category;
                    reason = "Matched " + matchCount + " keywords";
                }
            }
        }

        // 如果没有找到足够高置信度的类别，使用默认类别
        if (bestCategory == null || highestConfidence < CONFIDENCE_THRESHOLD) {
            Category defaultCategory = getDefaultCategory(transaction.getType());
            return new ClassificationResult(
                    defaultCategory,
                    Math.max(0.5, highestConfidence),
                    "No strong keyword matches found"
            );
        }

        return new ClassificationResult(bestCategory, highestConfidence, reason);
    }

    /**
     * 分类结果类
     */
    public static class ClassificationResult {
        private final Category category;
        private final double confidence;
        private final String reason;

        public ClassificationResult(Category category, double confidence, String reason) {
            this.category = category;
            this.confidence = confidence;
            this.reason = reason;
        }

        public Category getCategory() {
            return category;
        }

        public double getConfidence() {
            return confidence;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public String toString() {
            return "ClassificationResult{" +
                    "category=" + category.getName() +
                    ", confidence=" + confidence +
                    ", reason='" + reason + '\'' +
                    '}';
        }
    }
}