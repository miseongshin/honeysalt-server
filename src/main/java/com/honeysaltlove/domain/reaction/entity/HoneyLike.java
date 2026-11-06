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

/**
 * 좋아요. 고민 · 생각꿀팁 · 스케줄 · 활동 · 댓글에 누른다 (한 사용자는 대상마다 한 번)
 * <pre>
 * - 취소하면 행을 지운다 · 고치지 않으니 등록일만 둔다
 * - 좋아요 수(like_count)는 1분마다 다시 세어 맞춘다 — 1분 동안은 실제 좋아요 수와 다를 수 있다
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
public class HoneyLike extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;                     // FK 없음 — 대상 테이블이 여러 개

    /**
     * 좋아요를 누른다
     * @author shinm
     * @since 2026-11-06
     */
    public static HoneyLike press(Member member, TargetType targetType, Long targetId) {
        HoneyLike l = new HoneyLike();
        l.member = member;
        l.targetType = targetType;
        l.targetId = targetId;
        return l;
    }
}
