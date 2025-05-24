package com.example.software.financeapp.service.localization;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 中国法定节假日服务类
 * 提供中国法定节假日的查询、判断和相关消费模式分析
 */
public class ChineseHolidayService {

    // 存储特定年份的节假日信息
    private Map<Integer, List<Holiday>> holidayCache = new HashMap<>();

    // 存储节假日的消费特点分析
    private Map<String, ConsumptionPattern> holidayConsumptionPatterns;

    public ChineseHolidayService() {
        initializeConsumptionPatterns();
    }

    /**
     * 初始化各节假日的消费特点数据
     */
    private void initializeConsumptionPatterns() {
        holidayConsumptionPatterns = new HashMap<>();

        // 春节消费特点
        holidayConsumptionPatterns.put("SPRING_FESTIVAL", new ConsumptionPattern(
                "春节",
                Arrays.asList("餐饮", "礼品", "服装", "交通"),
                3.5,
                "春节期间消费大幅增加，以餐饮、礼品和服装为主。春节前一周和春节期间是消费高峰。"
        ));

        // 国庆节消费特点
        holidayConsumptionPatterns.put("NATIONAL_DAY", new ConsumptionPattern(
                "国庆节",
                Arrays.asList("旅游", "餐饮", "住宿", "交通"),
                2.8,
                "国庆黄金周是全年旅游消费最高峰，外出旅游、酒店住宿和交通费用显著增加。"
        ));

        // 劳动节消费特点
        holidayConsumptionPatterns.put("LABOR_DAY", new ConsumptionPattern(
                "劳动节",
                Arrays.asList("旅游", "餐饮", "娱乐"),
                2.0,
                "五一小长假消费以短途旅游和本地休闲娱乐为主，商场促销活动增多。"
        ));

        // 元旦消费特点
        holidayConsumptionPatterns.put("NEW_YEAR", new ConsumptionPattern(
                "元旦",
                Arrays.asList("餐饮", "娱乐", "电子产品"),
                1.5,
                "元旦期间商场促销活动多，消费以餐饮、娱乐和年末电子产品促销为主。"
        ));

        // 中秋节消费特点
        holidayConsumptionPatterns.put("MID_AUTUMN", new ConsumptionPattern(
                "中秋节",
                Arrays.asList("礼品", "食品", "餐饮"),
                1.8,
                "中秋节消费以月饼等礼品和家庭聚餐为主，礼品消费占比较大。"
        ));

        // 端午节消费特点
        holidayConsumptionPatterns.put("DRAGON_BOAT", new ConsumptionPattern(
                "端午节",
                Arrays.asList("食品", "旅游", "餐饮"),
                1.5,
                "端午节消费以粽子等传统食品和短途旅游为主，消费增幅相对温和。"
        ));

        // 清明节消费特点
        holidayConsumptionPatterns.put("TOMB_SWEEPING", new ConsumptionPattern(
                "清明节",
                Arrays.asList("交通", "鲜花", "食品"),
                1.3,
                "清明节期间交通和祭祀相关消费增加，旅游消费适中。"
        ));
    }

    /**
     * 获取指定年份的所有中国法定节假日
     * @param year 年份
     * @return 节假日列表
     */
    public List<Holiday> getHolidaysForYear(int year) {
        // 如果缓存中已有数据，直接返回
        if (holidayCache.containsKey(year)) {
            return holidayCache.get(year);
        }

        List<Holiday> holidays = new ArrayList<>();

        // 添加固定日期的法定节假日
        // 元旦（1月1日）
        holidays.add(new Holiday(
                "元旦",
                LocalDate.of(year, Month.JANUARY, 1),
                1,
                HolidayType.NEW_YEAR
        ));

        // 劳动节（5月1日）
        holidays.add(new Holiday(
                "劳动节",
                LocalDate.of(year, Month.MAY, 1),
                5,
                HolidayType.LABOR_DAY
        ));

        // 国庆节（10月1日）
        holidays.add(new Holiday(
                "国庆节",
                LocalDate.of(year, Month.OCTOBER, 1),
                7,
                HolidayType.NATIONAL_DAY
        ));

        // 添加农历节日（使用估算日期，实际应通过农历转换）
        // 注意：真实应用中应使用农历转换库计算准确日期
        // 以下是2025年的预估日期
        if (year == 2025) {
            // 春节
            holidays.add(new Holiday(
                    "春节",
                    LocalDate.of(2025, Month.JANUARY, 29),
                    7,
                    HolidayType.SPRING_FESTIVAL
            ));

            // 清明节
            holidays.add(new Holiday(
                    "清明节",
                    LocalDate.of(2025, Month.APRIL, 4),
                    3,
                    HolidayType.TOMB_SWEEPING
            ));

            // 端午节
            holidays.add(new Holiday(
                    "端午节",
                    LocalDate.of(2025, Month.JUNE, 1),
                    3,
                    HolidayType.DRAGON_BOAT
            ));

            // 中秋节
            holidays.add(new Holiday(
                    "中秋节",
                    LocalDate.of(2025, Month.SEPTEMBER, 12),
                    3,
                    HolidayType.MID_AUTUMN
            ));
        } else if (year == 2024) {
            // 2024年的预估日期
            // 春节
            holidays.add(new Holiday(
                    "春节",
                    LocalDate.of(2024, Month.FEBRUARY, 10),
                    7,
                    HolidayType.SPRING_FESTIVAL
            ));

            // 清明节
            holidays.add(new Holiday(
                    "清明节",
                    LocalDate.of(2024, Month.APRIL, 4),
                    3,
                    HolidayType.TOMB_SWEEPING
            ));

            // 端午节
            holidays.add(new Holiday(
                    "端午节",
                    LocalDate.of(2024, Month.JUNE, 10),
                    3,
                    HolidayType.DRAGON_BOAT
            ));

            // 中秋节
            holidays.add(new Holiday(
                    "中秋节",
                    LocalDate.of(2024, Month.SEPTEMBER, 17),
                    3,
                    HolidayType.MID_AUTUMN
            ));
        }

        // 缓存结果
        holidayCache.put(year, holidays);
        return holidays;
    }

    /**
     * 判断指定日期是否为法定节假日
     * @param date 日期
     * @return 如果是节假日返回对应的Holiday对象，否则返回null
     */
    public Holiday getHoliday(LocalDate date) {
        List<Holiday> holidays = getHolidaysForYear(date.getYear());

        for (Holiday holiday : holidays) {
            LocalDate holidayEnd = holiday.getStartDate().plusDays(holiday.getDurationDays() - 1);
            if (!date.isBefore(holiday.getStartDate()) && !date.isAfter(holidayEnd)) {
                return holiday;
            }
        }

        return null;
    }

    /**
     * 判断指定日期是否为周末
     * @param date 日期
     * @return 是否为周末
     */
    public boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * 获取指定日期的消费特点
     * @param date 日期
     * @return 消费特点对象，如果不是特殊日期则返回null
     */
    public ConsumptionPattern getConsumptionPatternForDate(LocalDate date) {
        // 检查是否为法定节假日
        Holiday holiday = getHoliday(date);
        if (holiday != null) {
            return holidayConsumptionPatterns.get(holiday.getType().name());
        }

        // 检查是否为周末
        if (isWeekend(date)) {
            return new ConsumptionPattern(
                    "周末",
                    Arrays.asList("餐饮", "娱乐", "购物"),
                    1.4,
                    "周末消费以餐饮、购物和休闲娱乐为主，相比工作日上升约40%。"
            );
        }

        // 普通工作日
        return new ConsumptionPattern(
                "工作日",
                Arrays.asList("餐饮", "交通", "日常用品"),
                1.0,
                "工作日消费相对稳定，以日常餐饮、交通和必需品为主。"
        );
    }

    /**
     * 获取指定日期所在的季节
     * @param date 日期
     * @return 季节名称
     */
    public String getSeason(LocalDate date) {
        int month = date.getMonthValue();
        if (month >= 3 && month <= 5) {
            return "春季";
        } else if (month >= 6 && month <= 8) {
            return "夏季";
        } else if (month >= 9 && month <= 11) {
            return "秋季";
        } else {
            return "冬季";
        }
    }

    /**
     * 获取指定季节的消费特点
     * @param season 季节名称
     * @return 消费特点对象
     */
    public ConsumptionPattern getSeasonalConsumptionPattern(String season) {
        switch (season) {
            case "春季":
                return new ConsumptionPattern(
                        "春季",
                        Arrays.asList("服装", "旅游", "户外活动"),
                        1.1,
                        "春季消费以换季服装和短途户外游为主，气温回暖带动户外消费。"
                );
            case "夏季":
                return new ConsumptionPattern(
                        "夏季",
                        Arrays.asList("冷饮", "娱乐", "避暑产品"),
                        1.2,
                        "夏季消费特点是避暑、降温相关产品和服务增加，冷饮和室内娱乐消费上升。"
                );
            case "秋季":
                return new ConsumptionPattern(
                        "秋季",
                        Arrays.asList("服装", "教育", "数码产品"),
                        1.15,
                        "秋季是开学季和换季消费高峰，教育支出和服装消费增加，电子产品消费也会上升。"
                );
            case "冬季":
                return new ConsumptionPattern(
                        "冬季",
                        Arrays.asList("暖气设备", "服装", "餐饮"),
                        1.25,
                        "冬季消费以取暖相关产品和冬装为主，年末促销带动消费整体上升。"
                );
            default:
                return null;
        }
    }

    /**
     * 获取下一个法定节假日
     * @param fromDate 起始日期
     * @return 下一个法定节假日
     */
    public Holiday getNextHoliday(LocalDate fromDate) {
        // 获取当年的节假日
        List<Holiday> currentYearHolidays = getHolidaysForYear(fromDate.getYear());

        // 查找当年剩余的节假日
        for (Holiday holiday : currentYearHolidays) {
            if (holiday.getStartDate().isAfter(fromDate) || holiday.getStartDate().isEqual(fromDate)) {
                return holiday;
            }
        }

        // 如果当年没有剩余节假日，则查找下一年的第一个节假日
        List<Holiday> nextYearHolidays = getHolidaysForYear(fromDate.getYear() + 1);
        return nextYearHolidays.stream()
                .min(Comparator.comparing(Holiday::getStartDate))
                .orElse(null);
    }

    /**
     * 计算距离下一个法定节假日的天数
     * @param fromDate 起始日期
     * @return 距离天数
     */
    public long getDaysUntilNextHoliday(LocalDate fromDate) {
        Holiday nextHoliday = getNextHoliday(fromDate);
        if (nextHoliday != null) {
            return ChronoUnit.DAYS.between(fromDate, nextHoliday.getStartDate());
        }
        return -1; // 未找到下一个节假日
    }

    /**
     * 法定节假日类型枚举
     */
    public enum HolidayType {
        NEW_YEAR,           // 元旦
        SPRING_FESTIVAL,    // 春节
        TOMB_SWEEPING,      // 清明节
        LABOR_DAY,          // 劳动节
        DRAGON_BOAT,        // 端午节
        MID_AUTUMN,         // 中秋节
        NATIONAL_DAY        // 国庆节
    }

    /**
     * 法定节假日类
     */
    public static class Holiday {
        private String name;            // 节假日名称
        private LocalDate startDate;    // 开始日期
        private int durationDays;       // 持续天数
        private HolidayType type;       // 节假日类型

        public Holiday(String name, LocalDate startDate, int durationDays, HolidayType type) {
            this.name = name;
            this.startDate = startDate;
            this.durationDays = durationDays;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public int getDurationDays() {
            return durationDays;
        }

        public HolidayType getType() {
            return type;
        }

        public LocalDate getEndDate() {
            return startDate.plusDays(durationDays - 1);
        }

        @Override
        public String toString() {
            return name + " (" + startDate + " 至 " + getEndDate() + ")";
        }
    }

    /**
     * 消费特点类
     */
    public static class ConsumptionPattern {
        private String periodName;                  // 时段名称
        private List<String> topCategories;         // 主要消费类别
        private double consumptionMultiplier;       // 消费倍数（相对于普通日）
        private String description;                 // 消费特点描述

        public ConsumptionPattern(String periodName, List<String> topCategories,
                                  double consumptionMultiplier, String description) {
            this.periodName = periodName;
            this.topCategories = topCategories;
            this.consumptionMultiplier = consumptionMultiplier;
            this.description = description;
        }

        public String getPeriodName() {
            return periodName;
        }

        public List<String> getTopCategories() {
            return topCategories;
        }

        public double getConsumptionMultiplier() {
            return consumptionMultiplier;
        }

        public String getDescription() {
            return description;
        }
    }
}