package com.honeysaltlove.domain;

import com.honeysaltlove.domain.activity.entity.ActivitySource;
import com.honeysaltlove.domain.activity.entity.HoneyActivity;
import com.honeysaltlove.domain.activity.entity.Mission;
import com.honeysaltlove.domain.activity.repository.HoneyActivityRepository;
import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.entity.Provider;
import com.honeysaltlove.domain.member.repository.MemberRepository;
import com.honeysaltlove.domain.schedule.entity.HoneySchedule;
import com.honeysaltlove.domain.schedule.repository.HoneyScheduleRepository;
import com.honeysaltlove.domain.tip.entity.HoneyActivityTip;
import com.honeysaltlove.domain.tip.entity.HoneyThoughtTip;
import com.honeysaltlove.domain.tip.repository.HoneyActivityTipRepository;
import com.honeysaltlove.domain.tip.repository.HoneyThoughtTipRepository;
import com.honeysaltlove.domain.worry.entity.HoneyWorry;
import com.honeysaltlove.domain.worry.repository.HoneyWorryRepository;
import com.honeysaltlove.global.common.Lang;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 꿀방울 주요기능 테이블의 제약 · 저장 확인 — love_local(터널)에 붙어 돌고, 끝나면 되돌린다(@Transactional)
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
class HoneyMainRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired HoneyWorryRepository worryRepository;
    @Autowired HoneyThoughtTipRepository thoughtTipRepository;
    @Autowired HoneyActivityRepository activityRepository;
    @Autowired HoneyActivityTipRepository activityTipRepository;
    @Autowired HoneyScheduleRepository scheduleRepository;
    @Autowired EntityManager em;

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.saveAndFlush(Member.join(Provider.GOOGLE, "g-main", "주요기능", Lang.KO));
    }

    @Test
    @DisplayName("미션은 번호로 저장되고 다시 읽으면 같은 미션이다")
    void missionNo() {
        // given
        HoneyActivity saved = activityRepository.saveAndFlush(HoneyActivity.defaultActivity(Mission.CLEAN, "room"));
        em.clear();

        // when
        Integer no = (Integer) em.createNativeQuery("select mission_no from honey_activity where activity_id = :id")
                .setParameter("id", saved.getId()).getSingleResult();
        HoneyActivity found = activityRepository.findById(saved.getId()).orElseThrow();

        // then
        assertThat(no).isEqualTo(2);
        assertThat(found.getMission()).isEqualTo(Mission.CLEAN);
    }

    @Test
    @DisplayName("같은 미션에 속한 활동은 이름이 중복될 수 없다")
    void sameNameInMission() {
        // given
        activityRepository.saveAndFlush(HoneyActivity.create(member, Mission.MOVE, "베란다 걷기", Lang.KO, null, ActivitySource.WEB, null));

        // when · then
        assertThatThrownBy(() -> activityRepository.saveAndFlush(
                HoneyActivity.create(member, Mission.MOVE, "베란다 걷기", Lang.KO, null, ActivitySource.WEB, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("같은 고민에 한 사람이 같은 활동을 중복 추천할 수 없다")
    void sameActivityTipTwice() {
        // given
        HoneyWorry worry = worryRepository.saveAndFlush(HoneyWorry.write(member, "잠이 안 올 때", Lang.KO, true, false));
        HoneyActivity activity = activityRepository.saveAndFlush(HoneyActivity.defaultActivity(Mission.MOVE, "bridge"));
        activityTipRepository.saveAndFlush(HoneyActivityTip.recommend(worry, activity, member));

        // when · then
        assertThatThrownBy(() -> activityTipRepository.saveAndFlush(HoneyActivityTip.recommend(worry, activity, member)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("생각꿀팁 링크를 바꾸면 기존 링크를 삭제하고 새 링크를 지정된 순서로 저장한다")
    void replaceUrls() {
        // given
        HoneyWorry worry = worryRepository.saveAndFlush(HoneyWorry.write(member, "잠이 안 올 때", Lang.KO, true, false));
        HoneyThoughtTip tip = HoneyThoughtTip.write(member, worry, "일어나서 물 한 잔", Lang.KO, false);
        tip.replaceUrls(List.of("https://a", "https://b", "https://c"));
        thoughtTipRepository.saveAndFlush(tip);

        // when
        tip.replaceUrls(List.of("https://x", "https://y"));
        thoughtTipRepository.saveAndFlush(tip);
        em.clear();

        // then
        HoneyThoughtTip found = thoughtTipRepository.findById(tip.getId()).orElseThrow();
        assertThat(found.getUrls()).extracting(u -> u.getUrl()).containsExactly("https://x", "https://y");
    }

    @Test
    @DisplayName("스케줄에 같은 활동을 두 번 넣을 수 없다")
    void sameActivityInSchedule() {
        // given
        HoneyActivity activity = activityRepository.saveAndFlush(HoneyActivity.defaultActivity(Mission.MOVE, "squat"));
        HoneySchedule schedule = HoneySchedule.write(member, "아침 루틴", null, Lang.KO, false);
        schedule.addActivity(activity, "0,3");
        scheduleRepository.saveAndFlush(schedule);

        // when · then
        assertThatThrownBy(() -> schedule.addActivity(activity, "1"))
                .isInstanceOf(IllegalStateException.class);
        assertThat(schedule.getItems()).hasSize(1);
    }
}
