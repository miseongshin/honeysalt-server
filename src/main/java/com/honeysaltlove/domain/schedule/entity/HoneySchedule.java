package com.honeysaltlove.domain.schedule.entity;

import com.honeysaltlove.domain.activity.entity.HoneyActivity;
import com.honeysaltlove.domain.member.entity.Member;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * 꿀방울 스케줄. 여러 활동을 묶으며 고민과는 연결하지 않는다
 * 설명은 한국어 · 중국어 · 일본어는 200자, 그 외 언어는 400자까지 작성할 수 있다 (DB 컬럼은 최대 500자)
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
public class HoneySchedule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "schedule_title", nullable = false, length = 50)
    private String scheduleTitle;

    @Column(name = "schedule_text", length = 500)
    private String scheduleText;               // 선택

    @Column(nullable = false, length = 20)
    private Lang lang;

    @Column(name = "show_tags", nullable = false)
    private boolean showTags;

    @Embedded
    private Visibility visibility = Visibility.visible();

    @Column(name = "like_count", nullable = false)
    private int likeCount;

    @Column(name = "send_count", nullable = false)
    private int sendCount;                     // 앱에서 스케줄을 가져간 횟수 · 가져갈 때(ack)마다 1씩 증가한다

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HoneyScheduleActivity> items = new ArrayList<>();

    /**
     * 스케줄을 등록한다
     * @author shinm
     * @since 2026-11-04
     */
    public static HoneySchedule write(Member member, String scheduleTitle, String scheduleText, Lang lang, boolean showTags) {
        HoneySchedule s = new HoneySchedule();
        s.member = member;
        s.scheduleTitle = scheduleTitle;
        s.scheduleText = scheduleText;
        s.lang = lang;
        s.showTags = showTags;
        return s;
    }

    /**
     * 활동을 추가한다. 같은 활동은 스케줄에 한 번만 (요일은 days 로)
     * @author shinm
     * @since 2026-11-04
     */
    public void addActivity(HoneyActivity activity, String days) {
        boolean exists = items.stream().anyMatch(i -> i.getActivity() == activity);
        if (exists) {
            throw new IllegalStateException("이미 넣은 활동이다");
        }
        items.add(HoneyScheduleActivity.of(this, activity, days));
    }

    public void hide(String reason, Instant now) {
        visibility.hide(reason, now);
    }

    public void show() {
        visibility.show();
    }
}
