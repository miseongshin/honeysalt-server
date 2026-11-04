package com.honeysaltlove.domain.schedule.entity;

import com.honeysaltlove.domain.activity.entity.HoneyActivity;
import com.honeysaltlove.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 스케줄 구성. 스케줄에 포함된 활동을 한 줄씩 저장하고, 활동을 실행할 요일을 지정한다
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HoneyScheduleActivity extends BaseTimeEntity {

    @EmbeddedId
    private HoneyScheduleActivityId id = new HoneyScheduleActivityId();

    @MapsId("scheduleId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private HoneySchedule schedule;

    @MapsId("activityId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private HoneyActivity activity;

    @Column(length = 20)
    private String days;                       // 월요일 0 · 화요일 1 … 일요일 6 · 예 "0,3" = 월·목 · 비워 두면 매일 실행한다

    static HoneyScheduleActivity of(HoneySchedule schedule, HoneyActivity activity, String days) {
        HoneyScheduleActivity a = new HoneyScheduleActivity();
        a.schedule = schedule;
        a.activity = activity;
        a.days = days;
        return a;
    }
}
