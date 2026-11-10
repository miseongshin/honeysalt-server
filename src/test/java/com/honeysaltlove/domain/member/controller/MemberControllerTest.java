package com.honeysaltlove.domain.member.controller;

import com.honeysaltlove.domain.member.auth.LoginMember;
import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.entity.Provider;
import com.honeysaltlove.domain.member.repository.MemberRepository;
import com.honeysaltlove.global.common.Lang;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    @Autowired MemberRepository memberRepository;

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
    @DisplayName("탈퇴하면 200 · 세션이 끊겨 같은 세션으로 내 정보를 보면 401")
    void withdrawLogsOut() throws Exception {
        // given
        Member member = memberRepository.saveAndFlush(Member.join(Provider.GOOGLE, "g-c3", "떠날벌", Lang.KO));
        MockHttpSession session = new MockHttpSession();
        Cookie csrf = mockMvc.perform(get("/api/web/v1/terms").session(session))
                .andReturn().getResponse().getCookie("XSRF-TOKEN");

        // when
        mockMvc.perform(delete("/api/web/v1/member").session(session)
                        .with(authentication(memberToken("g-c3", member.getId())))
                        .cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"));

        // then
        mockMvc.perform(get("/api/web/v1/member").session(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("개정 약관에 아직 동의하지 않은 회원(가입 대기 권한)도 탈퇴할 수 있다 — 약관 제13조")
    void reconsentMemberCanWithdraw() throws Exception {
        // given
        Member member = memberRepository.saveAndFlush(Member.join(Provider.GOOGLE, "g-c4", "동의안한벌", Lang.KO));
        MockHttpSession session = new MockHttpSession();
        Cookie csrf = mockMvc.perform(get("/api/web/v1/terms").session(session))
                .andReturn().getResponse().getCookie("XSRF-TOKEN");
        OAuth2AuthenticationToken reconsent = token("g-c4", member.getId(), "ROLE_PENDING");

        // when · then
        mockMvc.perform(delete("/api/web/v1/member").session(session)
                        .with(authentication(reconsent))
                        .cookie(csrf).header("X-XSRF-TOKEN", csrf.getValue()))
                .andExpect(status().isOk());
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

    private static OAuth2AuthenticationToken memberToken(String providerUid, Long memberId) {
        return token(providerUid, memberId, "ROLE_USER");
    }

    private static OAuth2AuthenticationToken token(String providerUid, Long memberId, String role) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        OidcIdToken token = OidcIdToken.withTokenValue("test").subject(providerUid)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        LoginMember login = new LoginMember(new DefaultOidcUser(authorities, token), memberId, Provider.GOOGLE,
                providerUid, authorities);
        return new OAuth2AuthenticationToken(login, authorities, "google");
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
