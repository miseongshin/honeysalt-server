package com.honeysaltlove.domain.tag.entity;

/**
 * 회원 태그 묶음 — 마이페이지 「나의 대표태그」 탭. 선언 순서 = 화면 순서
 * 묶음 이름(관심사 · 신체 활동 …)은 Vue 에 · 묶음을 바꿔도 태그 행은 그대로다(tag_group 값만 바꾼다)
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 * 2026-11-11        shinm              대표태그 탭 다섯 묶음으로 바꿈
 */
public enum TagGroup {
    INTEREST,           // 관심사
    MOVEMENT,           // 신체 활동 (헬스 · 러닝 · 움직임 × 상 · 중 · 하)
    RHYTHM,             // 생활리듬 (아침형 · 중간형 · 저녁형)
    PERSONALITY_TYPE,   // 성격유형 A · B · C · D
    MBTI                // MBTI — 뚜렷한 글자만 (E · I · S · N · T · F · J · P)
}
