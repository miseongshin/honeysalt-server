package com.honeysaltlove.domain.reaction.repository;

import com.honeysaltlove.domain.activity.entity.HoneyActivity;
import com.honeysaltlove.domain.activity.entity.Mission;
import com.honeysaltlove.domain.activity.repository.HoneyActivityRepository;
import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.entity.Provider;
import com.honeysaltlove.domain.member.repository.MemberRepository;
import com.honeysaltlove.domain.reaction.entity.ComplaintReason;
import com.honeysaltlove.domain.reaction.entity.HoneyComment;
import com.honeysaltlove.domain.reaction.entity.HoneyComplaint;
import com.honeysaltlove.domain.reaction.entity.HoneyLike;
import com.honeysaltlove.domain.reaction.entity.TargetType;
import com.honeysaltlove.domain.schedule.entity.HoneySchedule;
import com.honeysaltlove.domain.schedule.repository.HoneyScheduleRepository;
import com.honeysaltlove.global.common.Lang;
import com.honeysaltlove.global.common.Status;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 댓글 · 좋아요 · 신고 테이블의 제약 · 저장 확인 — love_local(터널)에 붙어 돌고, 테스트가 끝나면 롤백한다(@Transactional)
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
class ReactionRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired HoneyActivityRepository activityRepository;
    @Autowired HoneyScheduleRepository scheduleRepository;
    @Autowired HoneyCommentRepository commentRepository;
    @Autowired HoneyLikeRepository likeRepository;
    @Autowired HoneyComplaintRepository complaintRepository;
    @Autowired EntityManager em;

    private Member member;
    private HoneyActivity activity;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(Member.join(Provider.GOOGLE, "g-reaction", "반응테스트", Lang.KO));
        activity = activityRepository.save(HoneyActivity.defaultActivity(Mission.MOVE, "test-reaction-walk"));
    }

    @Test
    @DisplayName("댓글은 고칠 수 있고, 숨긴 댓글은 목록에 나오지 않는다")
    void commentEditAndHide() {
        // given
        HoneyComment kept = commentRepository.save(HoneyComment.onActivity(member, activity, "좋아요", Lang.KO));
        HoneyComment hidden = commentRepository.save(HoneyComment.onActivity(member, activity, "지울 댓글", Lang.KO));

        // when
        kept.edit("정말 좋아요");
        hidden.hide("본인 삭제", Instant.now());
        em.flush();
        em.clear();

        // then
        assertThat(commentRepository.findByActivityIdAndVisibilityStatusOrderByCreatedAtAsc(activity.getId(), Status.PUBLIC))
                .extracting(HoneyComment::getCommentText).containsExactly("정말 좋아요");
    }

    @Test
    @DisplayName("댓글은 활동 · 스케줄 둘 중 하나만 달 수 있다 (둘 다 비면 DB 가 막는다)")
    void commentOneTarget() {
        // given
        HoneySchedule schedule = scheduleRepository.saveAndFlush(HoneySchedule.write(member, "아침 루틴", null, Lang.KO, false));
        commentRepository.saveAndFlush(HoneyComment.onSchedule(member, schedule, "스케줄 댓글", Lang.KO));

        // when · then — 엔티티로는 만들 수 없는 경우라 SQL 로 직접 넣어 본다
        assertThatThrownBy(() -> em.createNativeQuery(
                        "insert into honey_comment (member_id, comment_text, lang, status, created_at, updated_at) "
                                + "values (:m, '대상 없음', 'ko', 'PUBLIC', now(), now())")
                .setParameter("m", member.getId()).executeUpdate())
                .hasStackTraceContaining("ck_honey_comment_one_target");   // 다른 오류(오타 등)로 통과하지 않게
    }

    @Test
    @DisplayName("댓글은 활동 · 스케줄 둘 다에 달 수 없다 (둘 다 채우면 DB 가 막는다)")
    void commentBothTargets() {
        // given
        HoneySchedule schedule = scheduleRepository.saveAndFlush(HoneySchedule.write(member, "저녁 루틴", null, Lang.KO, false));

        // when · then — 엔티티로는 만들 수 없는 경우라 SQL 로 직접 넣어 본다
        assertThatThrownBy(() -> em.createNativeQuery(
                        "insert into honey_comment (activity_id, schedule_id, member_id, comment_text, lang, status, created_at, updated_at) "
                                + "values (:a, :s, :m, '대상 둘', 'ko', 'PUBLIC', now(), now())")
                .setParameter("a", activity.getId())
                .setParameter("s", schedule.getId())
                .setParameter("m", member.getId()).executeUpdate())
                .hasStackTraceContaining("ck_honey_comment_one_target");
    }

    @Test
    @DisplayName("좋아요는 한 사용자가 대상마다 한 번만 누를 수 있다")
    void likeOnce() {
        // given
        likeRepository.saveAndFlush(HoneyLike.press(member, TargetType.ACTIVITY, activity.getId()));

        // when · then
        assertThatThrownBy(() -> likeRepository.saveAndFlush(HoneyLike.press(member, TargetType.ACTIVITY, activity.getId())))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("좋아요를 취소하면 행이 지워지고, 다시 세면 수가 줄어든다")
    void likeCancel() {
        // given
        Member other = memberRepository.save(Member.join(Provider.GOOGLE, "g-reaction2", "반응테스트2", Lang.KO));
        likeRepository.save(HoneyLike.press(member, TargetType.ACTIVITY, activity.getId()));
        likeRepository.save(HoneyLike.press(other, TargetType.ACTIVITY, activity.getId()));
        em.flush();

        // when
        int deleted = likeRepository.cancel(member.getId(), TargetType.ACTIVITY, activity.getId());

        // then
        assertThat(deleted).isEqualTo(1);
        assertThat(likeRepository.countByTargetTypeAndTargetId(TargetType.ACTIVITY, activity.getId())).isEqualTo(1);
        assertThat(likeRepository.existsByMemberIdAndTargetTypeAndTargetId(member.getId(), TargetType.ACTIVITY, activity.getId())).isFalse();
    }

    @Test
    @DisplayName("댓글에도 좋아요를 누를 수 있다")
    void likeComment() {
        // given
        HoneyComment comment = commentRepository.saveAndFlush(HoneyComment.onActivity(member, activity, "해 보니 좋았어요", Lang.KO));

        // when
        likeRepository.saveAndFlush(HoneyLike.press(member, TargetType.COMMENT, comment.getId()));

        // then
        assertThat(likeRepository.existsByTargetTypeAndTargetId(TargetType.COMMENT, comment.getId())).isTrue();
    }

    @Test
    @DisplayName("같은 대상의 신고 수를 셀 수 있고, 한 사용자는 대상마다 한 번만 신고한다")
    void complaint() {
        // given
        Member second = memberRepository.save(Member.join(Provider.GOOGLE, "g-reaction3", "반응테스트3", Lang.KO));
        Member writer = memberRepository.save(Member.join(Provider.GOOGLE, "g-reaction4", "반응테스트4", Lang.KO));
        complaintRepository.save(HoneyComplaint.report(member, writer, TargetType.ACTIVITY, activity.getId(), ComplaintReason.AD, null));
        complaintRepository.save(HoneyComplaint.report(second, writer, TargetType.ACTIVITY, activity.getId(), ComplaintReason.ETC, "이상한 링크"));
        em.flush();

        // when · then
        assertThat(complaintRepository.countByTargetTypeAndTargetId(TargetType.ACTIVITY, activity.getId())).isEqualTo(2);
        assertThat(complaintRepository.findByHandledAtIsNullOrderByCreatedAtAsc()).hasSize(2);
        assertThatThrownBy(() -> complaintRepository.saveAndFlush(
                HoneyComplaint.report(member, writer, TargetType.ACTIVITY, activity.getId(), ComplaintReason.ABUSE, null)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
