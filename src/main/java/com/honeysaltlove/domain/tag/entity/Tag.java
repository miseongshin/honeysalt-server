package com.honeysaltlove.domain.tag.entity;

import com.honeysaltlove.global.common.BaseTimeEntity;
import com.honeysaltlove.global.common.Lang;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.text.Normalizer;
import java.util.Locale;

/**
 * 태그.
 * <pre>
 * - 이름만으로 중복을 막는다 (uk_tag_name) — 묶음을 옮기거나 없애도 태그는 한 행이다
 * - 이름은 normalize() 로 맞춘 값으로 저장 · 조회한다 ("#광고 " · "광-고!" · "AD" 로 차단을 피하지 못하게)
 *   - 기호는 받지 않는다: C++ · C# · C 는 모두 c 로 합쳐진다 (+ · # 를 받으면 "광고+" 로 차단을 피할 수 있어서)
 *   - 정규화 뒤 빈 이름("#!!!" · 이모지만)은 서비스에서 거부한다
 * - #상황 태그: 사용자가 글에 #으로 만든다 · 묶음 없음
 * - 회원 태그: 운영자가 맞춤설정 묶음(TagGroup)에 넣는다 · 같은 이름이면 #상황 태그와 같은 태그다
 * - 언어(lang)는 처음 만든 사람의 언어 · 기록만 하고 중복 기준이나 조회 조건으로 쓰지 않는다
 * - 이름은 50자까지 저장한다 (작성 시 한국어 · 중국어 · 일본어는 10자, 그 외 언어는 20자 — 검증은 서비스에서)
 * - 사용 횟수(useCount)는 스케줄러가 1시간마다 공개 글만 세어 값을 갱신한다
 * </pre>
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag_group", length = 20)
    private TagGroup tagGroup;                 // 맞춤설정 묶음 · #상황 태그는 null

    @Column(length = 20)
    private Lang lang;                         // 처음 만든 사람의 언어 · 기록만

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "use_count", nullable = false)
    private int useCount;                      // 태그가 붙은 공개 글 수 · 많이 찾는 상황

    /**
     * 태그 이름을 맞춘다.
     * <pre>
     * 1. NFKC: 모양만 다른 글자를 하나로 — 전각 ＡＤ · 꾸밈 글꼴 𝐀𝐃 𝓐𝓓 → AD · 합자 ﬁ → fi · 풀린 한글 자모 → 붙은 한글 · e + ´ → é
     *    단, 키보드로 친 한글 자음 · 모음(ㄱ~ㆎ, U+3131~318E)은 NFKC 에 넣지 않는다 — 조합용 ᄏ 로 바뀌어 ㅋㅋ · ㅠㅠ 태그가 깨진다
     * 2. 글자 · 결합 부호 · 숫자만 남긴다 (# · 공백 · 기호 · 이모지는 지운다 · 태국어 모음 · 성조 같은 결합 부호는 지우면 다른 낱말이 되므로 남긴다)
     * 3. 영문은 소문자로
     * </pre>
     * 태그를 만들 때 · 찾을 때 · 차단할 때 모두 이 값을 쓴다
     * @author shinm
     * @since 2026-11-06
     */
    public static String normalize(String raw) {
        StringBuilder out = new StringBuilder(raw.length());
        StringBuilder run = new StringBuilder();             // NFKC 에 넣을 조각 (한글 자음 · 모음 사이)
        for (int i = 0; i < raw.length(); ) {
            int cp = raw.codePointAt(i);
            if (cp >= 0x3131 && cp <= 0x318E) {              // 키보드 한글 자음 · 모음은 그대로
                out.append(Normalizer.normalize(run, Normalizer.Form.NFKC));
                run.setLength(0);
                out.appendCodePoint(cp);
            } else {
                run.appendCodePoint(cp);
            }
            i += Character.charCount(cp);                    // 𝐀 처럼 두 칸(char)짜리 글자도 하나로 센다
        }
        out.append(Normalizer.normalize(run, Normalizer.Form.NFKC));
        return out.toString()
                .replaceAll("[^\\p{L}\\p{M}\\p{N}]", "")
                .toLowerCase(Locale.ROOT);
    }

    /**
     * 태그를 만든다 (#상황 태그). 처음 만든 사람의 언어를 함께 저장한다
     * @author shinm
     * @since 2026-11-06
     */
    public static Tag create(String name, Lang lang) {
        Tag t = new Tag();
        t.name = normalize(name);
        t.lang = lang;
        return t;
    }

    /**
     * 회원 태그를 만든다. 운영자가 맞춤설정 묶음에 넣는다
     * @author shinm
     * @since 2026-11-06
     */
    public static Tag memberTag(TagGroup tagGroup, String name, Lang lang) {
        Tag t = create(name, lang);
        t.tagGroup = tagGroup;
        return t;
    }

    /**
     * 맞춤설정 묶음을 바꾼다. null 이면 묶음에서 뺀다 (#상황 태그로)
     * @author shinm
     * @since 2026-11-06
     */
    public void changeGroup(TagGroup tagGroup) {
        this.tagGroup = tagGroup;
    }
}
