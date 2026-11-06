package com.honeysaltlove.domain.app.repository;

import com.honeysaltlove.domain.app.entity.MemberAppKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 앱 키 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public interface MemberAppKeyRepository extends JpaRepository<MemberAppKey, Long> {

    Optional<MemberAppKey> findByAppKey(String appKey);   // 요청 헤더 X-App-Key 로 찾기 · 인덱스: uk_member_app_key_app_key

    List<MemberAppKey> findByMemberId(Long memberId);     // 회원에 연결된 앱 · 인덱스: ix_member_app_key_member
}
