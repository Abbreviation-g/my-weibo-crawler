package com.my.crawler.weibo.app;

import com.my.crawler.weibo.config.AppConfig;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 集成测试：扫描并下载微博账号 2017376052 / 郭四火-。
 *
 * <p>该测试会访问微博接口，并写入 D:/temp/weibo_log 与 D:/temp/weibo_log_output。
 * 如果微博 Cookie 失效，需要先更新 src/main/resources/weibo_json_url_headers.txt。</p>
 */
class GuoSihuoIntegrationTest {
    /**
     * 扫描 2017376052 的前 2 页，合并到旧格式 pics_videos.log，
     * 再根据 done.list 跳过已下载文件并下载缺失媒体。
     */
    @Test
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

        Path picVideoLog = config.accountLogDir().resolve("pics_videos.log");
        assertTrue(Files.exists(picVideoLog), "pics_videos.log should be written");

        String content = Files.readString(picVideoLog, StandardCharsets.UTF_8);
        assertFalse(content.contains("\"screen_name\""), "scan result should not keep screen_name");
    }
}
