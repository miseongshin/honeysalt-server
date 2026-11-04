package com.honeysaltlove.domain.activity.entity;

import java.util.Arrays;

/**
 * 꿀방울 앱의 미션 7개 — 테이블 없이 enum (앱 코드와 함께 바뀐다)
 * DB 에는 앱과 같은 번호(1~7)로 저장한다 (MissionConverter) · 이름 · 순서는 Vue 번역 파일에
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
public enum Mission {
    MOVE(1),        // 움직이기
    CLEAN(2),       // 청소하기
    RUB(3),         // 주무르기
    AUTO(4),        // 자동행동
    TOMORROW(5),    // 내일할일
    KNOW(6),        // 나를 알기
    DETOX(7);       // 디지털 디톡스

    private final int no;

    Mission(int no) {
        this.no = no;
    }

    public int getNo() {
        return no;
    }

    /**
     * 앱이 보낸 미션 번호로 찾는다
     * @author shinm
     * @since 2026-11-04
     */
    public static Mission from(int no) {
        return Arrays.stream(values())
                .filter(m -> m.no == no)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("없는 미션 번호: " + no));
    }
}
