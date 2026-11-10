package com.honeysaltlove.global.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 결과 코드 — 응답의 status · code · message 를 정한다 (설계서 4절 오류 표)
 * <pre>
 * - message 는 개발과 기록 확인에 쓰는 영어다
 * - 사용자에게 보이는 문구는 화면(Vue) · 앱이 code 를 보고 그 나라 말로 정한다
 * </pre>
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
@Getter
public enum ResultCode {

    OK(HttpStatus.OK, "OK"),
    VALIDATION(HttpStatus.BAD_REQUEST, "Invalid input"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Login required"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Access denied"),
    TERMS_REQUIRED(HttpStatus.FORBIDDEN, "Terms agreement required"),   // 가입을 덜 마쳤거나 바뀐 약관에 아직 동의하지 않았다
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not found"),
    DUPLICATE(HttpStatus.CONFLICT, "Already exists"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final HttpStatus httpStatus;
    private final String message;

    ResultCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
