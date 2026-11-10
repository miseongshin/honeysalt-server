package com.honeysaltlove.global.config;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CSRF · CORS 확인 (컨트롤러가 아직 없어도 시큐리티가 먼저 막는다)
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
class CsrfCorsTest {

    private static final SimpleGrantedAuthority USER = new SimpleGrantedAuthority("ROLE_USER");

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("CSRF 토큰 없이 POST 하면 403 FORBIDDEN")
    void postWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/web/v1/worries").with(oauth2Login().authorities(USER)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("가입 대기 회원이 CSRF 토큰 없이 약관 동의를 보내면 TERMS_REQUIRED 가 아니라 FORBIDDEN (가입 마치기 화면에서 빙빙 돌지 않게)")
    void pendingPostWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/web/v1/member/terms")
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_PENDING"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("가입 대기 회원도 로그아웃할 수 있다 — 200 OK (가입을 그만두고 나갈 수 있게)")
    void pendingLogout() throws Exception {
        // given
        Cookie token = mockMvc.perform(get("/api/web/v1/worries")).andReturn().getResponse().getCookie("XSRF-TOKEN");

        // when · then
        mockMvc.perform(post("/api/web/v1/logout")
                        .with(oauth2Login().authorities(new SimpleGrantedAuthority("ROLE_PENDING")))
                        .cookie(token).header("X-XSRF-TOKEN", token.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"));
    }

    @Test
    @DisplayName("화면처럼 XSRF-TOKEN 쿠키를 받아 X-XSRF-TOKEN 헤더로 보내면 시큐리티를 지난다 (컨트롤러가 없어 404)")
    void postWithCsrfCookie() throws Exception {
        // given — 조회로 쿠키를 받는다 (.with(csrf()) 는 테스트 안의 저장소를 세션으로 바꿔서 쓰지 않는다)
        Cookie token = mockMvc.perform(get("/api/web/v1/worries")).andReturn().getResponse().getCookie("XSRF-TOKEN");

        // when · then
        mockMvc.perform(post("/api/web/v1/worries").with(oauth2Login().authorities(USER))
                        .cookie(token).header("X-XSRF-TOKEN", token.getValue()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("조회하면 XSRF-TOKEN 쿠키가 온다 (화면이 첫 POST 전에 받는다)")
    void csrfCookieOnGet() throws Exception {
        mockMvc.perform(get("/api/web/v1/worries"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }

    @Test
    @DisplayName("앱 API 는 CSRF 를 쓰지 않는다 — 토큰이 없어도 403 이 아니라 인증 단계에서 401")
    void appApiIgnoresCsrf() throws Exception {
        mockMvc.perform(post("/api/app/v1/activities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("화면 주소에서 온 사전 요청(preflight)은 허락하고 쿠키를 실어 보낼 수 있다")
    void corsPreflight() throws Exception {
        mockMvc.perform(options("/api/web/v1/worries")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    @DisplayName("다른 주소에서 온 사전 요청은 막는다")
    void corsOtherOrigin() throws Exception {
        mockMvc.perform(options("/api/web/v1/worries")
                        .header(HttpHeaders.ORIGIN, "https://evil.example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }
}
