package com.honeysaltlove.domain.member.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 자동 닉네임 — 2~12자 · 띄어쓰기 없음 규칙 안에 들고, 숫자를 붙일 자리(세 자리)가 남는다 (DB 없이 도는 테스트)
 *
 * @author shinm
 * @since 2026-11-11
 */
class NicknameGeneratorTest {

    private final NicknameGenerator generator = new NicknameGenerator();

    @Test
    @DisplayName("만든 이름은 띄어쓰기 없이 2~9자 — 뒤에 숫자 세 자리를 붙여도 12자를 넘지 않는다")
    void generate() {
        for (int i = 0; i < 1000; i++) {
            assertThat(generator.generate()).matches("^\\S{2,9}$").doesNotContainPattern("\\d");
        }
    }
}
