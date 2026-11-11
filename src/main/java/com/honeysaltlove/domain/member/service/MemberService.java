package com.honeysaltlove.domain.member.service;

import com.honeysaltlove.domain.app.entity.MemberAppKey;
import com.honeysaltlove.domain.app.repository.MemberAppKeyRepository;
import com.honeysaltlove.domain.cart.repository.CartRepository;
import com.honeysaltlove.domain.feedback.repository.FeedbackRepository;
import com.honeysaltlove.domain.member.auth.LoginMember;
import com.honeysaltlove.domain.member.dto.MemberResponse;
import com.honeysaltlove.domain.member.dto.MemberUpdateRequest;
import com.honeysaltlove.domain.member.dto.TermsAgreeRequest;
import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.entity.MemberStatus;
import com.honeysaltlove.domain.member.entity.MemberTermsHistory;
import com.honeysaltlove.domain.member.entity.ServiceType;
import com.honeysaltlove.domain.member.entity.Terms;
import com.honeysaltlove.domain.member.repository.MemberNicknameHistoryRepository;
import com.honeysaltlove.domain.member.repository.MemberRepository;
import com.honeysaltlove.domain.member.repository.MemberTermsHistoryRepository;
import com.honeysaltlove.domain.tag.entity.Tag;
import com.honeysaltlove.domain.tag.repository.TagRepository;
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
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 회원 — 가입 마치기 · 약관 다시 동의 · 내 정보 · 수정 · 탈퇴
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 * 2026-11-10        shinm              내 정보 · 수정 · 탈퇴
 * 2026-11-11        shinm              자동 닉네임이 50번 겹치면 뒤에 1, 2, 3 … 을 붙여 빈 이름까지 (가입이 막히지 않게)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private static final int NICKNAME_TRIES = 50;   // 자동 닉네임을 다시 뽑는 횟수 · 모두 겹치면 숫자를 붙인다
    private static final int NICKNAME_MAX = 12;   // 닉네임 2~12자 (TermsAgreeRequest · MemberUpdateRequest 와 같다)
    private static final int MIN_AGE = 14;     // 만 14세 미만은 가입할 수 없다

    private final MemberRepository memberRepository;
    private final MemberTermsHistoryRepository termsHistoryRepository;
    private final MemberNicknameHistoryRepository nicknameHistoryRepository;
    private final CartRepository cartRepository;
    private final FeedbackRepository feedbackRepository;
    private final MemberAppKeyRepository appKeyRepository;
    private final TagRepository tagRepository;
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
     * 내 정보
     * @author shinm
     * @since 2026-11-10
     */
    @Transactional(readOnly = true)
    public MemberResponse findMe(Long memberId) {
        return MemberResponse.from(getMember(memberId));   // 태그는 지연 로딩이라 트랜잭션 안에서 바꾼다
    }

    /**
     * 내 정보 수정 — 닉네임(비우면 그대로) · 출생연도(비우면 지움) · 내 정보 태그(고른 것 전부)
     * @author shinm
     * @since 2026-11-10
     */
    @Transactional
    public MemberResponse updateMe(Long memberId, MemberUpdateRequest request, Instant now) {
        Member member = getMember(memberId);

        if (StringUtils.hasText(request.nickname()) && !request.nickname().equals(member.getNickname())) {
            if (memberRepository.existsByNickname(request.nickname())) {
                throw new BusinessException(ResultCode.DUPLICATE);
            }
            nicknameHistoryRepository.save(member.changeNickname(request.nickname()));   // 바꾸기 전 이름을 백업
        }

        Integer birthYear = request.birthYear();
        int thisYear = now.atZone(ZoneOffset.UTC).getYear();
        if (birthYear != null && birthYear > thisYear - MIN_AGE) {
            throw new BusinessException(ResultCode.VALIDATION);   // 만 14세 미만은 가입할 수 없다 (약관 제2조)
        }
        member.changeBirthYear(birthYear);

        member.replaceTags(memberTags(request.tagIds()));
        try {
            memberRepository.flush();   // 닉네임이 동시에 겹치면 UQ 에 걸린다 — 500 이 아니라 409
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ResultCode.DUPLICATE, e);
        }
        return MemberResponse.from(member);
    }

    /**
     * 탈퇴 — 개인정보를 바로 비우고, 연결된 기록을 같은 트랜잭션에서 지운다 (개인정보처리방침 3절)
     * <pre>
     * - 비운다: 소셜 회원번호 · 닉네임 · 출생연도 · 마지막 로그인 · 내 정보 태그 (Member.withdraw)
     * - 지운다: 닉네임 백업 · 꿀팁 바구니 · 의견 보내기 · 약관 동의 기록
     * - 앱 연결: 회원 연결만 끊는다 (앱 키는 남겨서 앱 혼자 계속 쓴다)
     * - 남긴다: 글 · 댓글 · 좋아요 · 신고 기록 (작성자는 '탈퇴한 회원')
     * </pre>
     * @author shinm
     * @since 2026-11-10
     */
    @Transactional
    public void withdraw(Long memberId, Instant now) {
        Member member = getMember(memberId);
        nicknameHistoryRepository.deleteByMemberId(memberId);
        cartRepository.deleteByMemberId(memberId);
        feedbackRepository.deleteByMemberId(memberId);
        termsHistoryRepository.deleteByMemberId(memberId);
        appKeyRepository.findByMemberId(memberId).forEach(MemberAppKey::unlink);
        member.withdraw(now);
        log.info("member withdrawn: memberId={}", memberId);
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

    private Member getMember(Long memberId) {
        if (memberId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);   // 가입 대기(회원 번호 없음) — findById(null) 은 500 이 된다
        }
        return memberRepository.findById(memberId)
                .filter(m -> m.getStatus() == MemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED));
    }

    /**
     * 내 정보 태그 — 운영자가 대표태그 묶음에 넣은 태그만 고를 수 있다 (#상황 태그 · 없는 번호는 400)
     * 건강 · 마음 상태는 회원 정보에 두지 않는다는 결정(2026-11-10)을 서버에서도 지킨다
     */
    private Set<Tag> memberTags(List<Long> tagIds) {
        Set<Long> ids = new HashSet<>(tagIds);
        List<Tag> tags = tagRepository.findAllById(ids);
        if (tags.size() != ids.size() || tags.stream().anyMatch(t -> t.getTagGroup() == null)) {
            throw new BusinessException(ResultCode.VALIDATION);
        }
        return new HashSet<>(tags);
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
        // 50번 모두 겹치는 드문 경우 — 가입이 막히지 않게 이름 뒤에 1, 2, 3 … 을 붙여 빈 이름이 나올 때까지 본다
        // 12자를 넘으면(이름 9자 + 네 자리) 새 이름을 뽑아 1 부터 다시 센다
        log.warn("auto nickname collided {} times — appending number", NICKNAME_TRIES);
        String base = nicknameGenerator.generate();
        for (int n = 1; ; n++) {
            String nickname = base + n;
            if (nickname.length() > NICKNAME_MAX) {
                base = nicknameGenerator.generate();
                n = 0;
                continue;
            }
            if (!memberRepository.existsByNickname(nickname)) {
                return nickname;
            }
        }
    }
}
