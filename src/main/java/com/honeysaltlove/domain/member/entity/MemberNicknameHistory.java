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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 닉네임 백업. 닉네임을 바꿀 때마다 바꾸기 전 이름을 남긴다 · 고치지 않음 (created_at = 바꾼 때)
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberNicknameHistory extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nickname_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 12)
    private String nickname;                   // 바꾸기 전 닉네임

    private MemberNicknameHistory(Member member, String nickname) {
        this.member = member;
        this.nickname = nickname;
    }

    static MemberNicknameHistory of(Member member, String nickname) {
        return new MemberNicknameHistory(member, nickname);
    }
}
