package com.my.crawler.weibo.util;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** 微博 created_at 与旧 dateId 格式之间的日期转换工具。 */
public final class WeiboDate {
    private static final DateTimeFormatter WEIBO_CREATED_AT =
            DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);

    private WeiboDate() {
    }

    /** 解析微博接口返回的 created_at 字符串。 */
    public static LocalDate parseCreatedAt(String createdAt) {
        return ZonedDateTime.parse(createdAt, WEIBO_CREATED_AT).toLocalDate();
    }

    /** 生成旧 pics_videos.log 使用的 yyyy-MM-dd-weiboId key。 */
    public static String dateId(LocalDate date, String id) {
        return "%04d-%02d-%02d-%s".formatted(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), id);
    }

    /** 从旧 dateId key 中提取日期部分。 */
    public static LocalDate dateFromDateId(String dateId) {
        String[] split = dateId.split("-");
        if (split.length < 3) {
            throw new IllegalArgumentException("Bad date id: " + dateId);
        }
        return LocalDate.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
    }
}
