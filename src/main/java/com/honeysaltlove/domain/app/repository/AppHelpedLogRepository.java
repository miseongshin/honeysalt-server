package com.honeysaltlove.domain.app.repository;

import com.honeysaltlove.domain.app.entity.AppHelpedLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 도움돼요 로그 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public interface AppHelpedLogRepository extends JpaRepository<AppHelpedLog, Long> {

    // 그 앱 · 그 활동의 마지막 행 — 같은 상태가 두 번 들어오지 않게 · 인덱스: ix_app_helped_log_app_activity
    // 같은 시각에 두 행이 생겨도 순서가 정해지게 번호로 정렬한다
    Optional<AppHelpedLog> findFirstByAppKeyIdAndActivityIdOrderByIdDesc(Long appKeyId, Long activityId);
}
