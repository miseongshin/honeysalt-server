package com.honeysaltlove.domain.member.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/**
 * 약관 동의 · 가입 마치기 요청 — POST /api/web/v1/member/terms
 * <pre>
 * - agreedTermsIds: 동의한 약관 번호 (GET /terms 의 termsId) · 필수 약관이 모두 있어야 한다
 * - nickname: 처음 가입할 때만 쓴다 · 비우면 자동으로 만든다 · 2~12자, 띄어쓰기 없음
 * - lang: 화면 언어 (ko · en …) · 처음 가입할 때만 쓴다 · 비우면 ko
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
public record TermsAgreeRequest(
        @NotEmpty List<Long> agreedTermsIds,
        @Pattern(regexp = "^$|^\\S{2,12}$") String nickname,
        String lang
) {
}
