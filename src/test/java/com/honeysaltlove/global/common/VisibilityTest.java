package com.honeysaltlove.global.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 공개 · 숨김 규칙 — DB 없이
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
class VisibilityTest {

    @Test
    @DisplayName("숨기면 상태가 HIDDEN으로 바뀌고, 숨긴 사람과 사유가 기록된다")
    void hide() {
        // given
        Visibility v = Visibility.visible();
        Instant now = Instant.parse("2026-11-04T00:00:00Z");

        // when
        v.hide("AI: 광고", now);

        // then
        assertThat(v.getStatus()).isEqualTo(Status.HIDDEN);
        assertThat(v.getHiddenReason()).isEqualTo("AI: 광고");
        assertThat(v.getHiddenAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("다시 공개하면 숨긴 시각과 사유가 지워진다")
    void show() {
        // given
        Visibility v = Visibility.visible();
        v.hide("관리자: 욕설", Instant.now());

        // when
        v.show();

        // then
        assertThat(v.getStatus()).isEqualTo(Status.PUBLIC);
        assertThat(v.getHiddenReason()).isNull();
        assertThat(v.getHiddenAt()).isNull();
    }
}
