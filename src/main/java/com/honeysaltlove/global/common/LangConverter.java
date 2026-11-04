package com.honeysaltlove.global.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Lang ↔ DB 소문자 코드(ko · en …). 엔티티의 Lang 칸에 저절로 붙는다(autoApply)
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
@Converter(autoApply = true)
public class LangConverter implements AttributeConverter<Lang, String> {

    @Override
    public String convertToDatabaseColumn(Lang lang) {
        return lang == null ? null : lang.getCode();
    }

    @Override
    public Lang convertToEntityAttribute(String code) {
        return code == null ? null : Lang.from(code);
    }
}
