package com.honeysaltlove.domain.tip.repository;

import com.honeysaltlove.domain.tip.entity.HoneyActivityTip;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 활동꿀팁 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
public interface HoneyActivityTipRepository extends JpaRepository<HoneyActivityTip, Long> {
}
