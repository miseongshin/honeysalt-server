package com.honeysaltlove.domain.app.entity;

import com.honeysaltlove.global.common.BaseCreatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 도움돼요 로그. 앱에서 켜고 끈 것을 둘 다 쌓는다 — 로그라 지우지 않는다
 * <pre>
 * - helpful true 켬(+1) · false 끔(-1) · 활동별로 더하면 지금 도움돼요 수
 * - 같은 상태가 두 번 오면 넣지 않는다 — 서비스에서 그 앱 · 그 활동의 마지막 행을 보고
 * - 행이 없으면 끈 상태로 보고 끔은 넣지 않는다 — 첫 행은 늘 켬이라 합이 0 밑으로 내려가지 않는다
 * - 스케줄러가 1시간마다 활동별로 더해 활동의 helped_count 를 맞춘다
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
public class AppHelpedLog extends BaseCreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "helped_id")
    private Long id;

    @Column(name = "app_key_id", nullable = false)
    private Long appKeyId;                     // 보낸 앱 · FK 없음

    @Column(name = "activity_id", nullable = false)
    private Long activityId;                   // FK 없음

    @Column(nullable = false)
    private boolean helpful;                   // true 켬 · false 끔

    /**
     * 도움돼요를 켜거나 끈 것을 남긴다
     * @author shinm
     * @since 2026-11-06
     */
    public static AppHelpedLog record(Long appKeyId, Long activityId, boolean helpful) {
        AppHelpedLog h = new AppHelpedLog();
        h.appKeyId = appKeyId;
        h.activityId = activityId;
        h.helpful = helpful;
        return h;
    }
}
