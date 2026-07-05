package com.my.crawler.weibo.app;

import com.my.crawler.weibo.config.AppConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

/**
 * 手动集成测试：扫描并下载微博账号 2017376052 / 郭四火-。
 *
 * <p>该测试会访问微博接口并写入 D:/temp/weibo_log 与 D:/temp/weibo_log_output，
 * 因此默认禁用，避免普通构建时触发网络请求和大量下载。</p>
 */
class GuoSihuoIntegrationTest {
    /**
     * 扫描 2017376052 的前 2 页，合并到旧格式 pics_videos.log，
     * 再根据 done.list 跳过已下载文件并下载缺失媒体。
     */
    @Test
    @Disabled("Manual integration test: requires valid Weibo cookies/network and writes to D:/temp.")
    void scanAndDownloadGuoSihuo() throws Exception {
        AppConfig config = new AppConfig(
                "2017376052",
                "郭四火-",
                Path.of("D:/temp/weibo_log"),
                Path.of("D:/temp/weibo_log_output"),
                0,
                2,
                true,
                true,
                1000L);

        ScanWeiboMain.scan(config);
        DownloadWeiboMain.download(config);
    }
}
