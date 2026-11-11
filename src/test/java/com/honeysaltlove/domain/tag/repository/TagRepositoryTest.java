package com.honeysaltlove.domain.tag.repository;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.entity.Provider;
import com.honeysaltlove.domain.member.repository.MemberRepository;
import com.honeysaltlove.domain.tag.entity.Tag;
import com.honeysaltlove.domain.tag.entity.TagBlocklist;
import com.honeysaltlove.domain.tag.entity.TagGroup;
import com.honeysaltlove.domain.worry.entity.HoneyWorry;
import com.honeysaltlove.domain.worry.repository.HoneyWorryRepository;
import com.honeysaltlove.global.common.Lang;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 태그 테이블의 제약 · 연결 확인 — love_local(터널)에 붙어 돌고, 테스트가 끝나면 롤백한다(@Transactional)
 *
 * @author shinm
 * @since 2026-11-06
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-06        shinm              최초 생성
 */
@SpringBootTest
@Transactional
class TagRepositoryTest {

    @Autowired TagRepository tagRepository;
    @Autowired TagBlocklistRepository tagBlocklistRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired HoneyWorryRepository worryRepository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("이름이 같은 태그는 다시 등록할 수 없다 (묶음 · 언어가 달라도)")
    void duplicateName() {
        // given
        tagRepository.saveAndFlush(Tag.memberTag(TagGroup.INTEREST, "테스트구직", Lang.KO));

        // when · then
        assertThatThrownBy(() -> tagRepository.saveAndFlush(Tag.create("테스트구직", Lang.EN)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("묶음을 바꿔도 태그는 한 행이고 이름으로 찾을 수 있다")
    void changeGroup() {
        // given
        Tag tag = tagRepository.saveAndFlush(Tag.memberTag(TagGroup.INTEREST, "테스트교대근무", Lang.KO));

        // when
        tag.changeGroup(TagGroup.RHYTHM);
        em.flush();
        em.clear();

        // then
        Tag found = tagRepository.findByNormalizedName("#테스트 교대근무").orElseThrow();   // 사용자가 쓴 그대로 찾는다
        assertThat(found.getId()).isEqualTo(tag.getId());
        assertThat(found.getTagGroup()).isEqualTo(TagGroup.RHYTHM);
    }

    @Test
    @DisplayName("글과 회원에 태그를 연결하고 다시 읽을 수 있다")
    void connectTags() {
        // given
        Member member = memberRepository.save(Member.join(Provider.GOOGLE, "g-tag", "태그테스트", Lang.KO));
        Tag situation = tagRepository.save(Tag.create("테스트출근", Lang.KO));
        Tag personality = tagRepository.save(Tag.memberTag(TagGroup.PERSONALITY_TYPE, "테스트A", Lang.KO));
        HoneyWorry worry = worryRepository.save(HoneyWorry.write(member, "출근하기 싫어요", Lang.KO, true, false));

        // when
        worry.replaceTags(Set.of(situation));
        member.replaceTags(Set.of(personality));
        em.flush();
        em.clear();

        // then
        assertThat(worryRepository.findById(worry.getId()).orElseThrow().getTags())
                .extracting(Tag::getName).containsExactly("테스트출근");
        assertThat(memberRepository.findById(member.getId()).orElseThrow().getTags())
                .extracting(Tag::getName).containsExactly("테스트a");   // 영문은 소문자로 저장된다
    }

    @Test
    @DisplayName("태그를 차단하면 tag 행과 연결 행이 지워지고, 이름이 tag_blocklist 에 남는다")
    void blockTag() {
        // given
        Member member = memberRepository.save(Member.join(Provider.GOOGLE, "g-block", "차단테스트", Lang.KO));
        Tag tag = tagRepository.save(Tag.create("테스트광고", Lang.KO));
        HoneyWorry worry = worryRepository.save(HoneyWorry.write(member, "광고 글", Lang.KO, true, false));
        worry.replaceTags(Set.of(tag));
        em.flush();
        em.clear();

        // when — 서비스가 할 일: tag 행을 지우고(연결 행은 cascade) 이름을 옮긴다
        em.createNativeQuery("delete from tag where tag_id = :id").setParameter("id", tag.getId()).executeUpdate();
        tagBlocklistRepository.saveAndFlush(TagBlocklist.block("테스트광고", "광고"));
        em.clear();

        // then — 사용자가 쓴 그대로의 이름으로도 확인된다
        assertThat(tagRepository.findByNormalizedName("#테스트광고 ")).isEmpty();
        assertThat(worryRepository.findById(worry.getId()).orElseThrow().getTags()).isEmpty();
        assertThat(tagBlocklistRepository.existsByNormalizedName("#테스트 광고!")).isTrue();
    }
}
