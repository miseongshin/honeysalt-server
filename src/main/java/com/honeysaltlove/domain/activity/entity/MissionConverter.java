package com.honeysaltlove.domain.activity.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Mission ↔ DB 번호(1~7). Mission 타입 엔티티 필드에 자동 적용된다(autoApply)
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
@Converter(autoApply = true)
public class MissionConverter implements AttributeConverter<Mission, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Mission mission) {
        return mission == null ? null : mission.getNo();
    }

    @Override
    public Mission convertToEntityAttribute(Integer no) {
        return no == null ? null : Mission.from(no);
    }
}
