package com.honeysaltlove.domain.tip.entity;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.worry.entity.HoneyWorry;
import com.honeysaltlove.global.common.BaseTimeEntity;
import com.honeysaltlove.global.common.Lang;
import com.honeysaltlove.global.common.Visibility;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 생각꿀팁. 고민에 답한다 — 고민에 연결된 글 (worry 는 꼭 있다)
 * <pre>
 * - 글은 한국어 · 중국어 · 일본어는 200자, 그 외 언어는 400자까지 작성할 수 있다 (DB 컬럼은 최대 500자)
 * - 좋아요를 받은 뒤에는 수정할 수 없고 숨김 처리만 가능하다 — 확인은 서비스에서 likes 로
 * </pre>
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
public class HoneyThoughtTip extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thought_tip_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "worry_id", nullable = false)
    private HoneyWorry worry;                  // 답한 고민

    @Column(name = "thought_tip_text", nullable = false, length = 500)
    private String thoughtTipText;

    @Column(nullable = false, length = 20)
    private Lang lang;

    @Column(name = "show_tags", nullable = false)
    private boolean showTags;

    @Embedded
    private Visibility visibility = Visibility.visible();

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @OneToMany(mappedBy = "thoughtTip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sort")
    private List<HoneyThoughtTipUrl> urls = new ArrayList<>();

    /**
     * 생각꿀팁을 등록한다
     * @author shinm
     * @since 2026-11-04
     */
    public static HoneyThoughtTip write(Member member, HoneyWorry worry, String thoughtTipText, Lang lang, boolean showTags) {
        HoneyThoughtTip t = new HoneyThoughtTip();
        t.member = member;
        t.worry = worry;
        t.thoughtTipText = thoughtTipText;
        t.lang = lang;
        t.showTags = showTags;
        return t;
    }

    /**
     * 링크를 통째로 바꾼다 — 기존 링크를 삭제하고 새 링크를 등록한다(orphanRemoval)
     * @author shinm
     * @since 2026-11-04
     */
    public void replaceUrls(List<String> newUrls) {
        urls.clear();
        for (int i = 0; i < newUrls.size(); i++) {
            urls.add(HoneyThoughtTipUrl.of(this, i, newUrls.get(i)));
        }
    }

    public void hide(String reason, Instant now) {
        visibility.hide(reason, now);
    }

    public void show() {
        visibility.show();
    }
}
