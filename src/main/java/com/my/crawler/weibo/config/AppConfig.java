package com.my.crawler.weibo.config;

import com.my.crawler.weibo.util.FileNames;

import java.nio.file.Path;

/**
 * 命令行聚合入口使用的配置对象。
 *
 * <p>两个历史入口类不依赖命令行参数；该配置只服务于 {@link WeiboCrawlerApp}
 * 以及测试时直接调用扫描/下载能力的场景。</p>
 */
public record AppConfig(
        String uid,
        String screenName,
        Path logRoot,
        Path outputRoot,
        int beginPage,
        int maxPages,
        boolean download,
        boolean downloadVideos,
        long delayMillis) {

    /** 解析聚合入口的 scan 命令参数。 */
    public static AppConfig fromArgs(String[] args) {
        if (args.length == 0 || "--help".equals(args[0]) || "-h".equals(args[0])) {
            throw new IllegalArgumentException(usage());
        }
        if (!"scan".equals(args[0])) {
            throw new IllegalArgumentException("Unknown command: " + args[0] + System.lineSeparator() + usage());
        }
        return parse(args, true, false, true);
    }

    /** 解析仅扫描场景的参数，主要用于兼容已有测试代码。 */
    public static AppConfig fromScanArgs(String[] args) {
        return fromScanArgs(args, args.length > 0 && "scan".equals(args[0]));
    }

    /** 解析仅下载场景的参数，主要用于兼容已有测试代码。 */
    public static AppConfig fromDownloadArgs(String[] args) {
        return parse(args, args.length > 0 && "download".equals(args[0]), true, true);
    }

    /** 用旧默认目录创建一个只扫描配置。 */
    public static AppConfig scanOnly(String uid, String screenName) {
        return new AppConfig(uid, screenName, Path.of("F:/weibo_log"), Path.of("H:/weibo"), 0, 0, false, true, 1500L);
    }

    /** 用旧默认目录创建一个只下载配置。 */
    public static AppConfig downloadOnly(String uid, String screenName) {
        return new AppConfig(uid, screenName, Path.of("F:/weibo_log"), Path.of("H:/weibo"), 0, 0, true, true, 1500L);
    }

    private static AppConfig fromScanArgs(String[] args, boolean hasCommand) {
        return parse(args, hasCommand, false, false);
    }

    private static AppConfig parse(String[] args, boolean hasCommand, boolean defaultDownload, boolean allowDownloadOptions) {
        if (args.length == 0 || "--help".equals(args[0]) || "-h".equals(args[0])) {
            throw new IllegalArgumentException(usage());
        }

        String uid = null;
        String name = null;
        Path logRoot = Path.of("D:/temp/weibo_log");
        Path outputRoot = Path.of("D:/temp/weibo_log_output");
        int beginPage = 0;
        int maxPages = 0;
        boolean download = defaultDownload;
        boolean downloadVideos = true;
        long delayMillis = 1500L;

        for (int i = hasCommand ? 1 : 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--uid" -> uid = needValue(args, ++i, arg);
                case "--name" -> name = needValue(args, ++i, arg);
                case "--log-dir" -> logRoot = Path.of(needValue(args, ++i, arg));
                case "--output-dir" -> outputRoot = Path.of(needValue(args, ++i, arg));
                case "--begin-page" -> beginPage = Integer.parseInt(needValue(args, ++i, arg));
                case "--max-pages" -> maxPages = Integer.parseInt(needValue(args, ++i, arg));
                case "--delay-ms" -> delayMillis = Long.parseLong(needValue(args, ++i, arg));
                case "--download" -> {
                    if (!allowDownloadOptions) {
                        throw new IllegalArgumentException("--download is only supported by the aggregate app");
                    }
                    download = true;
                }
                case "--no-download" -> download = false;
                case "--no-video" -> downloadVideos = false;
                default -> throw new IllegalArgumentException("Unknown option: " + arg);
            }
        }

        if (uid == null || uid.isBlank()) {
            throw new IllegalArgumentException("--uid is required");
        }
        if (name == null || name.isBlank()) {
            name = uid;
        }
        if (beginPage < 0 || maxPages < 0 || delayMillis < 0) {
            throw new IllegalArgumentException("--begin-page, --max-pages and --delay-ms must be non-negative");
        }
        return new AppConfig(uid, name, logRoot, outputRoot, beginPage, maxPages, download, downloadVideos, delayMillis);
    }

    /** 返回账号自己的日志目录，目录名会清理 Windows 非法字符。 */
    public Path accountLogDir() {
        return logRoot.resolve(FileNames.safeFileName(screenName));
    }

    /** 返回账号自己的媒体输出目录，目录名会清理 Windows 非法字符。 */
    public Path accountOutputDir() {
        return outputRoot.resolve(FileNames.safeFileName(screenName));
    }

    private static String needValue(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException(option + " needs a value");
        }
        return args[index];
    }

    /** 返回聚合入口的命令行使用说明。 */
    public static String usage() {
        return """
                Usage:
                  mvn exec:java -Dexec.args="scan --uid 2017376052 --name 郭四火- --log-dir D:/temp/weibo_log --output-dir D:/temp/weibo_log_output"

                Options:
                  --begin-page N     Start page, default 0.
                  --max-pages N      Stop after N pages, default 0 means all available pages.
                  --delay-ms N       Delay between API pages, default 1500.
                  --download         Aggregate app only: download after scanning.
                  --no-video         Download pictures only.
                """;
    }
}
