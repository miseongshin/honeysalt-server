package com.honeysaltlove.global.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 공개 여부와 숨김 정보를 묶는다 — 고민 · 생각꿀팁 · 활동 · 스케줄에 끼워 넣는다(@Embedded)
 * 칸은 각 테이블에 그대로 있다 (status · hidden_at · hidden_reason) · 숨김 규칙은 이곳에서만 관리한다
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Visibility {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "hidden_at")
    private Instant hiddenAt;

    @Column(name = "hidden_reason", length = 200)
    private String hiddenReason;               // 숨긴 사람과 사유 (예: AI: 광고 / 관리자: 욕설)

    private Visibility(Status status) {
        this.status = status;
    }

    /**
     * 새 글을 바로 공개한다
     * @author shinm
     * @since 2026-11-04
     */
    public static Visibility visible() {
        return new Visibility(Status.PUBLIC);
    }

    /**
     * 숨김 처리한다. 숨긴 사람과 사유를 남긴다
     * @author shinm
     * @since 2026-11-04
     */
    public void hide(String reason, Instant now) {
        this.status = Status.HIDDEN;
        this.hiddenAt = now;
        this.hiddenReason = reason;
    }

    /**
     * 다시 공개한다
     * @author shinm
     * @since 2026-11-04
     */
    public void show() {
        this.status = Status.PUBLIC;
        this.hiddenAt = null;
        this.hiddenReason = null;
    }

    public boolean isHidden() {
        return status == Status.HIDDEN;
    }
}
