package com.honeysaltlove.domain.reaction.entity;

/**
 * 신고 사유. 기타(ETC)일 때만 설명(complaint_text)을 받는다
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public enum ComplaintReason {
    AD,             // 광고
    ABUSE,          // 욕설
    INAPPROPRIATE,  // 부적절
    ETC             // 기타
}
