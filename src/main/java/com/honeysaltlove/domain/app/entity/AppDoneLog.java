package com.honeysaltlove.domain.app.entity;

import com.honeysaltlove.global.common.BaseCreatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 활동완료 로그. 앱이 앱 키로 보낸다 — 웹 계정 없이도 쌓인다
 * <pre>
 * - 쌓기만 하고 고치지 않는다 · 같은 활동은 하루 한 번
 * - 앱 키 · 활동은 번호만 둔다 (FK 없음 · 연관 없음)
 * - 스케줄러가 1시간마다 활동별로 다시 세어 활동의 use_count(앱 키를 겹치지 않게) · done_count 를 맞춘다
 * </pre>
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_app_done_log_activity_app_day",
        columnNames = {"activity_id", "app_key_id", "done_on"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppDoneLog extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "done_id")
    private Long id;

    @Column(name = "app_key_id", nullable = false)
    private Long appKeyId;                     // 보낸 앱 · 연결한 회원은 member_app_key.member_id 로

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    @Column(name = "done_on", nullable = false)
    private LocalDate doneOn;                  // 활동한 날 (앱이 보낸 날짜)

    /**
     * 활동완료를 남긴다
     * @author shinm
     * @since 2026-11-06
     */
    public static AppDoneLog record(Long appKeyId, Long activityId, LocalDate doneOn) {
        AppDoneLog d = new AppDoneLog();
        d.appKeyId = appKeyId;
        d.activityId = activityId;
        d.doneOn = doneOn;
        return d;
    }
}
