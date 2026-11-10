package com.honeysaltlove.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.honeysaltlove.domain.member.entity.Terms;
import com.honeysaltlove.domain.member.entity.TermsType;

import java.time.Instant;

/**
 * 약관 하나 — body 는 DB 의 블록 JSON 을 그대로 내보낸다 (문자열로 감싸지 않는다)
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
public record TermsResponse(
        Long termsId,
        TermsType type,
        String version,
        boolean required,
        Instant effectiveAt,
        @JsonRawValue String body
) {

    public static TermsResponse from(Terms terms) {
        return new TermsResponse(terms.getId(), terms.getType(), terms.getVersion(), terms.isRequired(),
                terms.getEffectiveAt(), terms.getBody());
    }
}
