package com.honeysaltlove.domain.activity.entity;

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
 * 활동 영상 · 글 링크. 수정할 때는 기존 항목을 삭제한 뒤 새로 등록한다 (HoneyActivity.replaceUrls)
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
public class HoneyActivityUrl extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_url_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private HoneyActivity activity;

    @Column(nullable = false)
    private int sort;                          // 화면 순서 · 0부터

    @Column(nullable = false, length = 500)
    private String url;

    static HoneyActivityUrl of(HoneyActivity activity, int sort, String url) {
        HoneyActivityUrl u = new HoneyActivityUrl();
        u.activity = activity;
        u.sort = sort;
        u.url = url;
        return u;
    }
}
