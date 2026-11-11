package com.honeysaltlove.domain.member.entity;

import com.honeysaltlove.domain.tag.entity.Tag;
import com.honeysaltlove.global.common.BaseTimeEntity;
import com.honeysaltlove.global.common.Lang;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

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
 * 2026-11-06        shinm              태그 연결 (member_tag)
 * 2026-11-10        shinm              탈퇴 (status · withdrawnAt · withdraw) — 개인정보처리방침대로 탈퇴하면 바로 비운다
 * 2026-11-10        shinm              출생연도 바꾸기
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

    @Column(name = "provider_uid", length = 100)
    private String providerUid;                // provider와 조합한 값이 중복되면 안 된다 (UK) · 탈퇴하면 null

    @Column(length = 12, unique = true)
    private String nickname;                   // 2~12자 (시안의 닉네임 규칙) · 탈퇴하면 null

    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(nullable = false, length = 20)
    private Lang lang;                         // 가입할 때 화면(Vue)의 언어 · DB 에는 ko · en … (LangConverter)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    @Column(name = "withdrawn_at")
    private Instant withdrawnAt;               // 탈퇴일

    @Column(name = "last_login_at")
    private Instant lastLoginAt;               // 마지막 로그인 · 탈퇴하면 null

    @ManyToMany
    @JoinTable(name = "member_tag",
            joinColumns = @JoinColumn(name = "member_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();   // 대표태그로 고른 회원 태그

    private Member(Provider provider, String providerUid, String nickname, Lang lang) {
        this.provider = provider;
        this.providerUid = providerUid;
        this.nickname = nickname;
        this.lang = lang;
        this.role = Role.USER;
        this.status = MemberStatus.ACTIVE;
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
     * 닉네임을 바꾼다. 닉네임 변경 이력을 남기기 위해 변경 전 닉네임을 이력(MemberNicknameHistory)으로 반환한다
     * 반환된 이력은 호출한 서비스에서 저장한다
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

    /**
     * 출생연도를 바꾼다
     * @author shinm
     * @since 2026-11-10
     */
    public void changeBirthYear(Integer birthYear) {
        this.birthYear = birthYear;            // null 이면 비운다 (선택 항목)
    }

    /**
     * 태그를 새로 고른 것으로 바꾼다 (빠진 태그의 연결 행은 지우고, 새 태그는 넣는다)
     * @author shinm
     * @since 2026-11-06
     */
    public void replaceTags(Set<Tag> newTags) {
        tags.retainAll(newTags);   // clear() 를 쓰면 자기 tags 를 넘겼을 때 모두 사라진다
        tags.addAll(newTags);
    }

    /**
     * 탈퇴한다. 이 행의 개인정보(소셜 회원번호 · 닉네임 · 출생연도 · 마지막 로그인)를 비우고 내 정보 태그를 지운다
     * 회원 행은 남은 글 · 좋아요 · 신고 기록이 가리켜서 남긴다
     * 닉네임 백업 · 꿀팁 바구니 · 의견 · 약관 동의 기록 · 앱 연결은 호출한 서비스가 같은 트랜잭션에서 삭제한다
     * @author shinm
     * @since 2026-11-10
     */
    public void withdraw(Instant now) {
        this.status = MemberStatus.WITHDRAWN;
        this.withdrawnAt = now;
        this.providerUid = null;
        this.nickname = null;
        this.birthYear = null;
        this.lastLoginAt = null;
        this.tags.clear();
    }
}
