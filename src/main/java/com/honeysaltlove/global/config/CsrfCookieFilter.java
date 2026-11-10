package com.honeysaltlove.global.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 요청마다 CSRF 토큰을 꺼내 XSRF-TOKEN 쿠키가 응답에 실리게 한다
 * <pre>
 * - 스프링 시큐리티 6 은 토큰을 쓸 때까지 만들지 않아서(지연 로딩), 화면이 첫 POST 전에 쿠키를 못 받는다
 * - 화면(Vue)은 이 쿠키 값을 X-XSRF-TOKEN 헤더로 다시 보낸다
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) {
            token.getToken();   // 꺼내기만 해도 쿠키가 만들어진다
        }
        chain.doFilter(request, response);
    }
}
