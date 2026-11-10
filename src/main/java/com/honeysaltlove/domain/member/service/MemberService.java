package com.honeysaltlove.domain.member.service;

import com.honeysaltlove.domain.member.auth.LoginMember;
import com.honeysaltlove.domain.member.dto.TermsAgreeRequest;
import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.entity.MemberTermsHistory;
import com.honeysaltlove.domain.member.entity.ServiceType;
import com.honeysaltlove.domain.member.entity.Terms;
import com.honeysaltlove.domain.member.repository.MemberRepository;
import com.honeysaltlove.domain.member.repository.MemberTermsHistoryRepository;
import com.honeysaltlove.global.common.BusinessException;
import com.honeysaltlove.global.common.Lang;
import com.honeysaltlove.global.common.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 회원 — 가입 마치기 · 약관 다시 동의
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private static final int NICKNAME_TRIES = 10;

    private final MemberRepository memberRepository;
    private final MemberTermsHistoryRepository termsHistoryRepository;
    private final TermsService termsService;
    private final NicknameGenerator nicknameGenerator;

    /**
     * 약관에 동의한다
     * <pre>
     * - 처음 온 사람(가입 대기): 회원을 만들고 동의를 남긴다 — 가입 마치기
     * - 이미 회원: 아직 동의하지 않은 약관만 남긴다 — 약관이 개정됐을 때 다시 동의
     * - 지금 보여 주는 필수 약관에 모두 동의해야 한다 (아니면 400 VALIDATION)
     * </pre>
     * @author shinm
     * @since 2026-11-10
     */
    @Transactional
    public Member agreeTerms(LoginMember login, TermsAgreeRequest request, Instant now) {
        Set<Long> agreed = new HashSet<>(request.agreedTermsIds());
        List<Terms> current = termsService.findCurrent(ServiceType.HONEYSALTLOVE, Lang.KO, now);
        boolean allRequired = current.stream().filter(Terms::isRequired).allMatch(t -> agreed.contains(t.getId()));
        if (!allRequired) {
            throw new BusinessException(ResultCode.VALIDATION);
        }

        Member member = findOrJoin(login, request);
        Set<Long> already = termsHistoryRepository.findTermsIdsByMemberId(member.getId());
        current.stream()
                .filter(t -> agreed.contains(t.getId()) && !already.contains(t.getId()))
                .forEach(t -> termsHistoryRepository.save(MemberTermsHistory.agree(member, t)));
        log.info("terms agreed: memberId={}", member.getId());
        return member;
    }

    /**
     * 지금 보여 주는 필수 약관에 모두 동의했는지 — 아니면 로그인할 때 다시 동의를 받는다
     * @author shinm
     * @since 2026-11-10
     */
    @Transactional(readOnly = true)
    public boolean hasAgreedCurrentTerms(Long memberId, Instant now) {
        Set<Long> already = termsHistoryRepository.findTermsIdsByMemberId(memberId);
        return termsService.findCurrentRequired(now).stream().allMatch(t -> already.contains(t.getId()));
    }

    private Member findOrJoin(LoginMember login, TermsAgreeRequest request) {
        if (login.getMemberId() != null) {
            return memberRepository.findById(login.getMemberId())
                    .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED));
        }
        // 가입 마치기를 두 번 눌러도 회원이 둘이 되지 않게 — 이미 만들어졌으면 그 회원을 쓴다
        return memberRepository.findByProviderAndProviderUid(login.getProvider(), login.getProviderUid())
                .orElseGet(() -> join(login, request));
    }

    private Member join(LoginMember login, TermsAgreeRequest request) {
        String nickname = StringUtils.hasText(request.nickname()) ? request.nickname() : newNickname();
        if (memberRepository.existsByNickname(nickname)) {
            throw new BusinessException(ResultCode.DUPLICATE);
        }
        Lang lang = StringUtils.hasText(request.lang()) ? Lang.from(request.lang()) : Lang.KO;
        Member member;
        try {
            // 바로 DB 에 보낸다 — 거의 동시에 두 번 오거나(더블클릭) 같은 닉네임을 동시에 고르면 UQ 에 걸리는데, 500 이 아니라 409 로
            member = memberRepository.saveAndFlush(
                    Member.join(login.getProvider(), login.getProviderUid(), nickname, lang));
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ResultCode.DUPLICATE, e);
        }
        log.info("member joined: memberId={}", member.getId());
        return member;
    }

    private String newNickname() {
        for (int i = 0; i < NICKNAME_TRIES; i++) {
            String nickname = nicknameGenerator.generate();
            if (!memberRepository.existsByNickname(nickname)) {
                return nickname;
            }
        }
        // 열 번 모두 겹치는 드문 경우 — 숫자 두 자리를 붙인다 (그래도 겹치면 저장할 때 409 DUPLICATE)
        log.warn("auto nickname collided {} times — appending number", NICKNAME_TRIES);
        return nicknameGenerator.generateWithNumber();
    }
}
