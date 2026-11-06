package com.honeysaltlove.domain.reaction.repository;

import com.honeysaltlove.domain.reaction.entity.HoneyComment;
import com.honeysaltlove.global.common.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 댓글 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public interface HoneyCommentRepository extends JpaRepository<HoneyComment, Long> {

    List<HoneyComment> findByActivityIdAndVisibilityStatusOrderByCreatedAtAsc(Long activityId, Status status);   // 활동 상세 · 인덱스: ix_honey_comment_activity

    List<HoneyComment> findByScheduleIdAndVisibilityStatusOrderByCreatedAtAsc(Long scheduleId, Status status);   // 스케줄 상세 · 인덱스: ix_honey_comment_schedule
}
