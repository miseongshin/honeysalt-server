package com.honeysaltlove.domain.member.service;

import com.honeysaltlove.domain.app.entity.MemberAppKey;
import com.honeysaltlove.domain.app.repository.MemberAppKeyRepository;
import com.honeysaltlove.domain.cart.entity.Cart;
import com.honeysaltlove.domain.cart.entity.CartTargetType;
import com.honeysaltlove.domain.cart.repository.CartRepository;
import com.honeysaltlove.domain.feedback.entity.Feedback;
import com.honeysaltlove.domain.feedback.entity.FeedbackType;
import com.honeysaltlove.domain.feedback.repository.FeedbackRepository;
import com.honeysaltlove.domain.member.dto.MemberResponse;
import com.honeysaltlove.domain.member.dto.MemberUpdateRequest;
import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.entity.MemberStatus;
import com.honeysaltlove.domain.member.entity.MemberTermsHistory;
import com.honeysaltlove.domain.member.entity.Provider;
import com.honeysaltlove.domain.member.repository.MemberNicknameHistoryRepository;
import com.honeysaltlove.domain.member.repository.MemberRepository;
import com.honeysaltlove.domain.member.repository.MemberTermsHistoryRepository;
import com.honeysaltlove.domain.tag.entity.Tag;
import com.honeysaltlove.domain.tag.entity.TagGroup;
import com.honeysaltlove.domain.tag.repository.TagRepository;
import com.honeysaltlove.global.common.BusinessException;
import com.honeysaltlove.global.common.Lang;
import com.honeysaltlove.global.common.ResultCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 내 정보 · 수정 · 탈퇴 — 끝나면 롤백
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
class MemberMeServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-10T09:00:00Z");

    @Autowired MemberService memberService;
    @Autowired TermsService termsService;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberNicknameHistoryRepository nicknameHistoryRepository;
    @Autowired MemberTermsHistoryRepository termsHistoryRepository;
    @Autowired CartRepository cartRepository;
    @Autowired FeedbackRepository feedbackRepository;
    @Autowired MemberAppKeyRepository appKeyRepository;
    @Autowired TagRepository tagRepository;
    @Autowired EntityManager em;

    private Member member;
    private Tag interestTag;

    @BeforeEach
    void setUp() {
        member = memberRepository.saveAndFlush(Member.join(Provider.GOOGLE, "g-me", "내정보벌", Lang.KO));
        interestTag = tagRepository.saveAndFlush(Tag.memberTag(TagGroup.INTEREST, "푹자기테스트", Lang.KO));
    }

    @Test
    @DisplayName("닉네임을 바꾸면 전 이름이 닉네임 백업에 남고, 출생연도 · 태그도 바뀐다")
    void update() {
        // when
        MemberResponse me = memberService.updateMe(member.getId(),
                new MemberUpdateRequest("새이름벌", 1990, List.of(interestTag.getId())), NOW);

        // then
        assertThat(me.nickname()).isEqualTo("새이름벌");
        assertThat(me.birthYear()).isEqualTo(1990);
        assertThat(me.tags()).extracting(MemberResponse.TagItem::name).containsExactly("푹자기테스트");
        assertThat(nicknameHistoryRepository.findByMemberIdOrderByCreatedAtDesc(member.getId()))
                .extracting(h -> h.getNickname()).containsExactly("내정보벌");
    }

    @Test
    @DisplayName("닉네임을 비우면 그대로 두고, 출생연도를 비우면 지운다")
    void keepNicknameClearBirthYear() {
        // given
        memberService.updateMe(member.getId(), new MemberUpdateRequest(null, 1990, List.of()), NOW);

        // when
        MemberResponse me = memberService.updateMe(member.getId(), new MemberUpdateRequest("", null, List.of()), NOW);

        // then
        assertThat(me.nickname()).isEqualTo("내정보벌");
        assertThat(me.birthYear()).isNull();
    }

    @Test
    @DisplayName("다른 회원이 쓰는 닉네임이면 409 DUPLICATE")
    void duplicateNickname() {
        memberRepository.saveAndFlush(Member.join(Provider.GOOGLE, "g-me2", "있는이름", Lang.KO));

        assertThatThrownBy(() -> memberService.updateMe(member.getId(),
                new MemberUpdateRequest("있는이름", null, List.of()), NOW))
                .extracting(e -> ((BusinessException) e).getResultCode()).isEqualTo(ResultCode.DUPLICATE);
    }

    @Test
    @DisplayName("만 14세 미만이 되는 출생연도는 400 VALIDATION")
    void underFourteen() {
        assertThatThrownBy(() -> memberService.updateMe(member.getId(),
                new MemberUpdateRequest(null, 2020, List.of()), NOW))
                .extracting(e -> ((BusinessException) e).getResultCode()).isEqualTo(ResultCode.VALIDATION);
    }

    @Test
    @DisplayName("#상황 태그는 내 정보 태그로 고를 수 없다 — 400 VALIDATION (건강 · 마음 상태를 회원 정보에 두지 않는다)")
    void situationTagNotAllowed() {
        Tag situation = tagRepository.saveAndFlush(Tag.create("상황태그테스트", Lang.KO));

        assertThatThrownBy(() -> memberService.updateMe(member.getId(),
                new MemberUpdateRequest(null, null, List.of(situation.getId())), NOW))
                .extracting(e -> ((BusinessException) e).getResultCode()).isEqualTo(ResultCode.VALIDATION);
    }

    @Test
    @DisplayName("탈퇴하면 개인정보를 비우고 연결된 기록을 지우며, 앱 키는 연결만 끊는다 · 같은 계정으로 다시 가입할 수 있다")
    void withdraw() {
        // given
        memberService.updateMe(member.getId(), new MemberUpdateRequest("바꾼이름", 1990, List.of(interestTag.getId())), NOW);
        termsService.findCurrentRequired(NOW).forEach(t -> termsHistoryRepository.save(MemberTermsHistory.agree(member, t)));
        cartRepository.save(Cart.put(member, CartTargetType.ACTIVITY, 1L, null));
        feedbackRepository.save(Feedback.send(member, FeedbackType.FEATURE, "의견입니다", Lang.KO));
        MemberAppKey appKey = MemberAppKey.issue("app-key-withdraw", "hash");
        appKey.link(member, NOW);
        appKeyRepository.save(appKey);
        em.flush();

        // when
        memberService.withdraw(member.getId(), NOW);
        em.flush();
        em.clear();

        // then
        Member gone = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(gone.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        assertThat(gone.getProviderUid()).isNull();
        assertThat(gone.getNickname()).isNull();
        assertThat(gone.getBirthYear()).isNull();
        assertThat(gone.getTags()).isEmpty();
        assertThat(nicknameHistoryRepository.findByMemberIdOrderByCreatedAtDesc(member.getId())).isEmpty();
        assertThat(termsHistoryRepository.findTermsIdsByMemberId(member.getId())).isEmpty();
        assertThat(cartRepository.findByMemberIdAndSentAtIsNullOrderByIdAsc(member.getId())).isEmpty();
        assertThat(feedbackRepository.findByHandledAtIsNullOrderByCreatedAtAsc())
                .noneMatch(f -> f.getFeedbackText().equals("의견입니다"));
        assertThat(appKeyRepository.findByAppKey("app-key-withdraw")).get()
                .satisfies(k -> assertThat(k.getMember()).isNull());

        Member again = memberRepository.saveAndFlush(Member.join(Provider.GOOGLE, "g-me", "내정보벌", Lang.KO));
        assertThat(again.getId()).isNotEqualTo(member.getId());
    }
}
