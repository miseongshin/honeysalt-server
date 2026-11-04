package com.honeysaltlove.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 요청별 접근 권한. 지금은 상태 확인(health)만 열고 나머지는 막는다
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
@Configuration
public class SecurityConfig {

    /**
     * health 만 열고 나머지는 로그인 뒤로 둔다. 로그인(OAuth)을 붙일 때 여기서 연다
     * @author shinm
     * @since 2026-11-04
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()   // nginx · 배포 뒤 살아 있는지 확인하는 자리
                .anyRequest().authenticated())
            .formLogin(AbstractHttpConfigurer::disable)            // API 서버라 스프링 기본 로그인 화면이 필요 없다
            .httpBasic(AbstractHttpConfigurer::disable)            // 아이디·비번을 매 요청에 싣는 방식은 쓰지 않는다
            .csrf(Customizer.withDefaults());                      // 웹 API(세션 쿠키)를 붙일 때 다시 정한다
        return http.build();
    }
}
