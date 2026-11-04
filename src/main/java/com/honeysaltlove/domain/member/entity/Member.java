package com.honeysaltlove.domain.member.entity;

import com.honeysaltlove.global.common.BaseTimeEntity;
import com.honeysaltlove.global.common.Lang;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 회원.
 * <pre>
 * - 구글 계정으로만 들어온다(2차) · 비밀번호 없음
 * - 앱가입: 앱 키(member_app_key) 등록 → 앱에서 웹 가입하면 member_app_key.member_id 가 채워진다
 * </pre>
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
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_member_provider_uid", columnNames = {"provider", "provider_uid"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_uid", nullable = false, length = 100)
    private String providerUid;                // provider 와 묶어서 겹치면 안 됨 (UK)

    @Column(nullable = false, length = 12, unique = true)
    private String nickname;                   // 2~12자 (시안의 닉네임 규칙)

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(nullable = false, length = 20)
    private Lang lang;                         // 가입할 때 화면(vue)의 언어 · DB 에는 ko · en … (LangConverter)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;               // 휴면 판단

    private Member(Provider provider, String providerUid, String nickname, Lang lang) {
        this.provider = provider;
        this.providerUid = providerUid;
        this.nickname = nickname;
        this.lang = lang;
        this.role = Role.USER;
    }

    /**
     * 회원을 등록한다.
     * @author shinm
     * @since 2026-11-04
     */
    public static Member join(Provider provider, String providerUid, String nickname, Lang lang) {
        return new Member(provider, providerUid, nickname, lang);
    }

    /**
     * 닉네임을 바꾼다. - 닉네임 이력을 위해 변경 전 닉네임을 이력(MemberNicknameHistory)으로 return한다
     * 돌려받은 이력은 부르는 쪽(서비스)이 저장한다
     * @author shinm
     * @since 2026-11-04
     */
    public MemberNicknameHistory changeNickname(String newNickname) {
        MemberNicknameHistory history = MemberNicknameHistory.of(this, this.nickname);
        this.nickname = newNickname;
        return history;
    }

    /**
     * 로그인한 시점 기록
     * @author shinm
     * @since 2026-11-04
     */
    public void recordLogin(Instant now) {
        this.lastLoginAt = now;
    }
}
