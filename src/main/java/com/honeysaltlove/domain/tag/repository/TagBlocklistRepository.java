package com.honeysaltlove.domain.tag.repository;

import com.honeysaltlove.domain.tag.entity.Tag;
import com.honeysaltlove.domain.tag.entity.TagBlocklist;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용이 차단된 태그 저장 · 조회 — 태그를 만들 때 existsByNormalizedName(이름) 으로 확인한다
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public interface TagBlocklistRepository extends JpaRepository<TagBlocklist, String> {

    /**
     * 사용자가 쓴 그대로의 이름이 차단된 이름인지 확인한다. 정규화를 거쳐 확인한다
     * @author shinm
     * @since 2026-11-06
     */
    default boolean existsByNormalizedName(String raw) {
        return existsById(Tag.normalize(raw));
    }
}
