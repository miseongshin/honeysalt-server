package com.honeysaltlove.domain.cart.entity;

/**
 * 활동바구니에 담을 수 있는 대상의 종류. 반응의 TargetType 과 따로 둔다 (고민 · 댓글은 담을 수 없게)
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public enum CartTargetType {
    THOUGHT_TIP,    // 생각꿀팁
    SCHEDULE,       // 꿀방울 스케줄
    ACTIVITY        // 활동
}
