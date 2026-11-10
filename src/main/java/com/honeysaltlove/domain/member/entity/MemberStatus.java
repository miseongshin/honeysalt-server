package com.honeysaltlove.domain.member.entity;

/**
 * 회원 상태
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
public enum MemberStatus {
    ACTIVE,     // 활동
    WITHDRAWN   // 탈퇴 (개인정보는 비우고, 글이 가리키는 회원 행만 남긴다)
}
