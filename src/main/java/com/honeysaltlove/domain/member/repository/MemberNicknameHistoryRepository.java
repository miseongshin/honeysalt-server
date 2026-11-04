package com.honeysaltlove.domain.member.repository;

import com.honeysaltlove.domain.member.entity.MemberNicknameHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 닉네임 백업 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
public interface MemberNicknameHistoryRepository extends JpaRepository<MemberNicknameHistory, Long> {

    List<MemberNicknameHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);   // 색인 (member_id, created_at)
}
