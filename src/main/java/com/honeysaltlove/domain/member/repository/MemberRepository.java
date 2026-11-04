package com.honeysaltlove.domain.member.repository;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 회원 조회 · 저장
 *
 * @author shinm
 * @since 2026-11-04
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-04        shinm              최초 생성
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByProviderAndProviderUid(Provider provider, String providerUid);   // 로그인

    boolean existsByNickname(String nickname);
}
