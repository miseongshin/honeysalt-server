package com.honeysaltlove.domain.member.auth;

import com.honeysaltlove.domain.member.entity.Provider;

/**
 * 구글 회원번호 — openid 의 sub
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
public record GoogleUserInfo(String providerUid) implements OAuth2UserInfo {

    @Override
    public Provider provider() {
        return Provider.GOOGLE;
    }
}
