package com.honeysaltlove.domain.member.repository;

import com.honeysaltlove.domain.member.entity.MemberTermsHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

/**
 * 약관 동의 기록 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 * 2026-11-10        shinm              회원이 동의한 약관 번호 찾기
 */
public interface MemberTermsHistoryRepository extends JpaRepository<MemberTermsHistory, Long> {

    @Query("select h.terms.id from MemberTermsHistory h where h.member.id = :memberId")
    Set<Long> findTermsIdsByMemberId(@Param("memberId") Long memberId);   // 동의한 약관 번호 — 다시 동의할지 확인
}
