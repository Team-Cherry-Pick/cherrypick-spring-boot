package com.cherrypick.backend.global.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonTemplateUtil {

    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    private JsonTemplateUtil() {}

    public static String toJsonTemplate(Class<?> clazz) {
        try {
            Object emptyInstance = mapper.readValue("{}", clazz); // 비어 있는 객체 생성
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(emptyInstance);
        } catch (Exception e) {
            throw new RuntimeException("JSON 템플릿 생성 실패", e);
        }
    }

}
