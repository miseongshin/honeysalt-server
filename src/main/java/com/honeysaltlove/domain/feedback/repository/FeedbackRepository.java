package com.honeysaltlove.domain.feedback.repository;

import com.honeysaltlove.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
 * 2026-11-10        shinm              탈퇴하면 의견 삭제
 */
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByHandledAtIsNullOrderByCreatedAtAsc();   // 미처리 의견 목록 (운영자)

    @Modifying
    @Query("delete from Feedback f where f.member.id = :memberId")
    int deleteByMemberId(@Param("memberId") Long memberId);   // 탈퇴하면 바로 삭제
}
