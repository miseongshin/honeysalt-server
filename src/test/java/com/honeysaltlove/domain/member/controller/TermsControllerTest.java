package com.honeysaltlove.domain.member.controller;

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
 * 약관 조회 — Flyway 기본 데이터(V6 꿀방울 · V7 honeysaltlove)로 확인한다
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
class TermsControllerTest {

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("로그인하지 않아도 honeysaltlove 약관 두 개(이용약관 · 개인정보처리방침)를 볼 수 있다 — body 는 JSON 그대로")
    void honeysaltlove() throws Exception {
        mockMvc.perform(get("/api/web/v1/terms").param("service", "HONEYSALTLOVE").param("lang", "ko"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].type").value("SERVICE"))
                .andExpect(jsonPath("$.data[0].required").value(true))
                .andExpect(jsonPath("$.data[0].body.title").value("honeysaltlove 이용약관"))
                .andExpect(jsonPath("$.data[1].type").value("PRIVACY"));
    }

    @Test
    @DisplayName("요청한 언어의 약관이 없으면 한국어로 준다")
    void fallbackToKorean() throws Exception {
        mockMvc.perform(get("/api/web/v1/terms").param("service", "HONEYSALTLOVE").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].body.title").value("honeysaltlove 이용약관"));
    }

    @Test
    @DisplayName("꿀방울 약관은 언어마다 있다 (영어)")
    void honeyDropsEnglish() throws Exception {
        mockMvc.perform(get("/api/web/v1/terms").param("service", "HONEY_DROPS").param("lang", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].body.title").value("Honey Drops Terms of Service"));
    }

    @Test
    @DisplayName("없는 서비스를 보내면 400 VALIDATION")
    void wrongService() throws Exception {
        mockMvc.perform(get("/api/web/v1/terms").param("service", "NOPE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"));
    }
}
