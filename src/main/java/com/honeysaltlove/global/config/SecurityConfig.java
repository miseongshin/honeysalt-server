package com.honeysaltlove.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 *  SecurityConfig - 스프링 시큐리티 설정
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
     * 스프링 시큐리티 필터 설정
     *
     * @author shinm
     * @since 2026-11-04
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()   // nginx · 배포 후 서버 정상 여부 확인
                .anyRequest().authenticated())
            .formLogin(AbstractHttpConfigurer::disable)            // API 서버라 스프링 기본 로그인 화면을 사용하지 않는다
            .httpBasic(AbstractHttpConfigurer::disable)            // 아이디·비밀번호 로그인 기능을 사용하지 않는다
            .csrf(Customizer.withDefaults());                      // CSRF 토큰 보안을 사용한다
        return http.build();
    }
}
