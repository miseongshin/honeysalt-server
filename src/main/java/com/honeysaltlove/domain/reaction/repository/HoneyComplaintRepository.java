package com.honeysaltlove.domain.reaction.repository;

import com.honeysaltlove.domain.reaction.entity.HoneyComplaint;
import com.honeysaltlove.domain.reaction.entity.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 신고 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public interface HoneyComplaintRepository extends JpaRepository<HoneyComplaint, Long> {

    boolean existsByMemberIdAndTargetTypeAndTargetId(Long memberId, TargetType targetType, Long targetId);   // 이미 신고했는지

    long countByTargetTypeAndTargetId(TargetType targetType, Long targetId);   // 세 번째 신고면 관리자 이메일 · 인덱스: ix_honey_complaint_target

    List<HoneyComplaint> findByHandledAtIsNullOrderByCreatedAtAsc();         // 미처리 신고 목록(운영자)
}
