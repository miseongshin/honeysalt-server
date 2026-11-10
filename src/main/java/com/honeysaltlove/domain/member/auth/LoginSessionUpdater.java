package com.honeysaltlove.domain.member.auth;

import com.honeysaltlove.domain.member.entity.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 약관에 동의하면 세션의 로그인 회원을 회원 권한(ROLE_USER · ROLE_ADMIN)으로 바꾼다 — 다시 로그인하지 않아도 되게
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
@Component
public class LoginSessionUpdater {

    private final SecurityContextRepository repository = new HttpSessionSecurityContextRepository();

    /**
     * 가입 대기 → 회원
     * @author shinm
     * @since 2026-11-10
     */
    public void promote(Authentication authentication, LoginMember login, Member member,
                        HttpServletRequest request, HttpServletResponse response) {
        LoginMember updated = new LoginMember(login.getOidcUser(), member.getId(), login.getProvider(),
                login.getProviderUid(), List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name())));
        String registrationId = authentication instanceof OAuth2AuthenticationToken token
                ? token.getAuthorizedClientRegistrationId()
                : login.getProvider().name().toLowerCase();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new OAuth2AuthenticationToken(updated, updated.getAuthorities(), registrationId));
        SecurityContextHolder.setContext(context);
        repository.saveContext(context, request, response);
    }
}
