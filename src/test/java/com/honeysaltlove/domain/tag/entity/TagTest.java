package com.honeysaltlove.domain.tag.entity;

import com.honeysaltlove.global.common.Lang;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 태그 이름 정규화 확인 — DB 없이 돈다
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
class TagTest {

    @Test
    @DisplayName("이름은 글자 · 결합 부호 · 숫자만 남기고, 영문은 소문자로 맞춘다")
    void normalize() {
        assertThat(Tag.normalize("#광고 ")).isEqualTo("광고");
        assertThat(Tag.normalize("##AD")).isEqualTo("ad");
        assertThat(Tag.normalize("광-고!")).isEqualTo("광고");
        assertThat(Tag.normalize("잠이 안\t올 때😴")).isEqualTo("잠이안올때");
        assertThat(Tag.normalize("5분_산책")).isEqualTo("5분산책");
    }

    @Test
    @DisplayName("전각 영문과 풀린 한글 자모도 같은 이름으로 맞춘다")
    void fullWidthAndNfc() {
        assertThat(Tag.normalize("ＡＤ")).isEqualTo("ad");
        assertThat(Tag.normalize("\u1100\u1161\u11BC")).isEqualTo("강");   // ㄱ + ㅏ + ㅇ 이 풀린 채로 온 경우
        assertThat(Tag.normalize("e\u0301")).isEqualTo("\u00E9");          // e + ´ → é
    }

    @Test
    @DisplayName("꾸밈 글꼴 · 합자로 써도 같은 이름으로 맞춘다 (차단을 피하지 못하게)")
    void decorated() {
        assertThat(Tag.normalize("\uD835\uDC00\uD835\uDC03")).isEqualTo("ad");   // 𝐀𝐃 수학 굵은 글자
        assertThat(Tag.normalize("\uD835\uDCD0\uD835\uDCD3")).isEqualTo("ad");   // 𝓐𝓓 필체 꾸밈 글자
        assertThat(Tag.normalize("\uFB01t")).isEqualTo("fit");                       // ﬁ 합자
        assertThat(Tag.normalize("ㅋㅋ광고")).isEqualTo("ㅋㅋ광고");                         // 자음과 글자가 섞여도
    }

    @Test
    @DisplayName("키보드로 친 자음 · 모음 태그(ㅋㅋ · ㅠㅠ)는 그대로 둔다")
    void keepJamo() {
        assertThat(Tag.normalize("#ㅋㅋ")).isEqualTo("ㅋㅋ");
        assertThat(Tag.normalize("ㅠㅠ")).isEqualTo("ㅠㅠ");
    }

    @Test
    @DisplayName("결합 부호(태국어 모음 · 성조)는 지우지 않는다")
    void keepMarks() {
        assertThat(Tag.normalize("#นอนไม่หลับ")).isEqualTo("นอนไม่หลับ");
    }

    @Test
    @DisplayName("태그를 만들 때와 차단할 때 같은 규칙으로 맞춘다")
    void sameRule() {
        assertThat(Tag.create("#Stress ", Lang.KO).getName()).isEqualTo("stress");
        assertThat(TagBlocklist.block(" STRESS", "광고").getName()).isEqualTo("stress");
    }
}
