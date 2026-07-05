package com.my.crawler.weibo.history;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.crawler.weibo.config.Constants;
import com.my.crawler.weibo.util.WeiboDate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.TreeMap;

/** 读写账号目录里的历史日志，重点兼容旧 pics_videos.log 格式。 */
public final class HistoryStore {
    private static final TypeReference<TreeMap<String, JsonNode>> PIC_VIDEO_LOG_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper mapper;
    private final Path accountDir;

    public HistoryStore(ObjectMapper mapper, Path accountDir) {
        this.mapper = mapper;
        this.accountDir = accountDir;
    }

    /** 读取旧格式 pics_videos.log；文件不存在或为空时返回空 TreeMap。 */
    public TreeMap<String, JsonNode> readPicVideoLog() throws IOException {
        Path log = picVideoLog();
        if (!Files.exists(log) || Files.size(log) == 0) {
            return new TreeMap<>();
        }
        return mapper.readValue(Files.readString(log, StandardCharsets.UTF_8), PIC_VIDEO_LOG_TYPE);
    }

    /** 从旧 dateId key 中找出最新日期，用于增量扫描时提前停止。 */
    public Optional<LocalDate> latestDate(TreeMap<String, JsonNode> existing) {
        return existing.keySet().stream()
                .map(HistoryStore::tryParseDateId)
                .flatMap(Optional::stream)
                .max(LocalDate::compareTo);
    }

    /** 将合并后的媒体日志写回 pics_videos.log。 */
    public void writePicVideoLog(TreeMap<String, JsonNode> merged) throws IOException {
        Files.createDirectories(accountDir);
        mapper.writerWithDefaultPrettyPrinter().writeValue(picVideoLog().toFile(), merged);
    }

    /** 返回账号目录下的 pics_videos.log 路径。 */
    public Path picVideoLog() {
        return accountDir.resolve(Constants.PICS_VIDEOS_FILE_NAME);
    }

    private static Optional<LocalDate> tryParseDateId(String dateId) {
        try {
            return Optional.of(WeiboDate.dateFromDateId(dateId));
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }
}
