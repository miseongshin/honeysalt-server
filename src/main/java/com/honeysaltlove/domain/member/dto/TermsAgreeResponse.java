package com.honeysaltlove.domain.member.dto;

/**
 * 약관 동의 · 가입 마치기 응답 — 자동으로 만든 닉네임을 화면에 보여 줄 수 있게 돌려준다
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
public record TermsAgreeResponse(Long memberId, String nickname, boolean joined) {
}
