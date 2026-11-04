package com.honeysaltlove.domain.tip.entity;

import com.honeysaltlove.global.common.BaseCreatedEntity;
import jakarta.persistence.Column;
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

/**
 * 생각꿀팁 참고 링크. 등록·삭제할 수 있다(HoneyThoughtTip.replaceUrls)
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
public class HoneyThoughtTipUrl extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thought_tip_url_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "thought_tip_id", nullable = false)
    private HoneyThoughtTip thoughtTip;

    @Column(nullable = false)
    private int sort;                          // 화면 순서 · 0부터

    @Column(nullable = false, length = 500)
    private String url;                        // 유튜브면 화면에서 바로 재생

    static HoneyThoughtTipUrl of(HoneyThoughtTip thoughtTip, int sort, String url) {
        HoneyThoughtTipUrl u = new HoneyThoughtTipUrl();
        u.thoughtTip = thoughtTip;
        u.sort = sort;
        u.url = url;
        return u;
    }
}
