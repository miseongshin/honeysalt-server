package com.honeysaltlove.domain.member.repository;

import com.honeysaltlove.domain.member.entity.ServiceType;
import com.honeysaltlove.domain.member.entity.Terms;
import com.honeysaltlove.global.common.Lang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 약관 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 * 2026-11-10        shinm              서비스 · 언어로 찾기
 */
public interface TermsRepository extends JpaRepository<Terms, Long> {

    List<Terms> findByServiceAndLangOrderByEffectiveAtDesc(ServiceType service, Lang lang);   // 약관 확인 · 가입 동의 화면
}
