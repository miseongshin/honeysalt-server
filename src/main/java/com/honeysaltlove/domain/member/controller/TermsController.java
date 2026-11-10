package com.honeysaltlove.domain.member.controller;

import com.honeysaltlove.domain.member.dto.TermsResponse;
import com.honeysaltlove.domain.member.entity.ServiceType;
import com.honeysaltlove.domain.member.service.TermsService;
import com.honeysaltlove.global.common.ApiResponse;
import com.honeysaltlove.global.common.Lang;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

/**
 * 약관 확인 · 가입 동의 화면 — 로그인하지 않아도 볼 수 있다
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
@RequestMapping("/api/web/v1/terms")
public class TermsController {

    private final TermsService termsService;

    /**
     * 지금 보여 줄 약관 — GET /api/web/v1/terms?service=HONEYSALTLOVE&lang=ko
     * @author shinm
     * @since 2026-11-10
     */
    @GetMapping
    public ApiResponse<List<TermsResponse>> findCurrent(
            @RequestParam(defaultValue = "HONEYSALTLOVE") ServiceType service,
            @RequestParam(defaultValue = "ko") String lang) {
        List<TermsResponse> terms = termsService.findCurrent(service, Lang.from(lang), Instant.now()).stream()
                .map(TermsResponse::from)
                .toList();
        return ApiResponse.success(terms);
    }
}
