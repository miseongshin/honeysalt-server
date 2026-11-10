package com.honeysaltlove.domain.app.controller;

import com.honeysaltlove.domain.app.dto.AppKeyIssueResponse;
import com.honeysaltlove.domain.app.service.AppKeyService;
import com.honeysaltlove.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 앱 키 발급 — 꿀방울 앱이 처음 실행할 때 부른다 (로그인 없음)
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
@RequestMapping("/api/app/v1/keys")
public class AppKeyController {

    private final AppKeyService appKeyService;

    /**
     * 앱 키 발급 — POST /api/app/v1/keys · 비밀값은 이 응답에서 한 번만 준다
     * @author shinm
     * @since 2026-11-10
     */
    @PostMapping
    public ApiResponse<AppKeyIssueResponse> issue() {
        return ApiResponse.success(appKeyService.issue());
    }
}
