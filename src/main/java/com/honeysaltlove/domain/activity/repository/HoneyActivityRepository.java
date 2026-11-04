package com.honeysaltlove.domain.activity.repository;

import com.honeysaltlove.domain.activity.entity.HoneyActivity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 활동 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
public interface HoneyActivityRepository extends JpaRepository<HoneyActivity, Long> {
}
