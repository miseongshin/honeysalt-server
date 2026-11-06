package com.honeysaltlove.domain.tag.entity;

/**
 * 회원 태그 묶음 — 맞춤설정 화면에 보이는 묶음. 선언 순서 = 화면 순서
 * 묶음 이름(원하는 변화 · 직업 …)은 Vue 번역 파일에 · 묶음을 바꿔도 태그 행은 그대로다(tag_group 값만 바꾼다)
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public enum TagGroup {
    GOAL,               // 원하는 변화
    LIFE_EVENT,         // 겪고 있는 일 (직장 갈등 · 가족 문제 …)
    OCCUPATION,         // 직업 · 일상 환경
    PERSONALITY_TYPE,   // 성격 유형 A · B · C · D
    SOCIAL_STYLE,       // 사람과 함께하는 정도
    PLANNING_STYLE,     // 계획을 세우는 방식
    ACTIVITY_STYLE,     // 선호하는 활동 방식
    WITH,               // 함께하는 대상
    LEVEL,              // 지금 가능한 활동 강도
    TIME,               // 가능한 시간
    PAIN,               // 통증 · 신체 제약
    INTEREST,           // 관심 주제
    RHYTHM              // 생활 리듬
}
