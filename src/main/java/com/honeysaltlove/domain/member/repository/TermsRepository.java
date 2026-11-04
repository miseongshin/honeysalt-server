package com.honeysaltlove.domain.member.repository;

import com.honeysaltlove.domain.member.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 약관 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
public interface TermsRepository extends JpaRepository<Terms, Long> {
}
