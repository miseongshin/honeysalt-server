package com.honeysaltlove.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Swagger — local · 테스트에서는 켜져 있다 (운영은 application-prod.yml 에서 끈다)
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
@SpringBootTest
@AutoConfigureMockMvc
class SwaggerTest {

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("로그인하지 않아도 API 문서(/v3/api-docs)를 볼 수 있고, 만든 API 가 들어 있다")
    void apiDocs() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/web/v1/terms']").exists())
                .andExpect(jsonPath("$.paths['/api/app/v1/keys']").exists());
    }
}
