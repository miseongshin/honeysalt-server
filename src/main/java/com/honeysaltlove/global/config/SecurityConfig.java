package com.honeysaltlove.global.config;

import com.honeysaltlove.domain.member.auth.CustomOidcUserService;
import com.honeysaltlove.domain.member.auth.LoginFailureHandler;
import com.honeysaltlove.domain.member.auth.LoginSuccessHandler;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 *  SecurityConfig - 스프링 시큐리티 설정
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 * 2026-11-10        shinm              구글 로그인(oauth2Login) 연결 · 세션은 서버 메모리
 * 2026-11-10        shinm              권한 나누기 · 401 · 403 을 { status, code, message } 로
 * 2026-11-10        shinm              CORS · CSRF(쿠키 → X-XSRF-TOKEN 헤더) · 앱 API 는 CSRF 제외
 * 2026-11-10        shinm              로그아웃 POST /api/web/v1/logout
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final SecurityErrorHandler securityErrorHandler;

    @Value("${app.front-url:http://localhost:3000}")
    private String frontUrl;                   // CORS 허락 주소 · local http://localhost:3000 · prod https://honeysaltlove.com

    @Value("${app.cookie-domain:}")
    private String cookieDomain;               // CSRF 쿠키 도메인 · prod .honeysaltlove.com (화면이 읽게) · local 은 비움

    /**
     * 스프링 시큐리티 필터 설정
     *
     * @author shinm
     * @since 2026-11-04
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth                    // 위에서부터 먼저 맞는 줄이 적용된다
                .requestMatchers("/actuator/health").permitAll()   // nginx · 배포 후 서버 정상 여부 확인
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()   // 없는 주소가 401 이 아니라 404 로 가게 (오류 응답은 막지 않는다)
                .requestMatchers("/api/web/v1/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/web/v1/member/terms").hasAnyRole("PENDING", "USER", "ADMIN")   // 가입 마치기 · 바뀐 약관 다시 동의
                .requestMatchers("/api/web/v1/member/**").hasAnyRole("USER", "ADMIN")   // 내 정보는 조회(GET)도 로그인
                .requestMatchers(HttpMethod.GET, "/api/web/**").permitAll()             // 글 조회는 누구나
                .requestMatchers("/api/web/**").hasAnyRole("USER", "ADMIN")             // 쓰기는 가입을 마친 회원
                .anyRequest().authenticated())                     // 앱 API(/api/app/**)는 12. 앱 연결에서 앱 키 필터로 바꾼다
            .exceptionHandling(e -> e
                .authenticationEntryPoint(securityErrorHandler)    // 401 UNAUTHORIZED
                .accessDeniedHandler(securityErrorHandler))        // 403 FORBIDDEN · TERMS_REQUIRED
            .oauth2Login(oauth -> oauth                            // 구글 로그인 — 시작 /oauth2/authorization/google · 돌아오는 곳 /login/oauth2/code/google
                .userInfoEndpoint(info -> info.oidcUserService(customOidcUserService))   // 회원 찾기 · 가입 대기
                .successHandler(loginSuccessHandler)                                     // 화면으로 돌려보내기
                .failureHandler(loginFailureHandler))                                    // 실패 로그 · 화면 로그인으로
            .logout(logout -> logout                               // 권한 검사보다 먼저 돈다 — 가입 대기도 나갈 수 있다 (CSRF 는 받는다)
                .logoutUrl("/api/web/v1/logout")
                .deleteCookies("SESSION")
                .logoutSuccessHandler((request, response, auth) -> securityErrorHandler.writeOk(response)))   // 200 { OK }
            .formLogin(AbstractHttpConfigurer::disable)            // API 서버라 스프링 기본 로그인 화면을 사용하지 않는다
            .httpBasic(AbstractHttpConfigurer::disable)            // 아이디·비밀번호 로그인 기능을 사용하지 않는다
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository())        // 토큰을 XSRF-TOKEN 쿠키로 준다 → 화면이 X-XSRF-TOKEN 헤더로 다시 보낸다
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())   // 쿠키 값 그대로 비교 (화면이 읽은 값)
                .ignoringRequestMatchers("/api/app/**"))           // 앱 API 는 쿠키 대신 Authorization 헤더로 인증해서 CSRF 를 쓰지 않는다
            .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
        return http.build();
    }

    /**
     * CSRF 토큰 쿠키 — 화면 JS 가 읽어야 해서 HttpOnly 를 끈다 · 운영은 화면과 API 가 같이 읽게 도메인 .honeysaltlove.com
     */
    private CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        if (StringUtils.hasText(cookieDomain)) {
            repository.setCookieCustomizer(cookie -> cookie.domain(cookieDomain).secure(true).sameSite("Lax"));
        }
        return repository;
    }

    /**
     * CORS — 화면 주소 하나만 허락하고, 세션 쿠키를 실어 보낼 수 있게 한다
     */
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontUrl));
        config.setAllowedMethods(List.of("GET", "POST", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/web/**", config);
        return source;
    }
}
