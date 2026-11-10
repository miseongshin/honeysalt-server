package com.honeysaltlove.domain.member.service;

import com.honeysaltlove.domain.member.auth.LoginMember;
import com.honeysaltlove.domain.member.dto.TermsAgreeRequest;
import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.entity.Provider;
import com.honeysaltlove.domain.member.entity.Terms;
import com.honeysaltlove.domain.member.repository.MemberRepository;
import com.honeysaltlove.global.common.BusinessException;
import com.honeysaltlove.global.common.Lang;
import com.honeysaltlove.global.common.ResultCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 가입 마치기 · 약관 다시 동의 — Flyway 기본 데이터(V7 honeysaltlove 약관)로 확인한다 · 끝나면 롤백
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
@SpringBootTest
@Transactional
class MemberServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-10T09:00:00Z");

    @Autowired MemberService memberService;
    @Autowired TermsService termsService;
    @Autowired MemberRepository memberRepository;

    @Test
    @DisplayName("가입 대기가 필수 약관에 모두 동의하면 회원이 되고, 닉네임을 비우면 자동으로 만든다")
    void joinWithAutoNickname() {
        // when
        Member member = memberService.agreeTerms(pending("g-s1", null), request(requiredIds(), null), NOW);

        // then
        assertThat(member.getId()).isNotNull();
        assertThat(member.getNickname()).matches("^\\S{2,12}$");
        assertThat(memberService.hasAgreedCurrentTerms(member.getId(), NOW)).isTrue();
    }

    @Test
    @DisplayName("필수 약관 하나라도 빠지면 400 VALIDATION")
    void missingRequired() {
        // given
        List<Long> onlyOne = requiredIds().subList(0, 1);

        // when · then
        assertThatThrownBy(() -> memberService.agreeTerms(pending("g-s2", null), request(onlyOne, null), NOW))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getResultCode()).isEqualTo(ResultCode.VALIDATION);
    }

    @Test
    @DisplayName("이미 쓰는 닉네임이면 409 DUPLICATE")
    void duplicateNickname() {
        // given
        memberRepository.saveAndFlush(Member.join(Provider.GOOGLE, "g-s3-other", "같은벌", Lang.KO));

        // when · then
        assertThatThrownBy(() -> memberService.agreeTerms(pending("g-s3", null), request(requiredIds(), "같은벌"), NOW))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getResultCode()).isEqualTo(ResultCode.DUPLICATE);
    }

    @Test
    @DisplayName("가입 마치기를 두 번 눌러도 회원은 하나다")
    void twice() {
        // when
        Member first = memberService.agreeTerms(pending("g-s4", null), request(requiredIds(), "두번벌"), NOW);
        Member second = memberService.agreeTerms(pending("g-s4", null), request(requiredIds(), "두번벌"), NOW);

        // then
        assertThat(second.getId()).isEqualTo(first.getId());
    }

    @Test
    @DisplayName("동의 기록이 지금 약관과 다른 회원은 다시 동의해야 하고, 동의하면 기록만 남는다")
    void reconsent() {
        // given — 회원은 있지만 지금 약관 버전에 동의한 기록이 없다 (약관이 개정된 경우와 같다)
        Member member = memberRepository.saveAndFlush(Member.join(Provider.GOOGLE, "g-s5", "다시벌", Lang.KO));
        assertThat(memberService.hasAgreedCurrentTerms(member.getId(), NOW)).isFalse();

        // when
        Member agreed = memberService.agreeTerms(pending("g-s5", member.getId()), request(requiredIds(), null), NOW);

        // then
        assertThat(agreed.getId()).isEqualTo(member.getId());
        assertThat(agreed.getNickname()).isEqualTo("다시벌");
        assertThat(memberService.hasAgreedCurrentTerms(member.getId(), NOW)).isTrue();
    }

    private List<Long> requiredIds() {
        return termsService.findCurrentRequired(NOW).stream().map(Terms::getId).toList();
    }

    private static TermsAgreeRequest request(List<Long> ids, String nickname) {
        return new TermsAgreeRequest(ids, nickname, null);
    }

    static LoginMember pending(String providerUid, Long memberId) {
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_PENDING"));
        OidcIdToken token = OidcIdToken.withTokenValue("test").subject(providerUid)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        return new LoginMember(new DefaultOidcUser(authorities, token), memberId, Provider.GOOGLE, providerUid, authorities);
    }
}
