package com.honeysaltlove.domain.member.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 구글 로그인이 실패하면 로그를 남기고 화면(Vue) 로그인 페이지로 돌려보낸다
 * <pre>
 * - 로그에는 실패 이유(오류 코드)만 남긴다 · 토큰 · 소셜 회원번호는 찍지 않는다
 * - 사용자가 구글 화면에서 취소한 경우도 여기로 온다 (access_denied)
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
@Slf4j
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private static final String LOGIN_ERROR_PATH = "/login?error";   // 화면 로그인 페이지 — Vue 주소가 정해지면 맞춘다

    private final String frontUrl;

    public LoginFailureHandler(@Value("${app.front-url:http://localhost:3000}") String frontUrl) {
        this.frontUrl = frontUrl;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException e) throws IOException {
        String reason = e instanceof OAuth2AuthenticationException oauth
                ? oauth.getError().getErrorCode()
                : e.getClass().getSimpleName();
        log.warn("login failed: reason={}", reason);
        response.sendRedirect(frontUrl + LOGIN_ERROR_PATH);
    }
}
