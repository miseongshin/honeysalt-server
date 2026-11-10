package com.honeysaltlove.domain.member.repository;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.entity.MemberNicknameHistory;
import com.honeysaltlove.domain.member.entity.MemberTermsHistory;
import com.honeysaltlove.domain.member.entity.Provider;
import com.honeysaltlove.domain.member.entity.ServiceType;
import com.honeysaltlove.domain.member.entity.Terms;
import com.honeysaltlove.domain.member.entity.TermsType;
import com.honeysaltlove.global.common.Lang;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 회원 테이블의 제약 · 등록일 확인 — love_local(터널)에 붙어 돌고, 테스트가 끝나면 롤백한다(@Transactional)
 * Docker 가 없어 Testcontainers 대신 개발 DB 를 쓴다
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired MemberNicknameHistoryRepository nicknameHistoryRepository;
    @Autowired TermsRepository termsRepository;
    @Autowired MemberTermsHistoryRepository termsHistoryRepository;

    private Member newMember(String providerUid, String nickname) {
        return Member.join(Provider.GOOGLE, providerUid, nickname, Lang.KO);
    }

    @Test
    @DisplayName("저장하면 등록일 · 수정일이 채워진다")
    void auditing() {
        // given
        Member member = newMember("g-1", "꿀벌하나");

        // when
        Member saved = memberRepository.saveAndFlush(member);

        // then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("같은 소셜 계정으로 두 번 가입할 수 없다")
    void sameSocialAccount() {
        // given
        memberRepository.saveAndFlush(newMember("g-2", "꿀벌둘"));

        // when · then
        assertThatThrownBy(() -> memberRepository.saveAndFlush(newMember("g-2", "꿀벌셋")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("탈퇴하면 개인정보가 비워지고, 같은 소셜 계정 · 닉네임으로 다시 가입할 수 있다")
    void withdrawThenJoinAgain() {
        // given
        Member old = memberRepository.saveAndFlush(newMember("g-5", "다시온벌"));

        // when
        old.withdraw(Instant.parse("2026-11-10T00:00:00Z"));
        memberRepository.saveAndFlush(old);
        Member again = memberRepository.saveAndFlush(newMember("g-5", "다시온벌"));

        // then
        assertThat(old.getProviderUid()).isNull();
        assertThat(old.getNickname()).isNull();
        assertThat(again.getId()).isNotEqualTo(old.getId());
    }

    @Test
    @DisplayName("닉네임은 겹칠 수 없다")
    void sameNickname() {
        // given
        memberRepository.saveAndFlush(newMember("g-3", "같은이름"));

        // when · then
        assertThatThrownBy(() -> memberRepository.saveAndFlush(newMember("g-4", "같은이름")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("닉네임을 바꾸면 바꾸기 전 이름이 남는다")
    void nicknameHistory() {
        // given
        Member member = memberRepository.saveAndFlush(newMember("g-5", "예전이름"));

        // when
        MemberNicknameHistory history = member.changeNickname("새이름");
        nicknameHistoryRepository.saveAndFlush(history);

        // then
        List<MemberNicknameHistory> histories = nicknameHistoryRepository.findByMemberIdOrderByCreatedAtDesc(member.getId());
        assertThat(histories).extracting(MemberNicknameHistory::getNickname).containsExactly("예전이름");
        assertThat(member.getNickname()).isEqualTo("새이름");
    }

    @Test
    @DisplayName("같은 약관 버전에 두 번 동의할 수 없다")
    void sameTermsTwice() {
        // given
        Member member = memberRepository.saveAndFlush(newMember("g-6", "약관동의"));
        Terms terms = termsRepository.saveAndFlush(
                Terms.create(ServiceType.HONEYSALTLOVE, TermsType.SERVICE, Lang.KO, "test-2026-11", "이용약관", true,
                        Instant.parse("2026-11-01T00:00:00Z")));
        termsHistoryRepository.saveAndFlush(MemberTermsHistory.agree(member, terms));

        // when · then
        assertThatThrownBy(() -> termsHistoryRepository.saveAndFlush(MemberTermsHistory.agree(member, terms)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
