package com.honeysaltlove.global.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 화면(Vue)이 보낸 언어 코드 받기
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
class LangTest {

    @Test
    @DisplayName("화면의 언어 코드로 찾는다")
    void fromCode() {
        // given · when · then
        assertThat(Lang.from("ja")).isEqualTo(Lang.JA);
    }

    @Test
    @DisplayName("목록에 없는 언어는 영어로 받는다")
    void unsupported() {
        // given · when · then
        assertThat(Lang.from("xx")).isEqualTo(Lang.EN);
        assertThat(Lang.from(null)).isEqualTo(Lang.EN);
    }
}
