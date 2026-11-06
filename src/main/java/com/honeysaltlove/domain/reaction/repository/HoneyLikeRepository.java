package com.honeysaltlove.domain.reaction.repository;

import com.honeysaltlove.domain.reaction.entity.HoneyLike;
import com.honeysaltlove.domain.reaction.entity.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 좋아요 저장 · 조회 · 취소(삭제)
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public interface HoneyLikeRepository extends JpaRepository<HoneyLike, Long> {

    boolean existsByMemberIdAndTargetTypeAndTargetId(Long memberId, TargetType targetType, Long targetId);   // 내가 눌렀는지

    boolean existsByTargetTypeAndTargetId(TargetType targetType, Long targetId);   // 좋아요를 받은 글은 고치지 않는다(잠금)

    long countByTargetTypeAndTargetId(TargetType targetType, Long targetId);       // 대상 하나의 좋아요 수 (확인 · 테스트용) — 스케줄러가 1분마다 다시 셀 때는 쓰지 않고 group by 로 한 번에 센다 · 인덱스: ix_honey_like_target

    /**
     * 좋아요를 취소한다. 쿼리 한 번으로 지운다 (메서드 이름 삭제는 SELECT 뒤 한 건씩 DELETE)
     * 서비스 트랜잭션 안에서 부른다 · 같은 좋아요를 엔티티로 먼저 읽지 않는다 — 지운 행이 영속성 컨텍스트에 남는다
     * @author shinm
     * @since 2026-11-06
     */
    @Modifying
    @Query("delete from HoneyLike l where l.member.id = :memberId and l.targetType = :targetType and l.targetId = :targetId")
    int cancel(@Param("memberId") Long memberId, @Param("targetType") TargetType targetType, @Param("targetId") Long targetId);
}
