package com.example.software.financeapp.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 日期处理工具类
 */
public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * 格式化日期为字符串(yyyy-MM-dd)
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }

    /**
     * 格式化日期时间为字符串(yyyy-MM-dd HH:mm:ss)
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化年月为字符串(yyyy-MM)
     */
    public static String formatYearMonth(YearMonth yearMonth) {
        if (yearMonth == null) return "";
        return yearMonth.format(MONTH_FORMATTER);
    }

    /**
     * 字符串解析为日期
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 字符串解析为日期时间
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) return null;
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * 字符串解析为年月
     */
    public static YearMonth parseYearMonth(String yearMonthStr) {
        if (yearMonthStr == null || yearMonthStr.trim().isEmpty()) return null;
        return YearMonth.parse(yearMonthStr, MONTH_FORMATTER);
    }

    /**
     * 计算两个日期之间的月份数
     */
    public static long monthsBetween(LocalDate startDate, LocalDate endDate) {
        return ChronoUnit.MONTHS.between(
                YearMonth.from(startDate),
                YearMonth.from(endDate)
        );
    }

    /**
     * 获取月份的第一天
     */
    public static LocalDate firstDayOfMonth(YearMonth yearMonth) {
        return yearMonth.atDay(1);
    }

    /**
     * 获取月份的最后一天
     */
    public static LocalDate lastDayOfMonth(YearMonth yearMonth) {
        return yearMonth.atEndOfMonth();
    }

    /**
     * 获取当前年月的第一天
     */
    public static LocalDate firstDayOfCurrentMonth() {
        return YearMonth.now().atDay(1);
    }

    /**
     * 获取当前年月的最后一天
     */
    public static LocalDate lastDayOfCurrentMonth() {
        return YearMonth.now().atEndOfMonth();
    }

    /**
     * 获取几个月前的日期
     */
    public static LocalDate monthsAgo(int months) {
        return LocalDate.now().minusMonths(months);
    }

    /**
     * 获取几个月前的第一天
     */
    public static LocalDate firstDayOfMonthsAgo(int months) {
        return YearMonth.now().minusMonths(months).atDay(1);
    }
}