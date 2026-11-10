package com.honeysaltlove.domain.member.controller;

import com.honeysaltlove.domain.member.auth.LoginMember;
import com.honeysaltlove.domain.member.auth.LoginSessionUpdater;
import com.honeysaltlove.domain.member.dto.TermsAgreeRequest;
import com.honeysaltlove.domain.member.dto.TermsAgreeResponse;
import com.honeysaltlove.domain.member.entity.Member;
import com.honeysaltlove.domain.member.service.MemberService;
import com.honeysaltlove.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * 회원 — 가입 마치기 · 약관 다시 동의 (내 정보 · 탈퇴는 이어서 만든다)
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/web/v1/member")
public class MemberController {

    private final MemberService memberService;
    private final LoginSessionUpdater loginSessionUpdater;

    /**
     * 약관 동의 — 처음 온 사람은 가입을 마치고, 회원은 개정된 약관에 다시 동의한다
     * 끝나면 세션 권한을 회원 권한으로 바꾼다
     * @author shinm
     * @since 2026-11-10
     */
    @PostMapping("/terms")
    public ApiResponse<TermsAgreeResponse> agreeTerms(@AuthenticationPrincipal LoginMember login,
                                                      @Valid @RequestBody TermsAgreeRequest request,
                                                      Authentication authentication,
                                                      HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        boolean joined = login.getMemberId() == null;
        Member member = memberService.agreeTerms(login, request, Instant.now());
        loginSessionUpdater.promote(authentication, login, member, httpRequest, httpResponse);
        return ApiResponse.success(new TermsAgreeResponse(member.getId(), member.getNickname(), joined));
    }
}
