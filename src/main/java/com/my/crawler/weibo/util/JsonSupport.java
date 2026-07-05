package com.my.crawler.weibo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/** 集中创建 JSON mapper，确保所有日志输出格式一致。 */
public final class JsonSupport {
    private JsonSupport() {
    }

    /** 返回开启缩进输出的 ObjectMapper。 */
    public static ObjectMapper mapper() {
        return new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }
}
