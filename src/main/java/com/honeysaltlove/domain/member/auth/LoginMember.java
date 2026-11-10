package com.honeysaltlove.domain.member.auth;

import com.honeysaltlove.domain.member.entity.Provider;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.util.Collection;
import java.util.Map;

/**
 * 세션에 들어가는 로그인 회원
 * <pre>
 * - 가입한 회원: memberId 가 있고 권한은 ROLE_USER · ROLE_ADMIN
 * - 가입 대기(처음 온 사람): memberId 가 null 이고 권한은 ROLE_PENDING — 약관 동의 · 닉네임을 마치면 회원이 된다
 * - 다시 동의 대기(개정된 약관에 아직 동의하지 않은 회원): memberId 가 있고 권한은 ROLE_PENDING
 * - provider · providerUid 는 가입을 마칠 때 회원을 만들려고 들고 있다 · 로그에 남기지 않는다
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 * 2026-11-10        shinm              isPending 은 권한으로 본다 (다시 동의 대기 포함)
 */
@Getter
public class LoginMember implements OidcUser {

    private final OidcUser oidcUser;
    private final Long memberId;               // 가입 대기이면 null
    private final Provider provider;
    private final String providerUid;
    private final Collection<? extends GrantedAuthority> authorities;

    public LoginMember(OidcUser oidcUser, Long memberId, Provider provider, String providerUid,
                       Collection<? extends GrantedAuthority> authorities) {
        this.oidcUser = oidcUser;
        this.memberId = memberId;
        this.provider = provider;
        this.providerUid = providerUid;
        this.authorities = authorities;
    }

    /**
     * 약관 동의가 필요한지 — 가입 대기 · 다시 동의 대기 (화면을 가입 마치기로 보낸다)
     * @author shinm
     * @since 2026-11-10
     */
    public boolean isPending() {
        return authorities.stream().anyMatch(a -> "ROLE_PENDING".equals(a.getAuthority()));
    }

    /**
     * 시큐리티가 쓰는 이름 — 소셜 회원번호가 로그에 남지 않게 회원 번호를 쓴다
     */
    @Override
    public String getName() {
        return memberId == null ? "pending" : String.valueOf(memberId);
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser.getAttributes();
    }
}
