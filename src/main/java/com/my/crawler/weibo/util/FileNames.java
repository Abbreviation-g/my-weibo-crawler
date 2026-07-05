package com.my.crawler.weibo.util;

/** 文件名工具，负责兼容旧下载文件名格式并规避 Windows 非法字符。 */
public final class FileNames {
    private static final String INVALID_WINDOWS_CHARS = "[\\\\/:*?\"<>|]";

    private FileNames() {
    }

    /** 将账号名或搜索词转换为可用的 Windows 文件夹名。 */
    public static String safeFileName(String value) {
        String sanitized = value == null ? "" : value.replaceAll(INVALID_WINDOWS_CHARS, "").trim();
        return sanitized.isEmpty() ? "unknown" : sanitized;
    }

    /** 按旧格式生成媒体文件名：dateId-原始文件名。 */
    public static String mediaName(String url, String dateId) {
        return dateId + "-" + rawMediaName(url);
    }

    /** 从图片、视频或 livephoto URL 中取出原始文件名。 */
    public static String rawMediaName(String url) {
        String lower = url.toLowerCase();
        if (lower.contains("livephoto=")) {
            int index = Math.max(lower.lastIndexOf("%2f"), lower.lastIndexOf("%2F"));
            if (index >= 0) {
                return stripQuery(url.substring(index + 3));
            }
        }
        int slash = url.lastIndexOf('/');
        String tail = slash >= 0 ? url.substring(slash + 1) : url;
        return stripQuery(tail);
    }

    private static String stripQuery(String fileName) {
        int query = fileName.indexOf('?');
        return query >= 0 ? fileName.substring(0, query) : fileName;
    }
}
