package com.honeysaltlove.global.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 예외를 한곳에서 { status, code, message } 로 바꾼다
 *
 * @author shinm
 * @since 2026-11-10
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-11-10        shinm              최초 생성
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 업무 규칙 위반
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        return ApiResponse.toResponseEntity(e.getResultCode(), null);
    }

    /**
     * 입력값이 형식에 맞지 않을 때 — 400 VALIDATION
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, HandlerMethodValidationException.class,
            MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class})
    public ResponseEntity<ApiResponse<Void>> handleValidation(Exception e) {
        return ApiResponse.toResponseEntity(ResultCode.VALIDATION, null);
    }

    /**
     * 그 밖의 예외
     * <pre>
     * - 스프링 웹 예외 가운데 4xx(없는 주소 · 틀린 메서드 · 틀린 형식 …)는 오류 로그를 남기지 않는다 — 404 는 NOT_FOUND, 그 밖의 4xx 는 400 VALIDATION
     * - 나머지는 500 INTERNAL_ERROR (사람이 봐야 해서 스택을 남긴다)
     * </pre>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        if (e instanceof ErrorResponse er && er.getStatusCode().is4xxClientError()) {
            ResultCode code = er.getStatusCode().value() == 404 ? ResultCode.NOT_FOUND : ResultCode.VALIDATION;
            return ApiResponse.toResponseEntity(code, null);
        }
        log.error("unexpected error", e);
        return ApiResponse.toResponseEntity(ResultCode.INTERNAL_ERROR, null);
    }
}
