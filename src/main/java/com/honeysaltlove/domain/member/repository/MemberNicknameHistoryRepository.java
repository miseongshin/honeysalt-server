package com.honeysaltlove.domain.member.repository;

import com.honeysaltlove.domain.member.entity.MemberNicknameHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
 * 2026-11-10        shinm              탈퇴하면 닉네임 백업 삭제
 */
public interface MemberNicknameHistoryRepository extends JpaRepository<MemberNicknameHistory, Long> {

    List<MemberNicknameHistory> findByMemberIdOrderByCreatedAtDesc(Long memberId);   // 인덱스: (member_id, created_at)

    @Modifying
    @Query("delete from MemberNicknameHistory h where h.member.id = :memberId")
    int deleteByMemberId(@Param("memberId") Long memberId);   // 탈퇴하면 바로 삭제
}
