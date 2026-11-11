package com.honeysaltlove.domain.tag.repository;

import com.honeysaltlove.domain.tag.entity.Tag;
import com.honeysaltlove.domain.tag.entity.TagGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 태그 저장 · 조회
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findByName(String name);                       // 정규화된 이름으로만 부른다 — 서비스는 아래 findByNormalizedName 을 쓴다

    /**
     * 사용자가 쓴 그대로의 이름("#출근 ")으로 태그를 찾는다. 정규화를 거쳐 찾는다
     * @author shinm
     * @since 2026-11-06
     */
    default Optional<Tag> findByNormalizedName(String raw) {
        return findByName(Tag.normalize(raw));
    }

    List<Tag> findTop5ByOrderByUseCountDesc();                   // 많이 찾는 상황 · 묶음 · 언어와 상관없이 · 인덱스: ix_tag_popular

    List<Tag> findByTagGroupOrderByIdAsc(TagGroup tagGroup);     // 대표태그 묶음 목록
}
