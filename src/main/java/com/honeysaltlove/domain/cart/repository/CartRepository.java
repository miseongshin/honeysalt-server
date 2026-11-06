package com.honeysaltlove.domain.cart.repository;

import com.honeysaltlove.domain.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * 활동바구니 저장 · 조회 · 가져간 것 지우기
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public interface CartRepository extends JpaRepository<Cart, Long> {

    List<Cart> findByMemberIdAndSentAtIsNullOrderByIdAsc(Long memberId);      // 웹 바구니 · 인덱스: uk_cart_member_target

    List<Cart> findByMemberIdAndSentAtIsNotNullOrderByIdAsc(Long memberId);   // 앱이 받을 것 (GET /app/takes) · 지우지 않는다

    /**
     * 앱이 저장을 마친 번호만 지운다 (ack). 그 회원의 것만 지워서 남의 번호를 넣어도 지워지지 않는다
     * 보내지 않은 행(바구니에 있는 것)도 지우지 않는다 · 서비스 트랜잭션 안에서 부른다
     * 같은 행을 엔티티로 먼저 읽지 않는다 — 지운 행이 영속성 컨텍스트에 남는다
     * @author shinm
     * @since 2026-11-06
     */
    @Modifying
    @Query("delete from Cart c where c.member.id = :memberId and c.id in :cartIds and c.sentAt is not null")
    int deleteTaken(@Param("memberId") Long memberId, @Param("cartIds") Collection<Long> cartIds);
}
