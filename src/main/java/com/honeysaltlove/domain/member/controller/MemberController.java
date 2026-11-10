package com.honeysaltlove.domain.member.controller;

import com.honeysaltlove.domain.member.auth.LoginMember;
import com.honeysaltlove.domain.member.auth.LoginSessionUpdater;
import com.honeysaltlove.domain.member.dto.MemberResponse;
import com.honeysaltlove.domain.member.dto.MemberUpdateRequest;
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
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * 회원 — 내 정보 · 수정 · 탈퇴 · 가입 마치기 · 약관 다시 동의
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 * 2026-11-10        shinm              내 정보 · 수정 · 탈퇴
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/web/v1/member")
public class MemberController {

    private final MemberService memberService;
    private final LoginSessionUpdater loginSessionUpdater;

    /**
     * 내 정보 — GET /api/web/v1/member
     * @author shinm
     * @since 2026-11-10
     */
    @GetMapping
    public ApiResponse<MemberResponse> findMe(@AuthenticationPrincipal LoginMember login) {
        return ApiResponse.success(memberService.findMe(login.getMemberId()));
    }

    /**
     * 내 정보 수정 — PATCH /api/web/v1/member
     * @author shinm
     * @since 2026-11-10
     */
    @PatchMapping
    public ApiResponse<MemberResponse> updateMe(@AuthenticationPrincipal LoginMember login,
                                                @Valid @RequestBody MemberUpdateRequest request) {
        return ApiResponse.success(memberService.updateMe(login.getMemberId(), request, Instant.now()));
    }

    /**
     * 탈퇴 — DELETE /api/web/v1/member · 끝나면 세션을 끊는다 (로그아웃)
     * 개정 약관에 동의하지 않은 회원(가입 대기 권한)도 탈퇴할 수 있다 · 회원이 아닌 가입 대기는 로그아웃만 된다
     * @author shinm
     * @since 2026-11-10
     */
    @DeleteMapping
    public ApiResponse<Void> withdraw(@AuthenticationPrincipal LoginMember login, Authentication authentication,
                                      HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        if (login.getMemberId() != null) {
            memberService.withdraw(login.getMemberId(), Instant.now());
        }
        new SecurityContextLogoutHandler().logout(httpRequest, httpResponse, authentication);
        new CookieClearingLogoutHandler("SESSION").logout(httpRequest, httpResponse, authentication);
        return ApiResponse.success(null);
    }

    /**
     * 약관 동의 — POST /api/web/v1/member/terms · 처음 온 사람은 가입을 마치고, 회원은 개정된 약관에 다시 동의한다
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
