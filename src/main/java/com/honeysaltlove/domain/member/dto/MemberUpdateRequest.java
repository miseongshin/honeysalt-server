package com.honeysaltlove.domain.member.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

/**
 * 내 정보 수정 — PATCH /api/web/v1/member · 화면은 내 정보 수정 화면의 칸을 모두 보낸다
 * <pre>
 * - nickname: 비우면 그대로 둔다 · 2~12자, 띄어쓰기 없음 · 바꾸면 전 이름을 닉네임 백업에 남긴다
 * - birthYear: 비우면(null) 지운다 (선택 항목) · 만 14세 미만이 되는 해는 받지 않는다
 * - tagIds: 고른 내 정보 태그 전부 · 빈 목록이면 모두 지운다 · 운영자가 정한 맞춤설정 태그만 고를 수 있다
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
public record MemberUpdateRequest(
        @Pattern(regexp = "^$|^\\S{2,12}$") String nickname,
        @Min(1900) Integer birthYear,
        @NotNull List<Long> tagIds
) {
}
