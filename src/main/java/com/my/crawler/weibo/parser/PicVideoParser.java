package com.my.crawler.weibo.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.my.crawler.weibo.config.Constants;
import com.my.crawler.weibo.util.WeiboDate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

/** 将微博接口返回的微博数组解析成旧 pics_videos.log 的媒体结构。 */
public final class PicVideoParser {
    private final ObjectMapper mapper;

    public PicVideoParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /** 解析微博列表，输出按 dateId 排序的图片/视频记录。 */
    public TreeMap<String, JsonNode> parse(List<JsonNode> statuses) {
        TreeMap<String, JsonNode> result = new TreeMap<>();
        for (JsonNode status : statuses) {
            String id = status.path("id").asText("");
            String createdAt = status.path("created_at").asText("");
            if (id.isBlank() || createdAt.isBlank()) {
                continue;
            }

            ArrayNode pics = mapper.createArrayNode();
            ArrayNode videos = mapper.createArrayNode();
            collectVideos(status, videos);
            collectPics(status, pics);
            collectMixedMedia(status, pics, videos);
            if (pics.isEmpty() && videos.isEmpty()) {
                continue;
            }

            ObjectNode item = mapper.createObjectNode();
            if (!pics.isEmpty()) {
                item.set(Constants.PICS, pics);
            }
            if (!videos.isEmpty()) {
                item.set(Constants.VIDEOS, videos);
            }
            item.put(Constants.TEXT_RAW, bestText(status));
            addPageUrl(status, id, item);
            addTags(status, item);

            LocalDate date = WeiboDate.parseCreatedAt(createdAt);
            result.put(WeiboDate.dateId(date, id), item);
        }
        return result;
    }

    /** 解析普通视频结构。 */
    private static void collectVideos(JsonNode status, ArrayNode videos) {
        JsonNode playbackList = status.path("page_info").path("media_info").path("playback_list");
        if (playbackList.isArray() && !playbackList.isEmpty()) {
            addIfTextual(videos, playbackList.get(0).path("play_info").path("url"));
        }
        addIfTextual(videos, status.path("page_info").path("media_info").path("stream_url"));
    }

    /** 解析普通图片和 livephoto 图片结构。 */
    private static void collectPics(JsonNode status, ArrayNode pics) {
        JsonNode picIds = status.path("pic_ids");
        JsonNode picInfos = status.path("pic_infos");
        if (!picIds.isArray() || !picInfos.isObject()) {
            return;
        }
        for (JsonNode picIdNode : picIds) {
            JsonNode picInfo = picInfos.path(picIdNode.asText());
            if ("livephoto".equals(picInfo.path("type").asText())) {
                addIfTextual(pics, picInfo.path("video"));
            }
            addIfTextual(pics, picInfo.path("largest").path("url"));
        }
    }

    /** 解析图文/视频混排结构。 */
    private static void collectMixedMedia(JsonNode status, ArrayNode pics, ArrayNode videos) {
        JsonNode items = status.path("mix_media_info").path("items");
        if (!items.isArray()) {
            return;
        }
        for (JsonNode item : items) {
            JsonNode data = item.path("data");
            String type = item.path("type").asText("");
            if ("pic".equalsIgnoreCase(type)) {
                addIfTextual(pics, data.path("largest").path("url"));
            } else if ("video".equalsIgnoreCase(type)) {
                JsonNode mediaInfo = data.path("media_info");
                addIfTextual(videos, mediaInfo.path("stream_url"));
                JsonNode playbackList = mediaInfo.path("playback_list");
                if (playbackList.isArray() && !playbackList.isEmpty()) {
                    addIfTextual(videos, playbackList.get(0).path("play_info").path("url"));
                }
            }
        }
    }

    /** 添加 page_url，便于从日志追溯原微博；扫描结果不再写入 screen_name。 */
    private static void addPageUrl(JsonNode status, String id, ObjectNode item) {
        JsonNode user = status.path("user");
        String userId = user.path("idstr").asText(user.path("id").asText(""));
        if (!userId.isBlank()) {
            item.put(Constants.PAGE_URL, Constants.PAGE_URL_FORMAT.formatted(userId, id));
        }
    }

    /** 将微博 tags 数组合并成旧日志中的字符串字段。 */
    private static void addTags(JsonNode status, ObjectNode item) {
        JsonNode tags = status.path(Constants.TAGS);
        if (!tags.isArray()) {
            return;
        }
        Optional<String> joined = tags.findValues("tag").stream()
                .map(JsonNode::asText)
                .filter(text -> !text.isBlank())
                .reduce((a, b) -> a + " " + b);
        joined.ifPresent(value -> item.put(Constants.TAGS, value));
    }

    private static String bestText(JsonNode status) {
        String textRaw = status.path(Constants.TEXT_RAW).asText("");
        return textRaw.isBlank() ? status.path(Constants.TEXT).asText("") : textRaw;
    }

    private static void addIfTextual(ArrayNode array, JsonNode node) {
        if (node != null && node.isTextual() && !node.asText().isBlank()) {
            array.add(node.asText());
        }
    }
}
