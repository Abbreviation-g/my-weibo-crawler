package com.my.crawler.weibo.app;

import com.my.crawler.weibo.config.AppConfig;

/** 可传参数的聚合入口；历史入口请使用 ScanWeiboMain 和 DownloadWeiboMain。 */
public final class WeiboCrawlerApp {
    private WeiboCrawlerApp() {
    }

    /** 解析命令行参数，执行扫描，可选扫描后下载。 */
    public static void main(String[] args) {
        try {
            run(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void run(String[] args) throws Exception {
        AppConfig config = AppConfig.fromArgs(args);
        ScanWeiboMain.scan(config);
        if (config.download()) {
            DownloadWeiboMain.download(config);
        }
    }
}
