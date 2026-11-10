package com.honeysaltlove.domain.member.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 구글 로그인을 마치면 화면(Vue)으로 돌려보낸다
 * <pre>
 * - 가입한 회원: 화면 첫 페이지
 * - 가입 대기(처음 온 사람): 가입 마치기 화면 (약관 동의 · 닉네임)
 * - 다시 동의 대기(개정된 약관에 아직 동의하지 않은 회원): 약관 동의 화면 (닉네임 칸 없음)
 * - 화면 주소는 설정 app.front-url (local http://localhost:3000 · prod https://honeysaltlove.com)
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 * 2026-11-10        shinm              다시 동의 대기는 약관 동의 화면으로
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String SIGNUP_PATH = "/signup";         // 가입 마치기 화면 — Vue 주소가 정해지면 맞춘다
    private static final String RECONSENT_PATH = "/terms/agree"; // 개정 약관 다시 동의 화면 — Vue 주소가 정해지면 맞춘다

    private final String frontUrl;

    public LoginSuccessHandler(@Value("${app.front-url:http://localhost:3000}") String frontUrl) {
        this.frontUrl = frontUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String path = "/";
        if (authentication.getPrincipal() instanceof LoginMember m && m.isPending()) {
            path = m.getMemberId() == null ? SIGNUP_PATH : RECONSENT_PATH;
        }
        response.sendRedirect(frontUrl + path);
    }
}
