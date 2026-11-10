package com.honeysaltlove.domain.member.auth;

import com.honeysaltlove.domain.member.entity.Provider;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

/**
 * 소셜 로그인 응답에서 회원번호 하나만 꺼낸다 (소셜마다 응답 모양이 달라서 나눈다)
 * <pre>
 * - 지금은 구글만 · 나중에 카카오 · 네이버를 더하면 구현 클래스와 of() 의 줄만 늘린다
 * - 이메일 · 이름은 받지 않는다 (개인정보를 적게)
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
public interface OAuth2UserInfo {

    Provider provider();

    String providerUid();

    /**
     * 로그인한 소셜(registrationId)에 맞는 회원번호를 꺼낸다
     * @author shinm
     * @since 2026-11-10
     */
    static OAuth2UserInfo of(String registrationId, OidcUser user) {
        if ("google".equals(registrationId)) {
            return new GoogleUserInfo(user.getSubject());
        }
        throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"));
    }
}
