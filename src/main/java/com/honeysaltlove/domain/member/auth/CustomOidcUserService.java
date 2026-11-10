package com.honeysaltlove.domain.member.auth;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.service.LoginService;
import com.honeysaltlove.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 구글 로그인 성공 뒤 회원을 찾아 세션에 넣을 로그인 회원(LoginMember)을 만든다
 * <pre>
 * - 회원이 있으면: 마지막 로그인 시각을 남기고 ROLE_USER · ROLE_ADMIN
 * - 없으면: 가입 대기 ROLE_PENDING — 약관 · 닉네임 화면으로 보낸다 (LoginSuccessHandler)
 * - 회원인데 개정된 필수 약관에 아직 동의하지 않았으면: 회원 번호는 두고 ROLE_PENDING — 동의하면 회원 권한이 된다
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 * 2026-11-10        shinm              개정된 약관에 아직 동의하지 않은 회원도 ROLE_PENDING
 */
@Component
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private static final String ROLE_PENDING = "ROLE_PENDING";

    private final LoginService loginService;
    private final MemberService memberService;

    @Override
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(request);
        OAuth2UserInfo info = OAuth2UserInfo.of(request.getClientRegistration().getRegistrationId(), oidcUser);

        Instant now = Instant.now();
        Optional<Member> member = loginService.login(info.provider(), info.providerUid(), now);
        String role = member
                .filter(m -> memberService.hasAgreedCurrentTerms(m.getId(), now))   // 개정된 약관에 아직 동의하지 않았으면 가입 대기처럼
                .map(m -> "ROLE_" + m.getRole().name())
                .orElse(ROLE_PENDING);

        return new LoginMember(oidcUser, member.map(Member::getId).orElse(null),
                info.provider(), info.providerUid(), List.of(new SimpleGrantedAuthority(role)));
    }
}
