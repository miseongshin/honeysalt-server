package com.honeysaltlove.domain.reaction.entity;

import com.honeysaltlove.domain.activity.entity.HoneyActivity;
import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.schedule.entity.HoneySchedule;
import com.honeysaltlove.global.common.BaseTimeEntity;
import com.honeysaltlove.global.common.Lang;
import com.honeysaltlove.global.common.Visibility;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 댓글. 활동 · 꿀방울 스케줄 상세에 단다 (둘 중 하나만)
 * <pre>
 * - 수정할 수 있다 · 좋아요가 하나라도 있으면 수정할 수 없다 (서비스에서 honey_like 로 확인)
 * - 지우지 않고 숨긴다 (글과 같은 방식 — 작성자가 지우면 hide("본인 삭제")) · 활동 · 스케줄을 숨기면 조회할 때 같이 안 보인다
 * - 글은 한국어 · 중국어 · 일본어는 200자, 그 외 언어는 400자까지 작성할 수 있다 (DB 컬럼은 최대 500자 — 검증은 서비스에서) · 언어는 lang 칸으로 판단 · 길이는 length() 로 센다
 * </pre>
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HoneyComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id")
    private HoneyActivity activity;            // 활동 댓글이면 채움 · schedule 과 둘 중 하나만

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private HoneySchedule schedule;            // 스케줄 댓글이면 채움

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "comment_text", nullable = false, length = 500)
    private String commentText;

    @Column(nullable = false, length = 20)
    private Lang lang;

    @Embedded
    private Visibility visibility = Visibility.visible();

    @Column(name = "like_count", nullable = false)
    private int likeCount;                     // 좋아요 수 · 스케줄러가 다시 세어 맞춘다

    /**
     * 활동에 댓글을 단다
     * @author shinm
     * @since 2026-11-06
     */
    public static HoneyComment onActivity(Member member, HoneyActivity activity, String commentText, Lang lang) {
        HoneyComment c = of(member, commentText, lang);
        c.activity = activity;
        return c;
    }

    /**
     * 꿀방울 스케줄에 댓글을 단다
     * @author shinm
     * @since 2026-11-06
     */
    public static HoneyComment onSchedule(Member member, HoneySchedule schedule, String commentText, Lang lang) {
        HoneyComment c = of(member, commentText, lang);
        c.schedule = schedule;
        return c;
    }

    private static HoneyComment of(Member member, String commentText, Lang lang) {
        HoneyComment c = new HoneyComment();
        c.member = member;
        c.commentText = commentText;
        c.lang = lang;
        return c;
    }

    /**
     * 댓글 내용을 고친다
     * @author shinm
     * @since 2026-11-06
     */
    public void edit(String commentText) {
        this.commentText = commentText;
    }

    public void hide(String reason, Instant now) {
        visibility.hide(reason, now);
    }

    public void show() {
        visibility.show();
    }
}
