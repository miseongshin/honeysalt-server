package com.honeysaltlove.domain.member.entity;

import com.honeysaltlove.global.common.Lang;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 약관 초안 고치기 규칙 — DB 없이
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
class TermsTest {

    private final Instant effective = Instant.parse("2026-12-01T00:00:00Z");

    @Test
    @DisplayName("시행 전 초안은 고칠 수 있다")
    void editBeforeEffective() {
        // given
        Terms terms = Terms.create(ServiceType.HONEYSALTLOVE, TermsType.SERVICE, Lang.KO, "2026-12", "초안", true, effective);

        // when
        terms.editDraft("고친 초안", effective, Instant.parse("2026-11-20T00:00:00Z"));

        // then
        assertThat(terms.getBody()).isEqualTo("고친 초안");
    }

    @Test
    @DisplayName("시행된 약관은 고칠 수 없다")
    void editAfterEffective() {
        // given
        Terms terms = Terms.create(ServiceType.HONEYSALTLOVE, TermsType.SERVICE, Lang.KO, "2026-12", "본문", true, effective);

        // when · then
        assertThatThrownBy(() -> terms.editDraft("바꾼 본문", effective, Instant.parse("2026-12-02T00:00:00Z")))
                .isInstanceOf(IllegalStateException.class);
    }
}
