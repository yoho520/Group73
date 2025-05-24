package com.example.software.financeapp.service.localization;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 地区消费特点适配服务
 * 提供不同地区的消费特点分析和适配
 */
public class RegionalConsumptionService {

    // 存储城市消费数据
    private Map<String, CityConsumptionProfile> cityProfiles;

    // 存储行业消费标准
    private Map<String, IndustryStandard> industryStandards;

    // 中国法定节假日服务
    private ChineseHolidayService holidayService;

    public RegionalConsumptionService() {
        this.holidayService = new ChineseHolidayService();
        initializeCityProfiles();
        initializeIndustryStandards();
    }

    /**
     * 初始化城市消费特点数据
     */
    private void initializeCityProfiles() {
        cityProfiles = new HashMap<>();

        // 北京消费特点
        cityProfiles.put("北京", new CityConsumptionProfile(
                "北京",
                "一线城市",
                new BigDecimal("1.5"),  // 生活成本倍数（相对全国平均）
                new HashMap<String, BigDecimal>() {{
                    put("住房", new BigDecimal("0.40"));
                    put("餐饮", new BigDecimal("0.15"));
                    put("交通", new BigDecimal("0.10"));
                    put("教育", new BigDecimal("0.12"));
                    put("娱乐", new BigDecimal("0.08"));
                    put("购物", new BigDecimal("0.10"));
                    put("医疗", new BigDecimal("0.05"));
                }},
                "互联网科技、金融服务、文化产业、教育培训",
                Arrays.asList("高房价", "教育投入大", "生活节奏快", "消费品牌化")
        ));

        // 上海消费特点
        cityProfiles.put("上海", new CityConsumptionProfile(
                "上海",
                "一线城市",
                new BigDecimal("1.6"),
                new HashMap<String, BigDecimal>() {{
                    put("住房", new BigDecimal("0.42"));
                    put("餐饮", new BigDecimal("0.14"));
                    put("交通", new BigDecimal("0.09"));
                    put("教育", new BigDecimal("0.11"));
                    put("娱乐", new BigDecimal("0.09"));
                    put("购物", new BigDecimal("0.11"));
                    put("医疗", new BigDecimal("0.04"));
                }},
                "金融服务、商业贸易、时尚产业、国际教育",
                Arrays.asList("商业氛围浓厚", "国际化消费", "品质生活追求", "时尚潮流引领")
        ));

        // 广州消费特点
        cityProfiles.put("广州", new CityConsumptionProfile(
                "广州",
                "一线城市",
                new BigDecimal("1.3"),
                new HashMap<String, BigDecimal>() {{
                    put("住房", new BigDecimal("0.35"));
                    put("餐饮", new BigDecimal("0.18"));
                    put("交通", new BigDecimal("0.08"));
                    put("教育", new BigDecimal("0.10"));
                    put("娱乐", new BigDecimal("0.10"));
                    put("购物", new BigDecimal("0.12"));
                    put("医疗", new BigDecimal("0.07"));
                }},
                "贸易物流、餐饮美食、批发零售",
                Arrays.asList("美食文化浓厚", "商贸发达", "消费实惠", "轻奢品消费增长")
        ));

        // 深圳消费特点
        cityProfiles.put("深圳", new CityConsumptionProfile(
                "深圳",
                "一线城市",
                new BigDecimal("1.4"),
                new HashMap<String, BigDecimal>() {{
                    put("住房", new BigDecimal("0.38"));
                    put("餐饮", new BigDecimal("0.14"));
                    put("交通", new BigDecimal("0.09"));
                    put("教育", new BigDecimal("0.11"));
                    put("娱乐", new BigDecimal("0.10"));
                    put("购物", new BigDecimal("0.10"));
                    put("医疗", new BigDecimal("0.08"));
                }},
                "科技创新、电子制造、互联网、新兴产业",
                Arrays.asList("年轻人口占比高", "科技消费活跃", "创新消费理念", "健康生活方式")
        ));

        // 成都消费特点
        cityProfiles.put("成都", new CityConsumptionProfile(
                "成都",
                "新一线城市",
                new BigDecimal("1.1"),
                new HashMap<String, BigDecimal>() {{
                    put("住房", new BigDecimal("0.30"));
                    put("餐饮", new BigDecimal("0.22"));
                    put("交通", new BigDecimal("0.08"));
                    put("教育", new BigDecimal("0.09"));
                    put("娱乐", new BigDecimal("0.15"));
                    put("购物", new BigDecimal("0.10"));
                    put("医疗", new BigDecimal("0.06"));
                }},
                "休闲娱乐、餐饮美食、文化旅游",
                Arrays.asList("休闲消费高", "生活品质重视", "文化消费活跃", "餐饮特色鲜明")
        ));

        // 杭州消费特点
        cityProfiles.put("杭州", new CityConsumptionProfile(
                "杭州",
                "新一线城市",
                new BigDecimal("1.2"),
                new HashMap<String, BigDecimal>() {{
                    put("住房", new BigDecimal("0.35"));
                    put("餐饮", new BigDecimal("0.15"));
                    put("交通", new BigDecimal("0.08"));
                    put("教育", new BigDecimal("0.10"));
                    put("娱乐", new BigDecimal("0.12"));
                    put("购物", new BigDecimal("0.12"));
                    put("医疗", new BigDecimal("0.08"));
                }},
                "电子商务、数字经济、休闲旅游",
                Arrays.asList("线上消费发达", "品质生活追求", "文化休闲消费高", "数字化消费习惯")
        ));

        // 武汉消费特点
        cityProfiles.put("武汉", new CityConsumptionProfile(
                "武汉",
                "新一线城市",
                new BigDecimal("1.0"),
                new HashMap<String, BigDecimal>() {{
                    put("住房", new BigDecimal("0.28"));
                    put("餐饮", new BigDecimal("0.18"));
                    put("交通", new BigDecimal("0.10"));
                    put("教育", new BigDecimal("0.12"));
                    put("娱乐", new BigDecimal("0.10"));
                    put("购物", new BigDecimal("0.12"));
                    put("医疗", new BigDecimal("0.10"));
                }},
                "高等教育、汽车制造、钢铁产业、餐饮文化",
                Arrays.asList("高校消费集中", "性价比意识强", "地方美食特色", "创新开放消费")
        ));

        // 西安消费特点
        cityProfiles.put("西安", new CityConsumptionProfile(
                "西安",
                "新一线城市",
                new BigDecimal("0.95"),
                new HashMap<String, BigDecimal>() {{
                    put("住房", new BigDecimal("0.25"));
                    put("餐饮", new BigDecimal("0.20"));
                    put("交通", new BigDecimal("0.09"));
                    put("教育", new BigDecimal("0.12"));
                    put("娱乐", new BigDecimal("0.10"));
                    put("购物", new BigDecimal("0.14"));
                    put("医疗", new BigDecimal("0.10"));
                }},
                "文化旅游、高等教育、航空航天、美食",
                Arrays.asList("文化消费特色", "旅游带动消费", "传统与现代结合", "美食文化独特")
        ));

        // 更多城市...
    }

    /**
     * 初始化行业消费标准
     */
    private void initializeIndustryStandards() {
        industryStandards = new HashMap<>();

        // IT/互联网行业消费标准
        industryStandards.put("IT/互联网", new IndustryStandard(
                "IT/互联网",
                new BigDecimal("1.3"),
                new HashMap<String, BigDecimal>() {{
                    put("数码产品", new BigDecimal("0.15"));
                    put("餐饮", new BigDecimal("0.18"));
                    put("交通", new BigDecimal("0.08"));
                    put("住房", new BigDecimal("0.30"));
                    put("休闲娱乐", new BigDecimal("0.12"));
                    put("教育学习", new BigDecimal("0.10"));
                    put("其他", new BigDecimal("0.07"));
                }},
                Arrays.asList("数码产品消费高", "在线服务使用频繁", "工作餐消费占比大", "加班消费模式"),
                "数码产品、咖啡、外卖、线上服务订阅"
        ));

        // 金融行业消费标准
        industryStandards.put("金融", new IndustryStandard(
                "金融",
                new BigDecimal("1.4"),
                new HashMap<String, BigDecimal>() {{
                    put("服装", new BigDecimal("0.12"));
                    put("餐饮", new BigDecimal("0.20"));
                    put("交通", new BigDecimal("0.10"));
                    put("住房", new BigDecimal("0.30"));
                    put("休闲娱乐", new BigDecimal("0.15"));
                    put("教育学习", new BigDecimal("0.08"));
                    put("其他", new BigDecimal("0.05"));
                }},
                Arrays.asList("商务社交消费高", "品牌消费倾向", "出行消费档次高", "投资理财意识强"),
                "高端餐饮、品牌服装、金融产品、高端社交活动"
        ));

        // 制造业消费标准
        industryStandards.put("制造业", new IndustryStandard(
                "制造业",
                new BigDecimal("0.9"),
                new HashMap<String, BigDecimal>() {{
                    put("服装", new BigDecimal("0.08"));
                    put("餐饮", new BigDecimal("0.22"));
                    put("交通", new BigDecimal("0.12"));
                    put("住房", new BigDecimal("0.28"));
                    put("休闲娱乐", new BigDecimal("0.10"));
                    put("教育学习", new BigDecimal("0.15"));
                    put("其他", new BigDecimal("0.05"));
                }},
                Arrays.asList("实用性消费为主", "家庭消费占比高", "节约消费习惯", "子女教育投入大"),
                "家庭用品、子女教育、实用型交通工具"
        ));

        // 教育行业消费标准
        industryStandards.put("教育", new IndustryStandard(
                "教育",
                new BigDecimal("0.95"),
                new HashMap<String, BigDecimal>() {{
                    put("服装", new BigDecimal("0.08"));
                    put("餐饮", new BigDecimal("0.15"));
                    put("交通", new BigDecimal("0.10"));
                    put("住房", new BigDecimal("0.28"));
                    put("休闲娱乐", new BigDecimal("0.09"));
                    put("教育学习", new BigDecimal("0.25"));
                    put("其他", new BigDecimal("0.05"));
                }},
                Arrays.asList("自我提升投入大", "阅读消费比例高", "文化消费活跃", "理性消费习惯"),
                "书籍、文化产品、进修课程、知识付费"
        ));

        // 医疗行业消费标准
        industryStandards.put("医疗", new IndustryStandard(
                "医疗",
                new BigDecimal("1.1"),
                new HashMap<String, BigDecimal>() {{
                    put("服装", new BigDecimal("0.08"));
                    put("餐饮", new BigDecimal("0.15"));
                    put("交通", new BigDecimal("0.12"));
                    put("住房", new BigDecimal("0.28"));
                    put("休闲娱乐", new BigDecimal("0.10"));
                    put("健康医疗", new BigDecimal("0.20"));
                    put("其他", new BigDecimal("0.07"));
                }},
                Arrays.asList("健康产品消费高", "工作餐消费规律", "舒缓减压消费", "健康保险意识强"),
                "健康食品、保健品、健身服务、医疗保险"
        ));

        // 更多行业...
    }

    /**
     * 获取城市消费特点
     * @param cityName 城市名称
     * @return 城市消费特点
     */
    public CityConsumptionProfile getCityProfile(String cityName) {
        return cityProfiles.getOrDefault(cityName,
                new CityConsumptionProfile(
                        "全国平均",
                        "其他城市",
                        BigDecimal.ONE,
                        new HashMap<String, BigDecimal>() {{
                            put("住房", new BigDecimal("0.30"));
                            put("餐饮", new BigDecimal("0.18"));
                            put("交通", new BigDecimal("0.10"));
                            put("教育", new BigDecimal("0.10"));
                            put("娱乐", new BigDecimal("0.10"));
                            put("购物", new BigDecimal("0.12"));
                            put("医疗", new BigDecimal("0.10"));
                        }},
                        "综合产业",
                        Arrays.asList("消费均衡", "基础生活为主")
                ));
    }

    /**
     * 获取行业消费标准
     * @param industry 行业名称
     * @return 行业消费标准
     */
    public IndustryStandard getIndustryStandard(String industry) {
        return industryStandards.getOrDefault(industry,
                new IndustryStandard(
                        "其他行业",
                        BigDecimal.ONE,
                        new HashMap<String, BigDecimal>() {{
                            put("服装", new BigDecimal("0.10"));
                            put("餐饮", new BigDecimal("0.18"));
                            put("交通", new BigDecimal("0.10"));
                            put("住房", new BigDecimal("0.30"));
                            put("休闲娱乐", new BigDecimal("0.12"));
                            put("教育学习", new BigDecimal("0.12"));
                            put("其他", new BigDecimal("0.08"));
                        }},
                        Arrays.asList("基础生活消费为主"),
                        "日常生活用品、基础服务"
                ));
    }

    /**
     * 生成个性化预算建议
     * @param cityName 城市名称
     * @param industry 行业
     * @param monthlyIncome 月收入
     * @return 个性化预算建议
     */
    public Map<String, BigDecimal> generatePersonalizedBudget(String cityName, String industry, BigDecimal monthlyIncome) {
        CityConsumptionProfile cityProfile = getCityProfile(cityName);
        IndustryStandard industryStandard = getIndustryStandard(industry);

        Map<String, BigDecimal> budget = new HashMap<>();

        // 合并城市和行业特点，生成个性化预算
        // 使用城市消费分布为基础，结合行业特点进行调整
        for (Map.Entry<String, BigDecimal> cityEntry : cityProfile.getConsumptionDistribution().entrySet()) {
            String category = cityEntry.getKey();
            BigDecimal cityRatio = cityEntry.getValue();

            // 查找行业中对应类别的比例
            BigDecimal industryRatio = null;
            for (Map.Entry<String, BigDecimal> industryEntry : industryStandard.getConsumptionDistribution().entrySet()) {
                if (industryEntry.getKey().equalsIgnoreCase(category) ||
                        isRelatedCategory(industryEntry.getKey(), category)) {
                    industryRatio = industryEntry.getValue();
                    break;
                }
            }

            // 如果没有找到对应类别，使用城市比例
            if (industryRatio == null) {
                industryRatio = cityRatio;
            }

            // 加权平均：城市因素占70%，行业因素占30%
            BigDecimal weightedRatio = cityRatio.multiply(new BigDecimal("0.7"))
                    .add(industryRatio.multiply(new BigDecimal("0.3")));

            // 计算该类别的预算金额
            BigDecimal budgetAmount = monthlyIncome.multiply(weightedRatio)
                    .setScale(2, RoundingMode.HALF_UP);

            budget.put(category, budgetAmount);
        }

        return budget;
    }

    /**
     * 判断两个消费类别是否相关
     * @param category1 类别1
     * @param category2 类别2
     * @return 是否相关
     */
    private boolean isRelatedCategory(String category1, String category2) {
        Map<String, List<String>> relatedCategories = new HashMap<>();
        relatedCategories.put("住房", Arrays.asList("房租", "住宿", "房贷"));
        relatedCategories.put("餐饮", Arrays.asList("食品", "饮料", "外卖"));
        relatedCategories.put("交通", Arrays.asList("出行", "车辆", "通勤"));
        relatedCategories.put("教育", Arrays.asList("学习", "培训", "教育学习"));
        relatedCategories.put("娱乐", Arrays.asList("休闲", "休闲娱乐", "文化"));
        relatedCategories.put("购物", Arrays.asList("服装", "数码产品", "日用品"));
        relatedCategories.put("医疗", Arrays.asList("健康", "健康医疗", "保健"));

        // 检查类别1是否与类别2相关
        if (relatedCategories.containsKey(category1)) {
            return relatedCategories.get(category1).contains(category2);
        }

        // 检查类别2是否与类别1相关
        if (relatedCategories.containsKey(category2)) {
            return relatedCategories.get(category2).contains(category1);
        }

        return false;
    }

    /**
     * 分析特定城市在特定日期的消费模式
     * @param cityName 城市名称
     * @param date 日期
     * @return 消费模式分析
     */
    public ConsumptionAnalysis analyzeConsumptionPattern(String cityName, LocalDate date) {
        CityConsumptionProfile cityProfile = getCityProfile(cityName);
        ChineseHolidayService.ConsumptionPattern datePattern = holidayService.getConsumptionPatternForDate(date);
        String season = holidayService.getSeason(date);
        ChineseHolidayService.ConsumptionPattern seasonPattern = holidayService.getSeasonalConsumptionPattern(season);

        // 构建分析结果
        ConsumptionAnalysis analysis = new ConsumptionAnalysis();
        analysis.setCityName(cityName);
        analysis.setDate(date);
        analysis.setCityTier(cityProfile.getCityTier());

        // 设置日期特点
        if (datePattern != null) {
            analysis.setDateType(datePattern.getPeriodName());
            analysis.setDateConsumptionFactor(datePattern.getConsumptionMultiplier());
            analysis.setDateConsumptionCategories(datePattern.getTopCategories());
            analysis.setDateDescription(datePattern.getDescription());
        } else {
            analysis.setDateType("普通工作日");
            analysis.setDateConsumptionFactor(1.0);
            analysis.setDateConsumptionCategories(Arrays.asList("餐饮", "交通", "日常用品"));
            analysis.setDateDescription("普通工作日消费模式，以基本生活需求为主");
        }

        // 设置季节特点
        analysis.setSeason(season);
        if (seasonPattern != null) {
            analysis.setSeasonConsumptionFactor(seasonPattern.getConsumptionMultiplier());
            analysis.setSeasonConsumptionCategories(seasonPattern.getTopCategories());
            analysis.setSeasonDescription(seasonPattern.getDescription());
        }

        // 设置城市特点
        analysis.setCityConsumptionFactor(cityProfile.getLivingCostMultiplier().doubleValue());
        analysis.setCityConsumptionDistribution(cityProfile.getConsumptionDistribution());
        analysis.setCityFeatures(cityProfile.getConsumptionFeatures());

        // 计算综合消费系数
        double combinedFactor = analysis.getCityConsumptionFactor() *
                analysis.getDateConsumptionFactor() *
                analysis.getSeasonConsumptionFactor();
        analysis.setCombinedConsumptionFactor(combinedFactor);

        // 生成综合消费建议
        analysis.setRecommendedCategories(generateRecommendedCategories(datePattern, seasonPattern, cityProfile));
        analysis.setConsumptionAdvice(generateConsumptionAdvice(analysis));

        return analysis;
    }

    /**
     * 生成综合消费类别推荐
     */
    private List<String> generateRecommendedCategories(
            ChineseHolidayService.ConsumptionPattern datePattern,
            ChineseHolidayService.ConsumptionPattern seasonPattern,
            CityConsumptionProfile cityProfile) {

        Set<String> categories = new HashSet<>();

        // 添加日期相关的消费类别
        if (datePattern != null) {
            categories.addAll(datePattern.getTopCategories());
        }

        // 添加季节相关的消费类别
        if (seasonPattern != null) {
            categories.addAll(seasonPattern.getTopCategories());
        }

        // 添加城市特色消费类别
        Map<String, BigDecimal> cityDistribution = cityProfile.getConsumptionDistribution();
        if (cityDistribution != null && !cityDistribution.isEmpty()) {
            // 获取城市消费比例最高的3个类别
            List<Map.Entry<String, BigDecimal>> topCategories = cityDistribution.entrySet().stream()
                    .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                    .limit(3)
                    .collect(Collectors.toList());

            for (Map.Entry<String, BigDecimal> entry : topCategories) {
                categories.add(entry.getKey());
            }
        }

        return new ArrayList<>(categories);
    }

    /**
     * 生成消费建议
     */
    private String generateConsumptionAdvice(ConsumptionAnalysis analysis) {
        StringBuilder advice = new StringBuilder();

        // 基于日期特点的建议
        advice.append(analysis.getDateType()).append("期间，").append(analysis.getDateDescription()).append(" ");

        // 基于季节特点的建议
        advice.append(analysis.getSeason()).append("特点：").append(analysis.getSeasonDescription()).append(" ");

        // 基于城市特点的建议
        advice.append("在").append(analysis.getCityName()).append("这样的")
                .append(analysis.getCityTier()).append("，生活成本约为全国平均水平的")
                .append(String.format("%.1f", analysis.getCityConsumptionFactor())).append("倍。");

        // 综合建议
        advice.append("\n\n综合建议：");
        if (analysis.getCombinedConsumptionFactor() > 1.5) {
            advice.append("当前时期消费压力较大，建议控制非必要开支，重点关注");
        } else if (analysis.getCombinedConsumptionFactor() > 1.2) {
            advice.append("当前时期消费水平中等偏高，建议适度消费，可以适当关注");
        } else {
            advice.append("当前时期消费水平适中，可以合理安排消费，重点关注");
        }

        // 添加推荐类别
        List<String> topCategories = analysis.getRecommendedCategories();
        if (topCategories != null && !topCategories.isEmpty()) {
            advice.append("这些类别：");
            for (int i = 0; i < topCategories.size(); i++) {
                if (i > 0) {
                    advice.append("、");
                }
                advice.append(topCategories.get(i));
            }
        }

        return advice.toString();
    }

    /**
     * 预测即将到来的消费高峰期
     * @param startDate 开始日期
     * @param daysAhead 预测天数
     * @return 消费高峰期列表
     */
    public List<ConsumptionPeakPeriod> predictConsumptionPeaks(LocalDate startDate, int daysAhead) {
        List<ConsumptionPeakPeriod> peaks = new ArrayList<>();
        LocalDate endDate = startDate.plusDays(daysAhead);

        // 查找法定节假日
        List<ChineseHolidayService.Holiday> holidays = new ArrayList<>();
        int startYear = startDate.getYear();
        int endYear = endDate.getYear();

        for (int year = startYear; year <= endYear; year++) {
            holidays.addAll(holidayService.getHolidaysForYear(year));
        }

        // 过滤在预测期内的节假日
        List<ChineseHolidayService.Holiday> relevantHolidays = holidays.stream()
                .filter(h -> {
                    LocalDate holidayEnd = h.getStartDate().plusDays(h.getDurationDays() - 1);
                    return (!h.getStartDate().isBefore(startDate) && !h.getStartDate().isAfter(endDate)) ||
                            (!holidayEnd.isBefore(startDate) && !holidayEnd.isAfter(endDate));
                })
                .collect(Collectors.toList());

        // 将节假日转换为消费高峰期
        for (ChineseHolidayService.Holiday holiday : relevantHolidays) {
            ChineseHolidayService.ConsumptionPattern pattern =
                    holidayService.getConsumptionPatternForDate(holiday.getStartDate());

            if (pattern == null) continue;

            // 调整消费高峰范围（通常节前也是消费高峰）
            LocalDate peakStart = holiday.getStartDate().minusDays(7);
            if (peakStart.isBefore(startDate)) {
                peakStart = startDate;
            }

            LocalDate peakEnd = holiday.getStartDate().plusDays(holiday.getDurationDays() - 1);
            if (peakEnd.isAfter(endDate)) {
                peakEnd = endDate;
            }

            ConsumptionPeakPeriod peak = new ConsumptionPeakPeriod(
                    holiday.getName() + "消费高峰",
                    peakStart,
                    peakEnd,
                    pattern.getConsumptionMultiplier(),
                    pattern.getTopCategories(),
                    "节假日期间消费明显增加，" + pattern.getDescription()
            );

            peaks.add(peak);
        }

        // 添加季节交替的消费高峰（如换季消费）
        addSeasonalTransitionPeaks(peaks, startDate, endDate);

        // 添加开学季等特殊时期
        addSpecialPeriodPeaks(peaks, startDate, endDate);

        return peaks;
    }

/**
 * 添加季节转换
 /**
 * 添加季节转换相关的消费高峰
 * @param peaks 消费高峰列表
 * @param startDate 开始日期
 * @param endDate 结束日期
 */
private void addSeasonalTransitionPeaks(List<ConsumptionPeakPeriod> peaks,
                                        LocalDate startDate,
                                        LocalDate endDate) {
    // 季节交替的大致时间点
    Map<String, LocalDate> seasonStartDates = new HashMap<>();

    // 遍历预测范围内的年份
    for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
        // 春季开始 (约3月初)
        seasonStartDates.put("春季" + year, LocalDate.of(year, 3, 1));

        // 夏季开始 (约6月初)
        seasonStartDates.put("夏季" + year, LocalDate.of(year, 6, 1));

        // 秋季开始 (约9月初)
        seasonStartDates.put("秋季" + year, LocalDate.of(year, 9, 1));

        // 冬季开始 (约12月初)
        seasonStartDates.put("冬季" + year, LocalDate.of(year, 12, 1));
    }

    // 过滤在预测期内的季节转换期
    for (Map.Entry<String, LocalDate> entry : seasonStartDates.entrySet()) {
        String seasonName = entry.getKey();
        LocalDate seasonStart = entry.getValue();

        // 季节转换期通常是前后半个月
        LocalDate peakStart = seasonStart.minusDays(15);
        LocalDate peakEnd = seasonStart.plusDays(15);

        // 确保在预测范围内
        if (peakEnd.isBefore(startDate) || peakStart.isAfter(endDate)) {
            continue;
        }

        // 调整范围
        if (peakStart.isBefore(startDate)) {
            peakStart = startDate;
        }
        if (peakEnd.isAfter(endDate)) {
            peakEnd = endDate;
        }

        // 获取季节特点
        String season = seasonName.replaceAll("\\d+", "");
        ChineseHolidayService.ConsumptionPattern pattern =
                holidayService.getSeasonalConsumptionPattern(season);

        if (pattern == null) continue;

        // 创建消费高峰期
        ConsumptionPeakPeriod peak = new ConsumptionPeakPeriod(
                season + "换季消费高峰",
                peakStart,
                peakEnd,
                pattern.getConsumptionMultiplier() * 1.2,  // 换季期间消费更高
                pattern.getTopCategories(),
                season + "开始，换季消费明显增加。" + pattern.getDescription()
        );

        peaks.add(peak);
    }
}

    /**
     * 添加特殊时期的消费高峰
     * @param peaks 消费高峰列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     */
    private void addSpecialPeriodPeaks(List<ConsumptionPeakPeriod> peaks,
                                       LocalDate startDate,
                                       LocalDate endDate) {
        // 特殊消费期
        Map<String, Pair<LocalDate, LocalDate>> specialPeriods = new HashMap<>();

        // 遍历预测范围内的年份
        for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
            // 开学季 (约8月20日至9月10日)
            specialPeriods.put("开学季" + year,
                    new Pair<>(LocalDate.of(year, 8, 20), LocalDate.of(year, 9, 10)));

            // 双11购物节 (11月1日至11月12日)
            specialPeriods.put("双11购物节" + year,
                    new Pair<>(LocalDate.of(year, 11, 1), LocalDate.of(year, 11, 12)));

            // 双12购物节 (12月1日至12月13日)
            specialPeriods.put("双12购物节" + year,
                    new Pair<>(LocalDate.of(year, 12, 1), LocalDate.of(year, 12, 13)));

            // 年终促销 (12月20日至12月31日)
            specialPeriods.put("年终促销" + year,
                    new Pair<>(LocalDate.of(year, 12, 20), LocalDate.of(year, 12, 31)));
        }

        // 过滤在预测期内的特殊时期
        for (Map.Entry<String, Pair<LocalDate, LocalDate>> entry : specialPeriods.entrySet()) {
            String periodName = entry.getKey();
            LocalDate periodStart = entry.getValue().getFirst();
            LocalDate periodEnd = entry.getValue().getSecond();

            // 确保在预测范围内
            if (periodEnd.isBefore(startDate) || periodStart.isAfter(endDate)) {
                continue;
            }

            // 调整范围
            if (periodStart.isBefore(startDate)) {
                periodStart = startDate;
            }
            if (periodEnd.isAfter(endDate)) {
                periodEnd = endDate;
            }

            // 设置特定时期的消费特点
            double factor = 1.0;
            List<String> categories = new ArrayList<>();
            String description = "";

            if (periodName.contains("开学季")) {
                factor = 1.5;
                categories = Arrays.asList("教育", "文具", "服装", "电子产品");
                description = "开学季消费集中在教育用品、服装和数码产品，家庭支出明显增加";
            } else if (periodName.contains("双11")) {
                factor = 2.0;
                categories = Arrays.asList("数码产品", "服装", "家居", "美妆");
                description = "双11购物节是全年最大的线上消费高峰，各类商品折扣力度大";
            } else if (periodName.contains("双12")) {
                factor = 1.7;
                categories = Arrays.asList("服装", "家居", "美妆", "食品");
                description = "双12购物节是双11后的又一消费高峰，线上线下均有促销活动";
            } else if (periodName.contains("年终")) {
                factor = 1.6;
                categories = Arrays.asList("服装", "数码产品", "礼品", "食品");
                description = "年终促销期间商场折扣力度大，年货和礼品消费增加";
            }

            // 创建消费高峰期
            ConsumptionPeakPeriod peak = new ConsumptionPeakPeriod(
                    periodName.replaceAll("\\d+", ""),
                    periodStart,
                    periodEnd,
                    factor,
                    categories,
                    description
            );

            peaks.add(peak);
        }
    }

    /**
     * 键值对辅助类
     */
    public static class Pair<T, U> {
        private final T first;
        private final U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        public T getFirst() {
            return first;
        }

        public U getSecond() {
            return second;
        }
    }

    /**
     * 城市消费特点类
     */
    public static class CityConsumptionProfile {
        private String cityName;                             // 城市名称
        private String cityTier;                             // 城市等级
        private BigDecimal livingCostMultiplier;             // 生活成本倍数（相对全国平均）
        private Map<String, BigDecimal> consumptionDistribution; // 消费分布
        private String majorIndustries;                      // 主要产业
        private List<String> consumptionFeatures;            // 消费特点

        public CityConsumptionProfile(String cityName, String cityTier, BigDecimal livingCostMultiplier,
                                      Map<String, BigDecimal> consumptionDistribution, String majorIndustries,
                                      List<String> consumptionFeatures) {
            this.cityName = cityName;
            this.cityTier = cityTier;
            this.livingCostMultiplier = livingCostMultiplier;
            this.consumptionDistribution = consumptionDistribution;
            this.majorIndustries = majorIndustries;
            this.consumptionFeatures = consumptionFeatures;
        }

        public String getCityName() {
            return cityName;
        }

        public String getCityTier() {
            return cityTier;
        }

        public BigDecimal getLivingCostMultiplier() {
            return livingCostMultiplier;
        }

        public Map<String, BigDecimal> getConsumptionDistribution() {
            return consumptionDistribution;
        }

        public String getMajorIndustries() {
            return majorIndustries;
        }

        public List<String> getConsumptionFeatures() {
            return consumptionFeatures;
        }
    }

    /**
     * 行业消费标准类
     */
    public static class IndustryStandard {
        private String industryName;                         // 行业名称
        private BigDecimal incomeMultiplier;                 // 收入倍数（相对全国平均）
        private Map<String, BigDecimal> consumptionDistribution; // 消费分布
        private List<String> consumptionFeatures;            // 消费特点
        private String typicalConsumption;                   // 典型消费

        public IndustryStandard(String industryName, BigDecimal incomeMultiplier,
                                Map<String, BigDecimal> consumptionDistribution,
                                List<String> consumptionFeatures, String typicalConsumption) {
            this.industryName = industryName;
            this.incomeMultiplier = incomeMultiplier;
            this.consumptionDistribution = consumptionDistribution;
            this.consumptionFeatures = consumptionFeatures;
            this.typicalConsumption = typicalConsumption;
        }

        public String getIndustryName() {
            return industryName;
        }

        public BigDecimal getIncomeMultiplier() {
            return incomeMultiplier;
        }

        public Map<String, BigDecimal> getConsumptionDistribution() {
            return consumptionDistribution;
        }

        public List<String> getConsumptionFeatures() {
            return consumptionFeatures;
        }

        public String getTypicalConsumption() {
            return typicalConsumption;
        }
    }

    /**
     * 消费分析结果类
     */
    public static class ConsumptionAnalysis {
        private String cityName;                               // 城市名称
        private LocalDate date;                                // 分析日期
        private String cityTier;                               // 城市等级
        private String dateType;                               // 日期类型（工作日/周末/节假日）
        private double dateConsumptionFactor;                  // 日期消费系数
        private List<String> dateConsumptionCategories;        // 日期相关消费类别
        private String dateDescription;                        // 日期消费描述
        private String season;                                 // 季节
        private double seasonConsumptionFactor;                // 季节消费系数
        private List<String> seasonConsumptionCategories;      // 季节相关消费类别
        private String seasonDescription;                      // 季节消费描述
        private double cityConsumptionFactor;                  // 城市消费系数
        private Map<String, BigDecimal> cityConsumptionDistribution; // 城市消费分布
        private List<String> cityFeatures;                     // 城市特点
        private double combinedConsumptionFactor;              // 综合消费系数
        private List<String> recommendedCategories;            // 推荐消费类别
        private String consumptionAdvice;                      // 消费建议

        public ConsumptionAnalysis() {
            this.seasonConsumptionFactor = 1.0;
        }

        // 各字段的getter和setter方法
        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getCityTier() {
            return cityTier;
        }

        public void setCityTier(String cityTier) {
            this.cityTier = cityTier;
        }

        public String getDateType() {
            return dateType;
        }

        public void setDateType(String dateType) {
            this.dateType = dateType;
        }

        public double getDateConsumptionFactor() {
            return dateConsumptionFactor;
        }

        public void setDateConsumptionFactor(double dateConsumptionFactor) {
            this.dateConsumptionFactor = dateConsumptionFactor;
        }

        public List<String> getDateConsumptionCategories() {
            return dateConsumptionCategories;
        }

        public void setDateConsumptionCategories(List<String> dateConsumptionCategories) {
            this.dateConsumptionCategories = dateConsumptionCategories;
        }

        public String getDateDescription() {
            return dateDescription;
        }

        public void setDateDescription(String dateDescription) {
            this.dateDescription = dateDescription;
        }

        public String getSeason() {
            return season;
        }

        public void setSeason(String season) {
            this.season = season;
        }

        public double getSeasonConsumptionFactor() {
            return seasonConsumptionFactor;
        }

        public void setSeasonConsumptionFactor(double seasonConsumptionFactor) {
            this.seasonConsumptionFactor = seasonConsumptionFactor;
        }

        public List<String> getSeasonConsumptionCategories() {
            return seasonConsumptionCategories;
        }

        public void setSeasonConsumptionCategories(List<String> seasonConsumptionCategories) {
            this.seasonConsumptionCategories = seasonConsumptionCategories;
        }

        public String getSeasonDescription() {
            return seasonDescription;
        }

        public void setSeasonDescription(String seasonDescription) {
            this.seasonDescription = seasonDescription;
        }

        public double getCityConsumptionFactor() {
            return cityConsumptionFactor;
        }

        public void setCityConsumptionFactor(double cityConsumptionFactor) {
            this.cityConsumptionFactor = cityConsumptionFactor;
        }

        public Map<String, BigDecimal> getCityConsumptionDistribution() {
            return cityConsumptionDistribution;
        }

        public void setCityConsumptionDistribution(Map<String, BigDecimal> cityConsumptionDistribution) {
            this.cityConsumptionDistribution = cityConsumptionDistribution;
        }

        public List<String> getCityFeatures() {
            return cityFeatures;
        }

        public void setCityFeatures(List<String> cityFeatures) {
            this.cityFeatures = cityFeatures;
        }

        public double getCombinedConsumptionFactor() {
            return combinedConsumptionFactor;
        }

        public void setCombinedConsumptionFactor(double combinedConsumptionFactor) {
            this.combinedConsumptionFactor = combinedConsumptionFactor;
        }

        public List<String> getRecommendedCategories() {
            return recommendedCategories;
        }

        public void setRecommendedCategories(List<String> recommendedCategories) {
            this.recommendedCategories = recommendedCategories;
        }

        public String getConsumptionAdvice() {
            return consumptionAdvice;
        }

        public void setConsumptionAdvice(String consumptionAdvice) {
            this.consumptionAdvice = consumptionAdvice;
        }
    }

    /**
     * 消费高峰期类
     */
    public static class ConsumptionPeakPeriod {
        private String name;                  // 高峰期名称
        private LocalDate startDate;          // 开始日期
        private LocalDate endDate;            // 结束日期
        private double consumptionFactor;     // 消费倍数
        private List<String> mainCategories;  // 主要消费类别
        private String description;           // 描述

        public ConsumptionPeakPeriod(String name, LocalDate startDate, LocalDate endDate,
                                     double consumptionFactor, List<String> mainCategories,
                                     String description) {
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
            this.consumptionFactor = consumptionFactor;
            this.mainCategories = mainCategories;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public double getConsumptionFactor() {
            return consumptionFactor;
        }

        public List<String> getMainCategories() {
            return mainCategories;
        }

        public String getDescription() {
            return description;
        }

        public long getDurationDays() {
            return ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
    }
}