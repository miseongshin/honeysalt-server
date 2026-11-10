package com.honeysaltlove.domain.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.honeysaltlove.domain.app.entity.MemberAppKey;
import com.honeysaltlove.domain.app.repository.MemberAppKeyRepository;
import com.honeysaltlove.domain.app.service.AppKeyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 앱 키 발급 — 끝나면 롤백
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
@Transactional
class AppKeyControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberAppKeyRepository appKeyRepository;

    @Test
    @DisplayName("로그인 · CSRF 없이 앱 키와 비밀값을 받는다 · 서버에는 비밀값이 아니라 해시만 남는다")
    void issue() throws Exception {
        // when
        String body = mockMvc.perform(post("/api/app/v1/keys"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode data = objectMapper.readTree(body).path("data");
        String appKey = data.path("appKey").asText();
        String appSecret = data.path("appSecret").asText();

        // then
        assertThat(appKey).hasSize(32);
        assertThat(appSecret).hasSize(43);
        MemberAppKey saved = appKeyRepository.findByAppKey(appKey).orElseThrow();
        assertThat(saved.getAppHash()).isEqualTo(AppKeyService.hash(appSecret)).isNotEqualTo(appSecret);
        assertThat(saved.getMember()).isNull();   // 웹 계정과는 아직 연결 전
    }

    @Test
    @DisplayName("부를 때마다 다른 앱 키를 준다")
    void different() throws Exception {
        String first = mockMvc.perform(post("/api/app/v1/keys")).andReturn().getResponse().getContentAsString();
        String second = mockMvc.perform(post("/api/app/v1/keys")).andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(first).path("data").path("appKey").asText())
                .isNotEqualTo(objectMapper.readTree(second).path("data").path("appKey").asText());
    }
}
