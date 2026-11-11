package com.honeysaltlove.domain.reaction.entity;

/**
 * 좋아요 · 신고를 받는 대상의 종류. 대상 번호(target_id)와 함께 저장한다 (FK 없음)
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public enum TargetType {
    WORRY,          // 고민
    THOUGHT_TIP,    // 생각꿀팁
    SCHEDULE,       // 꿀방울스케줄
    ACTIVITY,       // 활동
    COMMENT         // 댓글
}
