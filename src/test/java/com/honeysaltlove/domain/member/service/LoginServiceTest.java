package com.honeysaltlove.domain.member.service;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.entity.Provider;
import com.honeysaltlove.domain.member.repository.MemberRepository;
import com.honeysaltlove.global.common.Lang;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 소셜 로그인 뒤 회원 찾기 — 테스트가 끝나면 롤백한다(@Transactional)
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
class LoginServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-10T09:00:00Z");

    @Autowired LoginService loginService;
    @Autowired MemberRepository memberRepository;

    @Test
    @DisplayName("가입한 회원이면 찾아지고 마지막 로그인 시각이 남는다")
    void loginMember() {
        // given
        Member member = memberRepository.saveAndFlush(Member.join(Provider.GOOGLE, "g-login", "로그인벌", Lang.KO));

        // when
        Optional<Member> found = loginService.login(Provider.GOOGLE, "g-login", NOW);

        // then
        assertThat(found).contains(member);
        assertThat(member.getLastLoginAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("처음 온 사람이면 비어 있다 (가입 대기)")
    void firstVisit() {
        // when · then
        assertThat(loginService.login(Provider.GOOGLE, "g-new", NOW)).isEmpty();
    }

    @Test
    @DisplayName("탈퇴한 회원은 같은 소셜 계정으로 로그인해도 찾아지지 않는다 (새로 가입)")
    void withdrawnMember() {
        // given
        Member member = memberRepository.saveAndFlush(Member.join(Provider.GOOGLE, "g-gone", "떠난벌", Lang.KO));
        member.withdraw(NOW);
        memberRepository.flush();

        // when · then
        assertThat(loginService.login(Provider.GOOGLE, "g-gone", NOW)).isEmpty();
    }
}
