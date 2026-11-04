package com.honeysaltlove.domain.member.entity;

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
 * 약관 동의 기록. 누가 어떤 약관 버전에 동의했는지 · 쌓기만 함 (created_at = 동의한 때)
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
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_member_terms_history_member_terms", columnNames = {"member_id", "terms_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberTermsHistory extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "terms_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "terms_id", nullable = false)
    private Terms terms;

    private MemberTermsHistory(Member member, Terms terms) {
        this.member = member;
        this.terms = terms;
    }

    /**
     * 동의 남기기
     * @author shinm
     * @since 2026-11-04
     */
    public static MemberTermsHistory agree(Member member, Terms terms) {
        return new MemberTermsHistory(member, terms);
    }
}
