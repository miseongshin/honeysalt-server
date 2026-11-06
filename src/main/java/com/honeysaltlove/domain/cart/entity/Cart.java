package com.honeysaltlove.domain.cart.entity;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.global.common.BaseTimeEntity;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 활동바구니. 웹에서 담고 "내 꿀방울로 보내기" 하면 연결된 앱이 가져간다
 * <pre>
 * - 같은 것은 한 번만 담긴다 · 빼기 = 삭제
 * - 앱은 보낸 것(sentAt 있음)만 받고, 저장을 마친 cart_id 를 알리면(ack) 그 행을 바로 지운다 — 스케줄이면 send_count +1
 * - 지울 때 그 행이 이 앱에 연결된 회원의 것인지 같이 본다
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
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_cart_member_target",
        columnNames = {"member_id", "target_type", "target_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private CartTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;                     // FK 없음 — 대상 테이블이 여러 개

    @Column(length = 30)
    private String days;                       // 활동만 · 바구니에서 고른 요일 (예: MON,WED)

    @Column(name = "sent_at")
    private Instant sentAt;                    // 내 꿀방울로 보낸 시각 · null 이면 바구니에 있음

    /**
     * 바구니에 담는다
     * @author shinm
     * @since 2026-11-06
     */
    public static Cart put(Member member, CartTargetType targetType, Long targetId, String days) {
        Cart c = new Cart();
        c.member = member;
        c.targetType = targetType;
        c.targetId = targetId;
        c.days = days;
        return c;
    }

    /**
     * 내 꿀방울로 보낸다 — 이때부터 앱이 받을 수 있다
     * @author shinm
     * @since 2026-11-06
     */
    public void markSent(Instant now) {
        this.sentAt = now;
    }
}
