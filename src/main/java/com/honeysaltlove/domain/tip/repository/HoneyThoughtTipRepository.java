package com.honeysaltlove.domain.tip.repository;

import com.honeysaltlove.domain.tip.entity.HoneyThoughtTip;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 생각꿀팁 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
public interface HoneyThoughtTipRepository extends JpaRepository<HoneyThoughtTip, Long> {
}
