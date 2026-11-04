package com.honeysaltlove.domain.worry.repository;

import com.honeysaltlove.domain.worry.entity.HoneyWorry;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 고민 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
public interface HoneyWorryRepository extends JpaRepository<HoneyWorry, Long> {
}
