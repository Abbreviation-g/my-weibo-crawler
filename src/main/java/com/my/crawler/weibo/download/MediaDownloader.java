package com.my.crawler.weibo.download;

import com.fasterxml.jackson.databind.JsonNode;
import com.my.crawler.weibo.config.Constants;
import com.my.crawler.weibo.util.FileNames;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** 根据 pics_videos.log 下载图片、视频，并维护旧 done.list/failed.list。 */
public final class MediaDownloader {
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /** 下载日志中的媒体文件，已存在于 done.list 的 URL 会跳过。 */
    public DownloadStats download(TreeMap<String, JsonNode> picVideoLog, Path doneListDir, Path outputDir, boolean includeVideos)
            throws IOException, InterruptedException {
        Set<String> done = readLines(doneListDir.resolve(Constants.DONE_LIST_FILE_NAME), true);
        Set<String> failed = new LinkedHashSet<>();
        int downloaded = 0;
        int skipped = 0;

        for (var entry : picVideoLog.entrySet()) {
            JsonNode item = entry.getValue();
            DownloadStats picStats = downloadUrls(item.path(Constants.PICS), done, failed, outputDir, entry.getKey());
            downloaded += picStats.downloaded();
            skipped += picStats.skipped();
            if (includeVideos) {
                DownloadStats videoStats = downloadUrls(item.path(Constants.VIDEOS), done, failed, outputDir, entry.getKey());
                downloaded += videoStats.downloaded();
                skipped += videoStats.skipped();
            }
        }

        Files.createDirectories(doneListDir);
        Files.write(doneListDir.resolve(Constants.DONE_LIST_FILE_NAME), done, StandardCharsets.UTF_8);
        if (!failed.isEmpty()) {
            Files.write(doneListDir.resolve(Constants.FAILED_LIST_FILE_NAME), failed, StandardCharsets.UTF_8);
        }
        return new DownloadStats(downloaded, skipped, failed.size());
    }

    /** 下载单条微博对应的一组媒体 URL，并按 dateId 生成旧文件名。 */
    private DownloadStats downloadUrls(JsonNode urls, Set<String> done, Set<String> failed, Path outputDir, String dateId)
            throws IOException, InterruptedException {
        if (!urls.isArray()) {
            return new DownloadStats(0, 0, 0);
        }
        int downloaded = 0;
        int skipped = 0;
        for (JsonNode urlNode : urls) {
            String url = urlNode.asText("");
            if (url.isBlank()) {
                continue;
            }
            String doneKey = doneKey(url);
            if (done.contains(doneKey)) {
                skipped++;
                continue;
            }
            Files.createDirectories(outputDir);
            Path target = outputDir.resolve(FileNames.mediaName(url, dateId));
            if (Files.exists(target) && Files.size(target) > 0) {
                done.add(doneKey);
                skipped++;
                continue;
            }
            if (downloadOne(url, target)) {
                done.add(doneKey);
                downloaded++;
                System.out.println("downloaded " + target);
            } else {
                failed.add(url);
            }
        }
        return new DownloadStats(downloaded, skipped, failed.size());
    }

    /** 下载单个媒体 URL 到目标文件。 */
    private boolean downloadOne(String url, Path target) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:117.0) Gecko/20100101 Firefox/117.0")
                .header("Accept", "image/avif,image/webp,image/png,image/jpeg,video/mp4,video/*,*/*;q=0.8")
                .header("Referer", "https://weibo.com/")
                .GET()
                .build();
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            System.err.println("download failed HTTP " + response.statusCode() + " " + url);
            return false;
        }
        try (InputStream body = response.body()) {
            Files.copy(body, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return true;
    }

    /** 读取 done.list 或 failed.list，缺失时返回空集合。 */
    private static Set<String> readLines(Path path, boolean sorted) throws IOException {
        if (!Files.exists(path)) {
            return sorted ? new TreeSet<>() : new LinkedHashSet<>();
        }
        Set<String> lines = sorted ? new TreeSet<>() : new LinkedHashSet<>();
        lines.addAll(Files.readAllLines(path, StandardCharsets.UTF_8));
        return lines;
    }

    // Old livephoto done.list entries ignored temporary query parameters after ".mov".
    private static String doneKey(String url) {
        int mov = url.toLowerCase().indexOf(".mov");
        return mov >= 0 ? url.substring(0, mov + 4) : url;
    }

    /** 下载结果统计。 */
    public record DownloadStats(int downloaded, int skipped, int failed) {
    }
}
