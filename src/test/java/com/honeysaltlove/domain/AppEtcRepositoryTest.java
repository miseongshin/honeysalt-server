package com.honeysaltlove.domain;

import com.honeysaltlove.domain.app.entity.AppDoneLog;
import com.honeysaltlove.domain.app.entity.AppHelpedLog;
import com.honeysaltlove.domain.app.entity.MemberAppKey;
import com.honeysaltlove.domain.app.repository.AppDoneLogRepository;
import com.honeysaltlove.domain.app.repository.AppHelpedLogRepository;
import com.honeysaltlove.domain.app.repository.MemberAppKeyRepository;
import com.honeysaltlove.domain.cart.entity.Cart;
import com.honeysaltlove.domain.cart.entity.CartTargetType;
import com.honeysaltlove.domain.cart.repository.CartRepository;
import com.honeysaltlove.domain.feedback.entity.Feedback;
import com.honeysaltlove.domain.feedback.entity.FeedbackType;
import com.honeysaltlove.domain.feedback.repository.FeedbackRepository;
import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.entity.Provider;
import com.honeysaltlove.domain.member.repository.MemberRepository;
import com.honeysaltlove.global.common.Lang;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 앱 키 · 앱 로그 · 활동바구니 · 의견 테이블의 제약 · 저장 확인 — love_local(터널)에 붙어 돌고, 테스트가 끝나면 롤백한다(@Transactional)
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
@SpringBootTest
@Transactional
class AppEtcRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired MemberAppKeyRepository appKeyRepository;
    @Autowired AppDoneLogRepository doneLogRepository;
    @Autowired AppHelpedLogRepository helpedLogRepository;
    @Autowired CartRepository cartRepository;
    @Autowired FeedbackRepository feedbackRepository;
    @Autowired EntityManager em;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.join(Provider.GOOGLE, "g-app", "앱테스트", Lang.KO));
    }

    @Test
    @DisplayName("앱 키는 겹칠 수 없다")
    void appKeyUnique() {
        // given
        appKeyRepository.saveAndFlush(MemberAppKey.issue("test-app-key-1", "hash-1"));

        // when · then
        assertThatThrownBy(() -> appKeyRepository.saveAndFlush(MemberAppKey.issue("test-app-key-1", "hash-2")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("앱 키를 웹 계정과 연결하면 회원으로 찾을 수 있다")
    void appKeyLink() {
        // given
        MemberAppKey key = appKeyRepository.save(MemberAppKey.issue("test-app-key-2", "hash"));

        // when
        key.link(member, Instant.now());
        em.flush();
        em.clear();

        // then
        assertThat(appKeyRepository.findByMemberId(member.getId()))
                .extracting(MemberAppKey::getAppKey).containsExactly("test-app-key-2");
    }

    @Test
    @DisplayName("활동완료는 같은 앱 · 같은 활동 · 같은 날 한 번만 남는다")
    void doneOncePerDay() {
        // given
        LocalDate today = LocalDate.of(2026, 11, 6);
        doneLogRepository.saveAndFlush(AppDoneLog.record(1L, 10L, today));

        // when · then
        assertThat(doneLogRepository.existsByActivityIdAndAppKeyIdAndDoneOn(10L, 1L, today)).isTrue();
        assertThatThrownBy(() -> doneLogRepository.saveAndFlush(AppDoneLog.record(1L, 10L, today)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("도움돼요는 켜기 · 끄기를 둘 다 쌓고, 마지막 상태를 찾을 수 있다")
    void helpedLastState() {
        // given
        helpedLogRepository.save(AppHelpedLog.record(1L, 10L, true));
        helpedLogRepository.save(AppHelpedLog.record(1L, 10L, false));
        helpedLogRepository.save(AppHelpedLog.record(2L, 10L, true));   // 다른 앱
        em.flush();

        // when
        AppHelpedLog last = helpedLogRepository.findFirstByAppKeyIdAndActivityIdOrderByIdDesc(1L, 10L).orElseThrow();

        // then
        assertThat(last.isHelpful()).isFalse();
    }

    @Test
    @DisplayName("앱은 보낸 것만 받고, 받았다고 알린 번호만 지운다")
    void cartTakeAndAck() {
        // given
        Cart sent1 = cartRepository.save(Cart.put(member, CartTargetType.SCHEDULE, 7L, null));
        Cart sent2 = cartRepository.save(Cart.put(member, CartTargetType.ACTIVITY, 15L, "MON,WED"));
        Cart inBasket = cartRepository.save(Cart.put(member, CartTargetType.THOUGHT_TIP, 3L, null));
        sent1.markSent(Instant.now());
        sent2.markSent(Instant.now());
        em.flush();

        // when
        List<Cart> takes = cartRepository.findByMemberIdAndSentAtIsNotNullOrderByIdAsc(member.getId());
        int deleted = cartRepository.deleteTaken(member.getId(), List.of(sent1.getId(), inBasket.getId()));

        // then
        assertThat(takes).extracting(Cart::getId).containsExactly(sent1.getId(), sent2.getId());
        assertThat(deleted).isEqualTo(1);   // 바구니에만 있는 것은 지우지 않는다
        assertThat(cartRepository.findAllById(List.of(sent1.getId(), sent2.getId(), inBasket.getId())))
                .extracting(Cart::getId).containsExactlyInAnyOrder(sent2.getId(), inBasket.getId());
    }

    @Test
    @DisplayName("남의 cart 번호를 알려도 지워지지 않는다")
    void cartAckOtherMember() {
        // given
        Member other = memberRepository.save(Member.join(Provider.GOOGLE, "g-app2", "앱테스트2", Lang.KO));
        Cart othersCart = cartRepository.save(Cart.put(other, CartTargetType.SCHEDULE, 7L, null));
        othersCart.markSent(Instant.now());
        em.flush();

        // when
        int deleted = cartRepository.deleteTaken(member.getId(), List.of(othersCart.getId()));

        // then
        assertThat(deleted).isZero();
    }

    @Test
    @DisplayName("같은 것은 바구니에 한 번만 담긴다")
    void cartOnce() {
        // given
        cartRepository.saveAndFlush(Cart.put(member, CartTargetType.ACTIVITY, 15L, null));

        // when · then
        assertThatThrownBy(() -> cartRepository.saveAndFlush(Cart.put(member, CartTargetType.ACTIVITY, 15L, "FRI")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("운영자가 확인한 의견은 미처리 목록에서 빠진다")
    void feedbackHandled() {
        // given
        Feedback done = feedbackRepository.save(Feedback.send(member, FeedbackType.FEATURE, "다크 모드가 있으면 좋겠어요", Lang.KO));
        feedbackRepository.save(Feedback.send(member, FeedbackType.INCONVENIENCE, "글씨가 작아요", Lang.KO));

        // when
        done.handle(Instant.now());
        em.flush();

        // then
        assertThat(feedbackRepository.findByHandledAtIsNullOrderByCreatedAtAsc())
                .extracting(Feedback::getFeedbackText).contains("글씨가 작아요").doesNotContain("다크 모드가 있으면 좋겠어요");
    }
}
