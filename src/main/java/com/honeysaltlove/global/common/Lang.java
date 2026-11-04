package com.honeysaltlove.global.common;

import java.util.Arrays;

/**
 * 지원 언어 — honeysalt-vue 와 같은 11개 (ISO 639-1). DB 에는 소문자 코드(ko)로 저장한다 (LangConverter)
 * 언어를 늘리거나 줄일 때 vue 의 목록(router · locales)도 같이 고친다
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
public enum Lang {
    KO("ko"), EN("en"), JA("ja"), ZH("zh"), ES("es"), FR("fr"),
    DE("de"), PT("pt"), VI("vi"), ID("id"), TH("th");

    private final String code;

    Lang(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 화면(vue)이 보낸 코드로 찾는다. 목록에 없으면 영어(EN) — vue 와 같은 규칙
     * @author shinm
     * @since 2026-11-04
     */
    public static Lang from(String code) {
        return Arrays.stream(values())
                .filter(l -> l.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(EN);
    }
}
