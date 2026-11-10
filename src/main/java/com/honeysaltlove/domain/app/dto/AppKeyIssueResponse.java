package com.honeysaltlove.domain.app.dto;

/**
 * 앱 키 발급 응답 — appSecret 은 이때 한 번만 준다 (서버에는 해시만 남아 다시 보여 줄 수 없다)
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
public record AppKeyIssueResponse(String appKey, String appSecret) {
}
