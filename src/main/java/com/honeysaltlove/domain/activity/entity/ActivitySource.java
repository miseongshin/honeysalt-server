package com.honeysaltlove.domain.activity.entity;

/**
 * 활동의 등록 경로
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
public enum ActivitySource {
    DEFAULT,    // 처음부터 있는 기본 활동
    WEB,        // 지혜나눔에서 회원이 등록
    APP         // 꿀방울 앱에서 사용자가 만들어 공유
}
