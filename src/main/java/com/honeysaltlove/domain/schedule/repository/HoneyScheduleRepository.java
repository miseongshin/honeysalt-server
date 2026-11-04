package com.honeysaltlove.domain.schedule.repository;

import com.honeysaltlove.domain.schedule.entity.HoneySchedule;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 스케줄 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
public interface HoneyScheduleRepository extends JpaRepository<HoneySchedule, Long> {
}
