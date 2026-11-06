package com.honeysaltlove.domain.reaction.entity;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.global.common.BaseCreatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 신고. 고민 · 생각꿀팁 · 스케줄 · 활동 · 댓글에 한다 (한 사용자는 대상마다 한 번)
 * <pre>
 * - 신고 내용은 고칠 수 없다 · 운영자가 확인하면 처리 시각(handledAt)만 기록한다
 * - 같은 대상에 세 번째 신고가 접수되면 관리자에게 이메일을 한 번 발송한다 (서비스에서 · 자동 숨김 없음)
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
public class HoneyComplaint extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;                     // FK 없음 — 대상 테이블이 여러 개

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ComplaintReason reason;

    @Column(name = "complaint_text", columnDefinition = "text")
    private String complaintText;              // 사유가 기타(ETC)일 때 입력하는 설명

    @Column(name = "handled_at")
    private Instant handledAt;                 // null 이면 대기

    /**
     * 신고한다
     * @author shinm
     * @since 2026-11-06
     */
    public static HoneyComplaint report(Member member, TargetType targetType, Long targetId,
                                        ComplaintReason reason, String complaintText) {
        HoneyComplaint c = new HoneyComplaint();
        c.member = member;
        c.targetType = targetType;
        c.targetId = targetId;
        c.reason = reason;
        c.complaintText = complaintText;
        return c;
    }

    /**
     * 운영자가 확인했다
     * @author shinm
     * @since 2026-11-06
     */
    public void handle(Instant now) {
        this.handledAt = now;
    }
}
