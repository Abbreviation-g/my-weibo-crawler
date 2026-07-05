package com.my.crawler.weibo.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** 读取微博接口请求头资源，并应用到 Java 21 HttpClient 请求。 */
final class HeaderLoader {
    private HeaderLoader() {
    }

    /** 从 resources/weibo_json_url_headers.txt 加载请求头。 */
    static Map<String, String> load() throws IOException {
        InputStream stream = HeaderLoader.class.getResourceAsStream("/weibo_json_url_headers.txt");
        if (stream == null) {
            throw new IOException("Missing resource: /weibo_json_url_headers.txt");
        }
        Map<String, String> headers = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                int index = line.indexOf(':');
                if (index <= 0) {
                    continue;
                }
                String name = line.substring(0, index).trim();
                String value = line.substring(index + 1).trim();
                if (isAllowedRequestHeader(name)) {
                    headers.put(name, value);
                }
            }
        }
        return headers;
    }

    /** 将请求头写入 HttpRequest.Builder。 */
    static void apply(HttpRequest.Builder builder, Map<String, String> headers) {
        headers.forEach(builder::header);
    }

    // Java HttpClient manages these connection-level headers itself.
    private static boolean isAllowedRequestHeader(String name) {
        return !name.equalsIgnoreCase("Host")
                && !name.equalsIgnoreCase("Connection")
                && !name.equalsIgnoreCase("Accept-Encoding")
                && !name.equalsIgnoreCase("TE");
    }
}
