package com.honeysaltlove.domain.member.service;

import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.entity.Provider;
import com.honeysaltlove.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * 소셜 로그인 성공 뒤 회원을 찾는다
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
public class LoginService {

    private final MemberRepository memberRepository;

    /**
     * 소셜 회원번호로 회원을 찾고, 있으면 마지막 로그인 시각을 남긴다
     * 없으면 비어 있는 값을 돌려준다 — 처음 온 사람이라 가입 대기로 둔다
     * 탈퇴한 회원은 소셜 회원번호를 비워서 찾아지지 않는다
     * @author shinm
     * @since 2026-11-10
     */
    @Transactional
    public Optional<Member> login(Provider provider, String providerUid, Instant now) {
        Optional<Member> member = memberRepository.findByProviderAndProviderUid(provider, providerUid);
        member.ifPresentOrElse(
                m -> {
                    m.recordLogin(now);
                    log.info("login: memberId={}, provider={}", m.getId(), provider);
                },
                () -> log.info("login: new visitor (signup pending), provider={}", provider));   // 소셜 회원번호는 찍지 않는다
        return member;
    }
}
