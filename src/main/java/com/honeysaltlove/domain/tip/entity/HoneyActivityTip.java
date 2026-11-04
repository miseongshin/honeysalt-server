package com.honeysaltlove.domain.tip.entity;

import com.honeysaltlove.domain.activity.entity.HoneyActivity;
import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.worry.entity.HoneyWorry;
import com.honeysaltlove.global.common.BaseCreatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * 활동꿀팁. “이 활동이 이 고민에 도움이 됐다” — 수정하지 않고 등록하거나 삭제한다
 * 고민 상세의 “이 활동이 도움이 됐다고 한 N명”을 여기서 센다
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
@Getter
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_honey_activity_tip_worry_activity_member",
        columnNames = {"worry_id", "activity_id", "member_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HoneyActivityTip extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_tip_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worry_id", nullable = false)
    private HoneyWorry worry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private HoneyActivity activity;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;                     // 추천한 사람

    /**
     * 고민에 활동을 추천한다
     * @author shinm
     * @since 2026-11-04
     */
    public static HoneyActivityTip recommend(HoneyWorry worry, HoneyActivity activity, Member member) {
        HoneyActivityTip t = new HoneyActivityTip();
        t.worry = worry;
        t.activity = activity;
        t.member = member;
        return t;
    }
}
