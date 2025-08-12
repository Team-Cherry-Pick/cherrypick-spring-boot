package com.cherrypick.backend.global.util;

import com.cherrypick.backend.domain.auth.presentation.dto.OAuth2UserDTO;
import com.cherrypick.backend.domain.user.enums.Gender;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonTemplateUtilTest {

    @Test
    void toJsonTemplate_shouldGenerateExpectedJsonStructure() {

        record SampleDto(String name, int age, boolean active, Gender gender) {}
        // when
        String json = JsonTemplateUtil.toJsonTemplate(SampleDto.class);

        // then
        assertTrue(json.contains("name"));
        assertTrue(json.contains("age"));
        assertTrue(json.contains("active"));
        assertTrue(json.contains("{"));
        assertTrue(json.contains("}"));
    }

    @Test
    void toJsonTemplate_shouldThrowRuntimeException_onInvalidClass() {
        class NoDefaultConstructor {
            private final String value;

            public NoDefaultConstructor(String value) {
                this.value = value;
            }
        }

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> JsonTemplateUtil.toJsonTemplate(NoDefaultConstructor.class)
        );

        assertTrue(thrown.getMessage().contains("JSON 템플릿 생성 실패"));
    }
}