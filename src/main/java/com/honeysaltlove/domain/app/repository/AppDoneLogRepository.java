package com.honeysaltlove.domain.app.repository;

import com.honeysaltlove.domain.app.entity.AppDoneLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

/**
 * 활동완료 로그 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public interface AppDoneLogRepository extends JpaRepository<AppDoneLog, Long> {

    boolean existsByActivityIdAndAppKeyIdAndDoneOn(Long activityId, Long appKeyId, LocalDate doneOn);   // 오늘 이미 보냈는지 · 인덱스: uk_app_done_log_activity_app_day
}
