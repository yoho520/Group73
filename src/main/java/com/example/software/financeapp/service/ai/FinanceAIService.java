package com.example.software.financeapp.service.ai;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 财务AI服务，提供财务相关问题的智能回答
 */
public class FinanceAIService {

    // 知识库：问题和对应的回答
    private Map<String, String> knowledgeBase;

    // 问题分类
    private Map<String, List<String>> categories;

    // 常见问题模式
    private List<Pattern> patterns;

    // 关键词映射
    private Map<String, List<String>> keywordMapping;

    public FinanceAIService() {
        initializeKnowledgeBase();
        initializeCategories();
        initializePatterns();
        initializeKeywordMapping();
    }

    /**
     * 初始化知识库
     */
    private void initializeKnowledgeBase() {
        knowledgeBase = new HashMap<>();

        // 预算相关
        knowledgeBase.put("budget_create", "创建预算可以在'预算管理'页面，点击'新建预算'按钮。您可以设置总预算金额，并为不同类别分配资金。");
        knowledgeBase.put("budget_adjust", "调整预算可以在'预算管理'页面，选择要修改的预算，点击'编辑'按钮进行修改。");
        knowledgeBase.put("budget_exceed", "超出预算时系统会发送通知提醒您。您可以在'预算管理'页面查看详细情况，并决定是否调整预算或减少支出。");
        knowledgeBase.put("budget_types", "本系统支持月度预算和自定义周期预算。您可以针对不同消费类别（如餐饮、交通、娱乐等）设置子预算。");

        // 支出分析相关
        knowledgeBase.put("analysis_view", "您可以在'支出分析'页面查看支出报表和图表。系统提供饼图、柱状图和趋势线等多种可视化方式。");
        knowledgeBase.put("analysis_categories", "系统默认的支出类别包括：餐饮、交通、住房、娱乐、购物、医疗、教育等。您也可以创建自定义类别。");
        knowledgeBase.put("analysis_export", "导出报表可以在'支出分析'页面，点击右上角的'导出'按钮，选择导出格式（PDF、Excel或CSV）。");
        knowledgeBase.put("analysis_period", "您可以选择日、周、月、季度和年度等不同时间段进行数据分析，在'支出分析'页面的顶部有时间范围选择器。");

        // 储蓄规划相关
        knowledgeBase.put("savings_goal", "设置储蓄目标可以在'智能储蓄计划'页面，点击'新建目标'按钮。您需要设置目标名称、金额和目标日期。");
        knowledgeBase.put("savings_track", "跟踪储蓄进度可以在'智能储蓄计划'页面查看。每个目标都有进度条和预计完成日期。");
        knowledgeBase.put("savings_suggestion", "系统会根据您的收入和支出情况，提供个性化的储蓄建议，包括每月应存金额和分配比例。");
        knowledgeBase.put("savings_tiers", "分层储蓄是将储蓄分为不同优先级层次（如紧急基金、短期目标、长期投资），帮助您更有策略地管理储蓄。");

        // 本地化财务适配相关
        knowledgeBase.put("local_city", "您可以在'本地化财务适配'页面选择您的所在城市，系统会根据当地消费水平和特点提供个性化建议。");
        knowledgeBase.put("local_holiday", "系统集成了中国法定节假日日历，会提醒您节假日临近及其可能带来的消费变化。");
        knowledgeBase.put("local_consumption", "不同城市有不同的消费特点，例如北京住房成本高，成都餐饮比例大，系统会据此调整预算建议。");
        knowledgeBase.put("local_season", "系统会根据季节变化提供消费建议，如冬季取暖、夏季防暑和换季消费高峰等。");

        // 账户和交易相关
        knowledgeBase.put("transaction_add", "添加新交易可以在'交易记录'页面，点击'新增交易'按钮。您需要填写金额、日期、类别和备注等信息。");
        knowledgeBase.put("transaction_edit", "编辑交易记录可以在'交易记录'页面，找到要修改的交易，点击'编辑'按钮进行修改。");
        knowledgeBase.put("transaction_delete", "删除交易记录可以在'交易记录'页面，找到要删除的交易，点击'删除'按钮。删除前会要求确认。");
        knowledgeBase.put("transaction_recurrent", "设置周期性交易可以在添加交易时勾选'周期性交易'选项，并设置重复频率（如每周、每月）。");

        // 账户管理相关
        knowledgeBase.put("account_add", "添加新账户可以在'账户管理'页面，点击'添加账户'按钮。您需要选择账户类型并填写相关信息。");
        knowledgeBase.put("account_types", "系统支持多种账户类型，包括现金账户、银行卡、信用卡、投资账户、数字钱包等。");
        knowledgeBase.put("account_sync", "同步银行账户交易需要先在'账户管理'页面添加您的银行账户，然后授权系统访问您的交易数据。");
        knowledgeBase.put("account_view", "查看所有账户余额可以在'仪表盘'页面的账户概览区域或'账户管理'页面。");

        // 系统使用相关
        knowledgeBase.put("system_start", "新用户建议先添加您的账户信息，然后记录日常交易，再设置预算和储蓄目标，最后使用分析功能查看财务状况。");
        knowledgeBase.put("system_data", "您的数据只存储在本地设备上，不会上传到云端，除非您启用了云备份功能。");
        knowledgeBase.put("system_backup", "备份数据可以在'设置'页面，点击'备份与恢复'选项，然后选择'创建备份'。");
        knowledgeBase.put("system_restore", "恢复数据可以在'设置'页面，点击'备份与恢复'选项，然后选择'从备份恢复'并选择备份文件。");

        // 财务建议相关
        knowledgeBase.put("advice_saving", "财务专家建议将收入的20%用于储蓄和投资，50%用于必要开支，30%用于个人消费。");
        knowledgeBase.put("advice_emergency", "建立应急基金是理财的第一步，通常建议存够3-6个月的生活费作为紧急备用金。");
        knowledgeBase.put("advice_debt", "如果您有多笔债务，建议先偿还高利率的债务（如信用卡），再处理低利率债务（如房贷）。");
        knowledgeBase.put("advice_invest", "投资前请确保您已建立应急基金，并了解投资产品的风险。分散投资可以降低风险。");

        // 其他常见问题
        knowledgeBase.put("help_contact", "如果您有其他问题，可以联系我们的客服邮箱：support@financeapp.com 或拨打客服热线：400-123-4567。");
        knowledgeBase.put("help_feedback", "提交反馈或建议可以在'设置'页面，点击'反馈与建议'选项，填写并提交您的意见。");
        knowledgeBase.put("help_faq", "更多常见问题可以在'帮助中心'页面查看完整的FAQ列表。");
        knowledgeBase.put("help_tutorial", "软件使用教程可以在'帮助中心'页面查看视频教程和分步指南。");
    }

    /**
     * 初始化问题分类
     */
    private void initializeCategories() {
        categories = new HashMap<>();

        // 预算管理类问题
        categories.put("预算管理", Arrays.asList(
                "如何创建预算", "怎么设置预算", "预算如何修改", "如何调整预算",
                "超出预算怎么办", "预算提醒", "支持哪些预算类型", "如何设置子预算",
                "预算管理功能在哪里", "预算报告怎么看"
        ));

        // 支出分析类问题
        categories.put("支出分析", Arrays.asList(
                "如何查看支出分析", "怎么看支出报表", "支出图表在哪里", "如何导出报表",
                "支持哪些支出类别", "如何自定义类别", "如何按时间段分析支出", "支出趋势怎么看",
                "如何对比不同月份支出", "消费结构分析"
        ));

        // 储蓄规划类问题
        categories.put("储蓄规划", Arrays.asList(
                "如何设置储蓄目标", "怎么跟踪储蓄进度", "储蓄建议如何生成", "什么是分层储蓄",
                "如何修改储蓄目标", "储蓄目标完成时间", "如何增加储蓄进度", "智能储蓄计划在哪里",
                "如何设置多个储蓄目标", "如何优先分配储蓄资金"
        ));

        // 本地化适配类问题
        categories.put("本地化适配", Arrays.asList(
                "如何设置所在城市", "节假日提醒功能", "城市消费特点", "季节消费建议",
                "如何获得本地化建议", "消费高峰期预测", "不同城市预算差异", "如何更改城市设置",
                "行业消费特点", "本地化财务适配在哪里"
        ));

        // 交易记录类问题
        categories.put("交易记录", Arrays.asList(
                "如何添加交易", "怎么编辑交易", "如何删除交易记录", "如何设置周期性交易",
                "交易分类怎么改", "批量导入交易", "交易搜索功能", "交易记录导出",
                "如何筛选交易", "交易附加图片票据"
        ));

        // 账户管理类问题
        categories.put("账户管理", Arrays.asList(
                "如何添加账户", "支持哪些账户类型", "如何同步银行交易", "怎么查看账户余额",
                "如何管理多个账户", "账户转账记录", "账户隐藏功能", "如何关联信用卡",
                "账户余额调整", "账户删除"
        ));

        // 系统使用类问题
        categories.put("系统使用", Arrays.asList(
                "新用户如何开始使用", "数据安全问题", "如何备份数据", "如何恢复数据",
                "如何更改密码", "数据加密方式", "如何清除数据", "忘记密码怎么办",
                "如何设置提醒", "如何个性化软件设置"
        ));

        // 财务建议类问题
        categories.put("财务建议", Arrays.asList(
                "储蓄比例建议", "应急基金建议", "债务管理建议", "投资建议",
                "如何制定财务计划", "收入分配比例", "如何控制支出", "信用卡使用建议",
                "理财产品推荐", "如何提高财务健康度"
        ));

        // 帮助与反馈类问题
        categories.put("帮助与反馈", Arrays.asList(
                "如何联系客服", "怎么提交反馈", "在哪里查看FAQ", "使用教程在哪里",
                "如何报告问题", "更新日志在哪看", "新功能介绍", "如何评价软件",
                "社区讨论在哪里", "如何参与内测"
        ));
    }

    /**
     * 初始化问题模式
     */
    private void initializePatterns() {
        patterns = new ArrayList<>();

        // 预算相关模式
        patterns.add(Pattern.compile(".*(?:如何|怎么|怎样).*(?:创建|设置|制定).*预算.*"));
        patterns.add(Pattern.compile(".*(?:预算).*(?:修改|调整|更改).*"));
        patterns.add(Pattern.compile(".*(?:超出|超过).*(?:预算).*"));
        patterns.add(Pattern.compile(".*(?:预算).*(?:类型|种类).*"));

        // 支出分析相关模式
        patterns.add(Pattern.compile(".*(?:如何|怎么|哪里).*(?:查看|看|找).*(?:支出分析|支出报表|消费分析).*"));
        patterns.add(Pattern.compile(".*(?:支出|消费).*(?:类别|分类|种类).*"));
        patterns.add(Pattern.compile(".*(?:导出|下载).*(?:报表|数据|分析).*"));
        patterns.add(Pattern.compile(".*(?:时间段|周期|日期范围).*(?:分析|统计).*"));

        // 储蓄相关模式
        patterns.add(Pattern.compile(".*(?:如何|怎么).*(?:设置|创建).*(?:储蓄目标|储蓄计划).*"));
        patterns.add(Pattern.compile(".*(?:跟踪|查看).*(?:储蓄进度|储蓄情况).*"));
        patterns.add(Pattern.compile(".*(?:储蓄建议|储蓄推荐).*"));
        patterns.add(Pattern.compile(".*(?:分层储蓄|储蓄层级|储蓄优先级).*"));

        // 本地化适配相关模式
        patterns.add(Pattern.compile(".*(?:设置|选择|更改).*(?:城市|地区|位置).*"));
        patterns.add(Pattern.compile(".*(?:节假日|法定假日|假期).*(?:提醒|预测|消费).*"));
        patterns.add(Pattern.compile(".*(?:城市|地区).*(?:消费特点|消费习惯|消费水平).*"));
        patterns.add(Pattern.compile(".*(?:季节|时令|春夏秋冬).*(?:消费|支出|花钱).*"));

        // 交易相关模式
        patterns.add(Pattern.compile(".*(?:如何|怎么).*(?:添加|记录|输入).*(?:交易|消费|支出|收入).*"));
        patterns.add(Pattern.compile(".*(?:编辑|修改).*(?:交易|记录).*"));
        patterns.add(Pattern.compile(".*(?:删除|移除).*(?:交易|记录).*"));
        patterns.add(Pattern.compile(".*(?:周期性|定期|重复).*(?:交易|消费|支出|收入).*"));

        // 账户相关模式
        patterns.add(Pattern.compile(".*(?:如何|怎么).*(?:添加|新建).*(?:账户|账号).*"));
        patterns.add(Pattern.compile(".*(?:账户|账号).*(?:类型|种类).*"));
        patterns.add(Pattern.compile(".*(?:同步|连接|关联).*(?:银行|账户).*(?:交易|数据).*"));
        patterns.add(Pattern.compile(".*(?:查看|显示).*(?:账户余额|余额|资金).*"));

        // 系统使用相关模式
        patterns.add(Pattern.compile(".*(?:新用户|开始使用|入门|新手).*"));
        patterns.add(Pattern.compile(".*(?:数据|信息).*(?:安全|隐私|保密).*"));
        patterns.add(Pattern.compile(".*(?:如何|怎么).*(?:备份|保存).*(?:数据|信息).*"));
        patterns.add(Pattern.compile(".*(?:恢复|还原).*(?:数据|信息|备份).*"));

        // 财务建议相关模式
        patterns.add(Pattern.compile(".*(?:储蓄|存钱).*(?:比例|百分比|多少).*"));
        patterns.add(Pattern.compile(".*(?:应急基金|紧急资金|备用金).*"));
        patterns.add(Pattern.compile(".*(?:债务|欠款|贷款).*(?:管理|偿还|处理).*"));
        patterns.add(Pattern.compile(".*(?:投资|理财).*(?:建议|推荐|方式).*"));

        // 帮助与反馈相关模式
        patterns.add(Pattern.compile(".*(?:联系|找|咨询).*(?:客服|客户服务|帮助).*"));
        patterns.add(Pattern.compile(".*(?:提交|提供|给出).*(?:反馈|建议|意见).*"));
        patterns.add(Pattern.compile(".*(?:常见问题|FAQ|问答).*"));
        patterns.add(Pattern.compile(".*(?:教程|指南|指导|攻略).*"));
    }

    /**
     * 初始化关键词映射
     */
    private void initializeKeywordMapping() {
        keywordMapping = new HashMap<>();

        // 预算相关关键词
        keywordMapping.put("budget_create", Arrays.asList("创建预算", "设置预算", "制定预算", "新建预算", "预算创建", "预算设置"));
        keywordMapping.put("budget_adjust", Arrays.asList("调整预算", "修改预算", "更改预算", "编辑预算", "预算调整", "预算修改"));
        keywordMapping.put("budget_exceed", Arrays.asList("超出预算", "超过预算", "预算超支", "预算不够", "预算不足", "预算警告"));
        keywordMapping.put("budget_types", Arrays.asList("预算类型", "预算种类", "子预算", "类别预算", "多种预算", "预算分类"));

        // 支出分析相关关键词
        keywordMapping.put("analysis_view", Arrays.asList("查看支出", "支出分析", "消费分析", "支出报表", "支出图表", "消费统计", "开支分析"));
        keywordMapping.put("analysis_categories", Arrays.asList("支出类别", "消费类别", "开支分类", "支出分类", "消费分类", "类别设置"));
        keywordMapping.put("analysis_export", Arrays.asList("导出报表", "导出数据", "下载报表", "保存报表", "报表导出", "数据导出"));
        keywordMapping.put("analysis_period", Arrays.asList("时间段分析", "周期分析", "日期范围", "时间范围", "按月分析", "按周分析", "按年分析"));

        // 储蓄规划相关关键词
        keywordMapping.put("savings_goal", Arrays.asList("储蓄目标", "存钱目标", "存款目标", "储蓄计划", "储蓄设置", "目标储蓄"));
        keywordMapping.put("savings_track", Arrays.asList("储蓄进度", "储蓄跟踪", "储蓄监控", "储蓄情况", "存钱进度", "目标进度"));
        keywordMapping.put("savings_suggestion", Arrays.asList("储蓄建议", "储蓄推荐", "存钱建议", "储蓄方案", "储蓄策略", "如何储蓄"));
        keywordMapping.put("savings_tiers", Arrays.asList("分层储蓄", "储蓄层级", "储蓄优先级", "储蓄分配", "储蓄划分", "多层储蓄"));

        // 本地化财务适配相关关键词
        keywordMapping.put("local_city", Arrays.asList("城市设置", "选择城市", "地区设置", "所在城市", "城市选择", "地区选择", "位置设置"));
        keywordMapping.put("local_holiday", Arrays.asList("节假日", "法定假日", "假期", "节日消费", "假日提醒", "节假日预测"));
        keywordMapping.put("local_consumption", Arrays.asList("城市消费", "地区消费", "消费特点", "消费习惯", "地方特色", "消费水平"));
        keywordMapping.put("local_season", Arrays.asList("季节消费", "季节变化", "时令消费", "季节支出", "春夏秋冬", "季节特点"));

        // 交易相关关键词
        keywordMapping.put("transaction_add", Arrays.asList("添加交易", "记录交易", "新增交易", "输入交易", "记账", "添加支出", "添加收入"));
        keywordMapping.put("transaction_edit", Arrays.asList("编辑交易", "修改交易", "更改交易", "交易编辑", "修改记录", "调整交易"));
        keywordMapping.put("transaction_delete", Arrays.asList("删除交易", "移除交易", "清除交易", "交易删除", "删除记录", "删除消费"));
        keywordMapping.put("transaction_recurrent", Arrays.asList("周期性交易", "定期交易", "重复交易", "自动交易", "固定交易", "循环交易"));

        // 账户相关关键词
        keywordMapping.put("account_add", Arrays.asList("添加账户", "新建账户", "创建账户", "账户添加", "新增账户", "账号添加"));
        keywordMapping.put("account_types", Arrays.asList("账户类型", "账号种类", "账户种类", "支持账户", "账户分类", "不同账户"));
        keywordMapping.put("account_sync", Arrays.asList("同步账户", "同步交易", "连接银行", "银行关联", "导入交易", "自动同步"));
        keywordMapping.put("account_view", Arrays.asList("查看余额", "账户余额", "资金余额", "余额查询", "账户总览", "资金总额"));

        // 系统使用相关关键词
        keywordMapping.put("system_start", Arrays.asList("开始使用", "新用户", "入门指南", "使用指南", "新手教程", "如何开始"));
        keywordMapping.put("system_data", Arrays.asList("数据安全", "信息安全", "隐私保护", "数据保密", "安全问题", "个人信息"));
        keywordMapping.put("system_backup", Arrays.asList("数据备份", "备份信息", "保存数据", "备份方法", "如何备份", "创建备份"));
        keywordMapping.put("system_restore", Arrays.asList("恢复数据", "还原数据", "从备份恢复", "数据还原", "恢复备份", "恢复信息"));

        // 财务建议相关关键词
        keywordMapping.put("advice_saving", Arrays.asList("储蓄比例", "存钱比例", "收入分配", "储蓄百分比", "应该存多少", "合理储蓄"));
        keywordMapping.put("advice_emergency", Arrays.asList("应急基金", "紧急资金", "备用金", "安全垫", "紧急备用", "风险准备金"));
        keywordMapping.put("advice_debt", Arrays.asList("债务管理", "欠款处理", "贷款偿还", "信用卡债务", "债务优先级", "欠款偿还"));
        keywordMapping.put("advice_invest", Arrays.asList("投资建议", "理财建议", "投资方式", "理财产品", "投资策略", "如何投资"));

        // 帮助与反馈相关关键词
        keywordMapping.put("help_contact", Arrays.asList("联系客服", "客户服务", "联系方式", "客服电话", "客服邮箱", "寻求帮助"));
        keywordMapping.put("help_feedback", Arrays.asList("提交反馈", "提供建议", "意见反馈", "问题反馈", "用户建议", "功能建议"));
        keywordMapping.put("help_faq", Arrays.asList("常见问题", "FAQ", "问答集", "问题解答", "常见疑问", "问题指南"));
        keywordMapping.put("help_tutorial", Arrays.asList("使用教程", "指南", "教学", "操作指导", "使用指导", "功能教程"));
    }

    /**
     * 根据用户问题提供智能回答
     * @param question 用户问题
     * @return AI回答
     */
    public String getAnswer(String question) {
        if (question == null || question.trim().isEmpty()) {
            return "请输入您的问题，我会尽力回答。";
        }

        // 对问题进行预处理
        String processedQuestion = preprocessQuestion(question);

        // 尝试直接匹配问题模式
        String patternMatchResult = matchPattern(processedQuestion);
        if (patternMatchResult != null) {
            return patternMatchResult;
        }

        // 尝试关键词匹配
        String keywordMatchResult = matchKeywords(processedQuestion);
        if (keywordMatchResult != null) {
            return keywordMatchResult;
        }

        // 如果没有找到匹配，返回默认回答
        return getDefaultAnswer(processedQuestion);
    }

    /**
     * 预处理问题
     * @param question 原始问题
     * @return 预处理后的问题
     */
    private String preprocessQuestion(String question) {
        // 转换为小写
        String result = question.toLowerCase();

        // 去除标点符号
        result = result.replaceAll("[,.?!;:\"']", " ");

        // 去除多余空格
        result = result.replaceAll("\\s+", " ").trim();

        return result;
    }

    /**
     * 匹配问题模式
     * @param question 预处理后的问题
     * @return 匹配到的回答，如果没匹配到则返回null
     */
    private String matchPattern(String question) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(question).matches()) {
                // 找到问题中的关键词
                for (Map.Entry<String, List<String>> entry : keywordMapping.entrySet()) {
                    for (String keyword : entry.getValue()) {
                        if (question.contains(keyword)) {
                            return knowledgeBase.get(entry.getKey());
                        }
                    }
                }
            }
        }
        return null;
    }
    /**
     * 关键词匹配
     * @param question 预处理后的问题
     * @return 匹配到的回答，如果没匹配到则返回null
     */
    private String matchKeywords(String question) {
        // 计算每个知识点的匹配分数
        Map<String, Integer> scores = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : keywordMapping.entrySet()) {
            String key = entry.getKey();
            List<String> keywords = entry.getValue();

            int score = 0;
            for (String keyword : keywords) {
                if (question.contains(keyword)) {
                    // 匹配到关键词，增加分数
                    score += 1;

                    // 如果是完全匹配的关键词，额外加分
                    if (question.equals(keyword)) {
                        score += 5;
                    }
                }
            }

            if (score > 0) {
                scores.put(key, score);
            }
        }

        // 如果有匹配的知识点，返回得分最高的
        if (!scores.isEmpty()) {
            String bestMatch = Collections.max(scores.entrySet(), Map.Entry.comparingByValue()).getKey();
            return knowledgeBase.get(bestMatch);
        }

        return null;
    }

    /**
     * 获取默认回答
     * @param question 预处理后的问题
     * @return 默认回答
     */
    private String getDefaultAnswer(String question) {
        // 尝试判断问题类型
        for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
            String category = entry.getKey();
            List<String> categoryQuestions = entry.getValue();

            for (String categoryQuestion : categoryQuestions) {
                double similarity = calculateSimilarity(question, categoryQuestion);
                if (similarity > 0.5) {  // 如果相似度超过阈值
                    return "您似乎在询问关于" + category + "的问题。请尝试更具体地描述您的问题，以便我能提供更准确的帮助。";
                }
            }
        }

        // 如果无法判断问题类型，返回通用回答
        return "抱歉，我无法理解您的问题。请尝试用不同的方式提问，或者查看帮助中心的常见问题解答。";
    }

    /**
     * 计算两个字符串的相似度（简化版）
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 相似度 (0-1)
     */
    private double calculateSimilarity(String str1, String str2) {
        // 这里使用一个简单的方法计算相似度
        // 在实际应用中可以使用更复杂的算法，如编辑距离、余弦相似度等

        Set<String> words1 = new HashSet<>(Arrays.asList(str1.split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(str2.split("\\s+")));

        // 交集大小
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);

        // 并集大小
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);

        // Jaccard相似度
        return (double) intersection.size() / union.size();
    }

    /**
     * 获取推荐问题
     * @param category 类别，如果为null则随机推荐
     * @return 推荐问题列表
     */
    public List<String> getRecommendedQuestions(String category) {
        List<String> recommendations = new ArrayList<>();

        if (category != null && categories.containsKey(category)) {
            // 从指定类别中随机选择问题
            List<String> categoryQuestions = categories.get(category);
            Random random = new Random();
            int questionsToAdd = Math.min(3, categoryQuestions.size());

            for (int i = 0; i < questionsToAdd; i++) {
                int index = random.nextInt(categoryQuestions.size());
                recommendations.add(categoryQuestions.get(index));
                categoryQuestions.remove(index);  // 避免重复
            }
        } else {
            // 从所有类别中随机选择问题
            Random random = new Random();
            List<String> allCategories = new ArrayList<>(categories.keySet());

            int categoriesToUse = Math.min(3, allCategories.size());
            for (int i = 0; i < categoriesToUse; i++) {
                String randomCategory = allCategories.get(random.nextInt(allCategories.size()));
                List<String> categoryQuestions = categories.get(randomCategory);

                if (!categoryQuestions.isEmpty()) {
                    int questionIndex = random.nextInt(categoryQuestions.size());
                    recommendations.add(categoryQuestions.get(questionIndex));
                }
            }
        }

        return recommendations;
    }

    /**
     * 获取所有问题分类
     * @return 分类列表
     */
    public List<String> getCategories() {
        return new ArrayList<>(categories.keySet());
    }
}