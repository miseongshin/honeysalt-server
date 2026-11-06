package com.honeysaltlove.domain.activity.entity;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.tag.entity.Tag;
import com.honeysaltlove.global.common.BaseTimeEntity;
import com.honeysaltlove.global.common.Lang;
import com.honeysaltlove.global.common.Visibility;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 활동. 기본 활동 · 회원 등록 활동 · 앱 공유 활동
 * <pre>
 * - 기본 활동은 이름 없이 `activity_key`만 둔다 (이름은 Vue 번역 파일)
 * - 세부 분류(상체 · 하체 등)는 별도 칸 대신 태그로 관리한다
 * - 좋아요를 받은 뒤에는 수정할 수 없고 숨김 처리만 가능하다 — 확인은 서비스에서 likes 로
 * </pre>
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 * 2026-11-06        shinm              태그 연결 (honey_activity_tag)
 * 2026-11-06        shinm              도움돼요 수 (helped_count)
 */
@Getter
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_honey_activity_mission_name", columnNames = {"mission_no", "name"}),
        @UniqueConstraint(name = "uk_honey_activity_app_local", columnNames = {"created_by", "app_local_key"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HoneyActivity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long id;

    @Column(name = "mission_no")
    private Mission mission;                   // 번호로 저장 (MissionConverter) · 미션 없는 습관이면 null

    @Column(name = "activity_key", length = 100)
    private String activityKey;                // 기본 활동을 구분하는 키 · 앱의 활동 id 와 같은 값

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(length = 14)
    private String name;                       // 사용자가 만든 활동만

    @Column(length = 20)
    private Lang lang;                         // 사용자가 만든 활동만

    @Column(columnDefinition = "text")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Member createdBy;                  // 기본 활동은 null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActivitySource source;

    @Column(name = "app_local_key", length = 100)
    private String appLocalKey;                // 앱에서 만든 활동 번호 · 같은 활동을 중복 등록하지 않는다

    @Embedded
    private Visibility visibility = Visibility.visible();

    @Column(name = "use_count", nullable = false)
    private int useCount;                      // 사용자 수 (해 본 앱 사용자 · 회원이 아니어도 센다) · 스케줄러가 1시간마다 집계

    @Column(name = "done_count", nullable = false)
    private int doneCount;                     // 완료 횟수 · 스케줄러가 1시간마다 집계

    @Column(name = "helped_count", nullable = false)
    private int helpedCount;                   // 도움돼요 수 · 스케줄러가 1시간마다 app_helped_log 를 더해(+1 / -1) 맞춘다

    @Column(name = "like_count", nullable = false)
    private int likeCount;                     // 좋아요 수 · 스케줄러가 1분마다 집계

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sort")
    private List<HoneyActivityUrl> urls = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "honey_activity_tag",
            joinColumns = @JoinColumn(name = "activity_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();   // #상황 태그 · 세부 분류(상체 · 하체 …)도 태그로

    /**
     * 기본 활동 만들기 (이름은 Vue 번역 파일에서 activityKey 로)
     * @author shinm
     * @since 2026-11-04
     */
    public static HoneyActivity defaultActivity(Mission mission, String activityKey) {
        HoneyActivity a = new HoneyActivity();
        a.mission = mission;
        a.activityKey = activityKey;
        a.isDefault = true;
        a.source = ActivitySource.DEFAULT;
        return a;
    }

    /**
     * 회원 활동 만들기 (웹에서 등록 · 앱에서 공유)
     * @author shinm
     * @since 2026-11-04
     */
    public static HoneyActivity create(Member createdBy, Mission mission, String name, Lang lang, String description,
                                       ActivitySource source, String appLocalKey) {
        HoneyActivity a = new HoneyActivity();
        a.createdBy = createdBy;
        a.mission = mission;
        a.name = name;
        a.lang = lang;
        a.description = description;
        a.source = source;
        a.appLocalKey = appLocalKey;
        return a;
    }

    /**
     * 링크를 통째로 바꾼다 — 기존 링크를 삭제하고 새 링크를 등록한다(orphanRemoval)
     * @author shinm
     * @since 2026-11-04
     */
    public void replaceUrls(List<String> newUrls) {
        urls.clear();
        for (int i = 0; i < newUrls.size(); i++) {
            urls.add(HoneyActivityUrl.of(this, i, newUrls.get(i)));
        }
    }

    /**
     * 태그를 새로 고른 것으로 바꾼다 (빠진 태그의 연결 행은 지우고, 새 태그는 넣는다)
     * @author shinm
     * @since 2026-11-06
     */
    public void replaceTags(Set<Tag> newTags) {
        tags.retainAll(newTags);   // clear() 를 쓰면 자기 tags 를 넘겼을 때 모두 사라진다
        tags.addAll(newTags);
    }

    public void hide(String reason, Instant now) {
        visibility.hide(reason, now);
    }

    public void show() {
        visibility.show();
    }
}
