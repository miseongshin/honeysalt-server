package com.honeysaltlove.global.common;

import org.springframework.http.ResponseEntity;

/**
 * 모든 API 응답 모양 — { status, code, message, data }
 * <pre>
 * - 성공은 등록을 포함해 모두 HTTP 200 · code OK
 * - 실패는 data 없음 (default-property-inclusion: non_null 이라 null 칸은 빠진다)
 * - /actuator/health 만 Spring 기본 모양
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
public record ApiResponse<T>(int status, String code, String message, T data) {

    /**
     * 성공 응답
     * @author shinm
     * @since 2026-11-10
     */
    public static <T> ApiResponse<T> success(T data) {
        return of(ResultCode.OK, data);
    }

    /**
     * 실패 응답 (data 없음)
     * @author shinm
     * @since 2026-11-10
     */
    public static ApiResponse<Void> error(ResultCode resultCode) {
        return of(resultCode, null);
    }

    /**
     * 결과 코드의 HTTP 상태로 감싼 응답
     * @author shinm
     * @since 2026-11-10
     */
    public static <T> ResponseEntity<ApiResponse<T>> toResponseEntity(ResultCode resultCode, T data) {
        return ResponseEntity.status(resultCode.getHttpStatus()).body(of(resultCode, data));
    }

    private static <T> ApiResponse<T> of(ResultCode resultCode, T data) {
        return new ApiResponse<>(resultCode.getHttpStatus().value(), resultCode.name(), resultCode.getMessage(), data);
    }
}
