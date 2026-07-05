package com.my.crawler.weibo.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.crawler.weibo.util.WeiboDate;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 微博接口客户端，负责分页拉取用户微博列表。 */
public final class WeiboClient {
    private final ObjectMapper mapper;
    private final HttpClient client;
    private final Map<String, String> headers;

    public WeiboClient(ObjectMapper mapper) throws IOException {
        this.mapper = mapper;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        this.headers = HeaderLoader.load();
    }

    /** 分页获取微博；当扫到已存在历史日期之前时自动停止。 */
    public List<JsonNode> fetchStatuses(String uid, int beginPage, int maxPages, LocalDate latestExistingDate, long delayMillis)
            throws IOException, InterruptedException {
        List<JsonNode> result = new ArrayList<>();
        int page = beginPage;
        int fetchedPages = 0;
        while (maxPages == 0 || fetchedPages < maxPages) {
            JsonNode payload = fetchPage(uid, page);
            fetchedPages++;
            page++;

            JsonNode data = payload.path("data");
            JsonNode list = data.path("list");
            if (!list.isArray()) {
                System.err.println("No data.list in response, stop scanning.");
                break;
            }
            list.forEach(result::add);

            if (latestExistingDate != null && shouldStopAtExistingHistory(list, latestExistingDate)) {
                break;
            }
            String sinceId = data.path("since_id").asText("");
            if (sinceId.isBlank() || "0".equals(sinceId)) {
                break;
            }
            if (delayMillis > 0) {
                Thread.sleep(delayMillis);
            }
        }
        return result;
    }

    /** 拉取指定 UID 的单页微博 JSON。 */
    private JsonNode fetchPage(String uid, int page) throws IOException, InterruptedException {
        String encodedUid = URLEncoder.encode(uid, StandardCharsets.UTF_8);
        URI uri = URI.create("https://weibo.com/ajax/statuses/mymblog?uid=%s&page=%d&feature=0".formatted(encodedUid, page));
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(30))
                .GET();
        HeaderLoader.apply(builder, headers);

        System.out.println("GET " + uri);
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IOException("Weibo API returned HTTP " + response.statusCode() + ": " + response.body());
        }
        return mapper.readTree(response.body());
    }

    /** 判断当前页是否已经扫到旧历史之前。 */
    private static boolean shouldStopAtExistingHistory(JsonNode list, LocalDate latestExistingDate) {
        if (!list.isArray() || list.isEmpty()) {
            return false;
        }
        JsonNode last = list.get(list.size() - 1);
        if ("1".equals(last.path("isTop").asText())) {
            return false;
        }
        String createdAt = last.path("created_at").asText("");
        if (createdAt.isBlank()) {
            return false;
        }
        return WeiboDate.parseCreatedAt(createdAt).isBefore(latestExistingDate);
    }
}
