package com.honeysaltlove.domain.app.entity;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.global.common.BaseTimeEntity;
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

import java.time.Instant;

/**
 * 앱 키. 앱 하나당 행 하나 — 앱이 처음 실행할 때 만든다
 * <pre>
 * - 활동완료 · 도움돼요 기록은 앱 키만 확인한다
 * - cart 주고받기 · 웹 바로 들어가기는 앱 키 + 비밀값으로 확인한다 (비밀값은 저장하지 않고 해시만 둔다)
 * - 웹에서 연결하면 member 를 채운다 (기록은 옮기지 않는다) · 앱만 쓰면 비어 있다
 * </pre>
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 * 2026-11-10        shinm              연결 끊기 (탈퇴)
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberAppKey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_key_id")
    private Long id;

    @Column(name = "app_key", nullable = false, unique = true, length = 64)
    private String appKey;                     // 어느 앱인지 알아보는 값 · 요청 헤더 X-App-Key

    @Column(name = "app_hash", nullable = false, length = 64)
    private String appHash;                    // 비밀값의 SHA-256 해시

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;                     // 연결한 회원 · 앱만 쓰면 null

    @Column(name = "linked_at")
    private Instant linkedAt;                  // 웹 계정과 연결한 시각

    @Column(name = "last_used_at")
    private Instant lastUsedAt;                // 앱이 마지막으로 부른 시각 · 부를 때마다 고친다

    /**
     * 앱 키를 만든다 (앱이 처음 실행할 때)
     * @author shinm
     * @since 2026-11-06
     */
    public static MemberAppKey issue(String appKey, String appHash) {
        MemberAppKey k = new MemberAppKey();
        k.appKey = appKey;
        k.appHash = appHash;
        return k;
    }

    /**
     * 웹 계정과 연결한다 (앱이 웹에서 받은 1회용 연결 토큰으로 연결을 요청했을 때)
     * @author shinm
     * @since 2026-11-06
     */
    public void link(Member member, Instant now) {
        this.member = member;
        this.linkedAt = now;
    }

    /**
     * 웹 계정과의 연결을 끊는다 (회원이 탈퇴했을 때) — 앱 키는 남겨서 앱 혼자 계속 쓴다
     * @author shinm
     * @since 2026-11-10
     */
    public void unlink() {
        this.member = null;
        this.linkedAt = null;
    }

    /**
     * 비밀값을 다시 만든다 — 해시를 덮어써서 예전 비밀값은 저절로 못 쓴다
     * @author shinm
     * @since 2026-11-06
     */
    public void renewSecret(String appHash) {
        this.appHash = appHash;
    }

    /**
     * 앱이 불렀다
     * @author shinm
     * @since 2026-11-06
     */
    public void touch(Instant now) {
        this.lastUsedAt = now;
    }
}
