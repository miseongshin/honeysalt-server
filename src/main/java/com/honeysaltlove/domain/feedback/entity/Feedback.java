package com.honeysaltlove.domain.feedback.entity;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.global.common.BaseTimeEntity;
import com.honeysaltlove.global.common.Lang;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 의견 보내기. 로그인 후 남긴다
 * <pre>
 * - 의견 내용은 1,000자까지 — 요청 DTO 에서 막는다 (칸은 text)
 * - 운영자가 확인하면 처리 시각(handledAt)만 기록한다 · 비어 있으면 미처리
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
public class Feedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackType type;

    @Column(name = "feedback_text", nullable = false, columnDefinition = "text")
    private String feedbackText;

    @Column(nullable = false, length = 20)
    private Lang lang;

    @Column(name = "handled_at")
    private Instant handledAt;                 // null 이면 미처리

    /**
     * 의견을 보낸다
     * @author shinm
     * @since 2026-11-06
     */
    public static Feedback send(Member member, FeedbackType type, String feedbackText, Lang lang) {
        Feedback f = new Feedback();
        f.member = member;
        f.type = type;
        f.feedbackText = feedbackText;
        f.lang = lang;
        return f;
    }

    /**
     * 운영자가 확인했다
     * @author shinm
     * @since 2026-11-06
     */
    public void handle(Instant now) {
        this.handledAt = now;
    }
}
