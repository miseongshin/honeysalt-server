package com.honeysaltlove.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.honeysaltlove.global.common.ApiResponse;
import com.honeysaltlove.global.common.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 시큐리티가 막은 요청을 { status, code, message } 로 응답한다 (스프링 시큐리티 기본 응답은 모양이 달라서)
 * <pre>
 * - 로그인하지 않았다: 401 UNAUTHORIZED (구글 로그인 화면으로 보내지 않는다 — API 서버라서)
 * - 가입 대기(ROLE_PENDING): 403 TERMS_REQUIRED — 화면이 가입 마치기로 보낸다
 * - CSRF 토큰이 없거나 틀림: 403 FORBIDDEN (가입 대기여도 — TERMS_REQUIRED 를 주면 가입 마치기 화면에서 빙빙 돈다)
 * - 그 밖의 권한 없음: 403 FORBIDDEN
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final String ROLE_PENDING = "ROLE_PENDING";

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException {
        write(response, ResultCode.UNAUTHORIZED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException e) throws IOException {
        // CSRF 실패는 권한과 상관없이 FORBIDDEN — 가입 대기에게 TERMS_REQUIRED 를 주면 화면이 가입 마치기로 다시 보내서 빙빙 돈다
        boolean termsRequired = !(e instanceof CsrfException) && isPending();
        write(response, termsRequired ? ResultCode.TERMS_REQUIRED : ResultCode.FORBIDDEN);
    }

    private boolean isPending() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> ROLE_PENDING.equals(a.getAuthority()));
    }

    /**
     * 시큐리티가 직접 처리하는 성공 응답(로그아웃)도 같은 모양으로 — 200 { status, code: OK, message }
     * @author shinm
     * @since 2026-11-10
     */
    public void writeOk(HttpServletResponse response) throws IOException {
        write(response, ResultCode.OK);
    }

    private void write(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setStatus(resultCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.error(resultCode));
    }
}
