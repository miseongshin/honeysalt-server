package com.honeysaltlove.domain.member.auth;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.service.LoginService;
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
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
@Component
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private static final String ROLE_PENDING = "ROLE_PENDING";

    private final LoginService loginService;

    @Override
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(request);
        OAuth2UserInfo info = OAuth2UserInfo.of(request.getClientRegistration().getRegistrationId(), oidcUser);

        Optional<Member> member = loginService.login(info.provider(), info.providerUid(), Instant.now());
        String role = member.map(m -> "ROLE_" + m.getRole().name()).orElse(ROLE_PENDING);

        return new LoginMember(oidcUser, member.map(Member::getId).orElse(null),
                info.provider(), info.providerUid(), List.of(new SimpleGrantedAuthority(role)));
    }
}
