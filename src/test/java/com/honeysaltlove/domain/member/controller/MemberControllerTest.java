package com.honeysaltlove.domain.member.controller;

import com.honeysaltlove.domain.member.auth.LoginMember;
import com.honeysaltlove.domain.member.entity.Provider;
import com.honeysaltlove.domain.member.entity.Terms;
import com.honeysaltlove.domain.member.service.TermsService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 가입 마치기 API — 동의하면 같은 세션이 회원 권한으로 바뀌는지 확인한다 · 끝나면 롤백
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired TermsService termsService;

    @Test
    @DisplayName("가입 대기가 약관에 동의하면 200 · 같은 세션으로 내 정보 API 를 부를 때 더 이상 TERMS_REQUIRED 가 아니다")
    void signupPromotesSession() throws Exception {
        // given
        MockHttpSession session = new MockHttpSession();
        Cookie csrf = mockMvc.perform(get("/api/web/v1/terms").session(session))
                .andReturn().getResponse().getCookie("XSRF-TOKEN");
        String ids = termsService.findCurrentRequired(Instant.now()).stream()
                .map(Terms::getId).map(String::valueOf).collect(Collectors.joining(","));

        // when — 가입 마치기
        mockMvc.perform(post("/api/web/v1/member/terms").session(session)
                        .with(authentication(pendingToken("g-c1")))
                        .cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agreedTermsIds\":[" + ids + "],\"nickname\":\"가입벌\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.nickname").value("가입벌"))
                .andExpect(jsonPath("$.data.joined").value(true));

        // then — 같은 세션: 시큐리티를 지나 컨트롤러가 없는 주소라 404 (가입 대기였다면 403 TERMS_REQUIRED)
        mockMvc.perform(get("/api/web/v1/member/posts").session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("닉네임에 띄어쓰기가 있으면 400 VALIDATION")
    void invalidNickname() throws Exception {
        MockHttpSession session = new MockHttpSession();
        Cookie csrf = mockMvc.perform(get("/api/web/v1/terms").session(session))
                .andReturn().getResponse().getCookie("XSRF-TOKEN");

        mockMvc.perform(post("/api/web/v1/member/terms").session(session)
                        .with(authentication(pendingToken("g-c2")))
                        .cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"agreedTermsIds\":[1],\"nickname\":\"꿀 벌\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"));
    }

    private static OAuth2AuthenticationToken pendingToken(String providerUid) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_PENDING"));
        OidcIdToken token = OidcIdToken.withTokenValue("test").subject(providerUid)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        LoginMember login = new LoginMember(new DefaultOidcUser(authorities, token), null, Provider.GOOGLE,
                providerUid, authorities);
        return new OAuth2AuthenticationToken(login, authorities, "google");
    }
}
