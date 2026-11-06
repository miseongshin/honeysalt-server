package com.honeysaltlove.domain.feedback.repository;

import com.honeysaltlove.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 의견 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByHandledAtIsNullOrderByCreatedAtAsc();   // 미처리 의견 목록 (운영자)
}
