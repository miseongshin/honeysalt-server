package com.honeysaltlove.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 켜기 — BaseEntity(BaseTimeEntity, BaseCreatedEntity) 사용 설정
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
