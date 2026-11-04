package com.honeysaltlove.domain.worry.entity;

import com.honeysaltlove.domain.member.entity.Member;
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
 * 고민. 등록 즉시 공개하며, 삭제하지 않고 숨김(HIDDEN) 처리만 한다
 * 글은 한국어 · 중국어 · 일본어는 200자, 그 외 언어는 400자까지 작성할 수 있다 (DB 컬럼은 최대 500자 — AI 번역 여유)
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HoneyWorry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "worry_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "worry_text", nullable = false, length = 500)
    private String worryText;

    @Column(nullable = false, length = 20)
    private Lang lang;

    @Column(name = "is_anonymous", nullable = false)
    private boolean anonymous;

    @Column(name = "show_tags", nullable = false)
    private boolean showTags;                  // 글 옆에 내 태그(MBTI 등) 보이기

    @Embedded
    private Visibility visibility = Visibility.visible();

    @Column(name = "like_count", nullable = false)
    private int likeCount;                     // 좋아요 수 · 스케줄러가 1분마다 집계

    @Column(name = "answer_count", nullable = false)
    private int answerCount;                   // 0 = 답을 기다리는 고민

    /**
     * 고민을 등록한다
     * @author shinm
     * @since 2026-11-04
     */
    public static HoneyWorry write(Member member, String worryText, Lang lang, boolean anonymous, boolean showTags) {
        HoneyWorry w = new HoneyWorry();
        w.member = member;
        w.worryText = worryText;
        w.lang = lang;
        w.anonymous = anonymous;
        w.showTags = showTags;
        return w;
    }

    public void hide(String reason, Instant now) {
        visibility.hide(reason, now);
    }

    public void show() {
        visibility.show();
    }
}
