package com.honeysaltlove.global.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 권한 나누기 · 401 · 403 응답 모양 확인 (컨트롤러가 아직 없어도 시큐리티가 먼저 막는다)
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
class SecurityConfigTest {

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("로그인하지 않고 내 정보를 보면 401 UNAUTHORIZED (구글 화면으로 보내지 않는다)")
    void unauthorized() throws Exception {
        mockMvc.perform(get("/api/web/v1/member"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    @DisplayName("가입을 덜 마친 사람이 내 정보를 보면 403 TERMS_REQUIRED")
    void termsRequired() throws Exception {
        mockMvc.perform(get("/api/web/v1/member")
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_PENDING"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("TERMS_REQUIRED"));
    }

    @Test
    @DisplayName("일반 회원이 관리자 API 를 부르면 403 FORBIDDEN")
    void forbidden() throws Exception {
        mockMvc.perform(get("/api/web/v1/admin/complaints")
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("글 조회(GET)는 로그인하지 않아도 시큐리티가 막지 않는다 (컨트롤러가 없어 404)")
    void publicGet() throws Exception {
        mockMvc.perform(get("/api/web/v1/worries"))
                .andExpect(status().isNotFound());
    }
}
