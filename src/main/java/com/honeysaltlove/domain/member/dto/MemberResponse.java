package com.honeysaltlove.domain.member.dto;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.tag.entity.Tag;
import com.honeysaltlove.domain.tag.entity.TagGroup;

import java.util.Comparator;
import java.util.List;

/**
 * 내 정보 — GET /api/web/v1/member
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
public record MemberResponse(
        Long memberId,
        String nickname,
        Integer birthYear,
        String lang,
        String role,
        List<TagItem> tags
) {

    /**
     * 내 정보 태그 하나
     */
    public record TagItem(Long tagId, TagGroup tagGroup, String name) {
    }

    public static MemberResponse from(Member member) {
        List<TagItem> tags = member.getTags().stream()
                .sorted(Comparator.comparing(Tag::getId))
                .map(t -> new TagItem(t.getId(), t.getTagGroup(), t.getName()))
                .toList();
        return new MemberResponse(member.getId(), member.getNickname(), member.getBirthYear(),
                member.getLang().getCode(), member.getRole().name(), tags);
    }
}
