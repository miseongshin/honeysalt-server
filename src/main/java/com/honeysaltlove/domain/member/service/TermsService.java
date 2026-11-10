package com.honeysaltlove.domain.member.service;

import com.honeysaltlove.domain.member.entity.ServiceType;
import com.honeysaltlove.domain.member.entity.Terms;
import com.honeysaltlove.domain.member.entity.TermsType;
import com.honeysaltlove.domain.member.repository.TermsRepository;
import com.honeysaltlove.global.common.Lang;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 약관 찾기
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermsService {

    private final TermsRepository termsRepository;

    /**
     * 지금 보여 줄 약관 — 종류(이용약관 · 개인정보처리방침)마다 하나
     * <pre>
     * - 시행 중인 것 가운데 가장 최근 버전
     * - 시행 중인 것이 없으면 다음에 시행될 버전 (서비스를 열기 전 · 첫 버전 시행일 전)
     * - 요청한 언어의 약관이 하나도 없으면 한국어 (서비스 단위로 바꾼다)
     * - 그래서 개정할 때는 같은 버전을 모든 언어에 함께 넣는다 — 한 언어에만 넣으면 언어마다 다른 버전이 나간다
     * </pre>
     * @author shinm
     * @since 2026-11-10
     */
    public List<Terms> findCurrent(ServiceType service, Lang lang, Instant now) {
        List<Terms> all = termsRepository.findByServiceAndLangOrderByEffectiveAtDesc(service, lang);
        if (all.isEmpty() && lang != Lang.KO) {
            all = termsRepository.findByServiceAndLangOrderByEffectiveAtDesc(service, Lang.KO);
        }

        Map<TermsType, List<Terms>> byType = all.stream()
                .collect(Collectors.groupingBy(Terms::getType));   // 각 목록은 시행일이 늦은 순 그대로

        List<Terms> current = new ArrayList<>();
        byType.values().forEach(list -> current.add(pick(list, now)));
        current.sort(Comparator.comparing(Terms::getType));
        return current;
    }

    /**
     * 회원 가입에 동의해야 하는 honeysaltlove 필수 약관 (지금 보여 줄 버전)
     * @author shinm
     * @since 2026-11-10
     */
    public List<Terms> findCurrentRequired(Instant now) {
        return findCurrent(ServiceType.HONEYSALTLOVE, Lang.KO, now).stream()
                .filter(Terms::isRequired)
                .toList();
    }

    private Terms pick(List<Terms> newestFirst, Instant now) {
        return newestFirst.stream()
                .filter(t -> !t.getEffectiveAt().isAfter(now))
                .findFirst()
                .orElse(newestFirst.get(newestFirst.size() - 1));   // 아직 시행 전뿐이면 가장 먼저 시행될 버전
    }
}
