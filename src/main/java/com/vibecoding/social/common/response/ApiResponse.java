package com.vibecoding.social.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * PART 1 — 공통 응답 포맷 ApiResponse<T>
 * 모든 API 응답을 이 형태로 래핑한다.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드 JSON 제외
public class ApiResponse<T> {

    private final boolean success;
    private final String  code;
    private final String  message;
    private final T       data;
    private final List<FieldError> errors;
    private final String  timestamp;

    private ApiResponse(boolean success, String code, String message, T data, List<FieldError> errors) {
        this.success   = success;
        this.code      = code;
        this.message   = message;
        this.data      = data;
        this.errors    = errors;
        this.timestamp = LocalDateTime.now().toString();
    }

    // 성공
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "SUCCESS", "요청이 성공했습니다.", data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, "SUCCESS", message, data, null);
    }

    // 실패
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, code, message, null, null);
    }

    // 유효성 검사 실패
    public static <T> ApiResponse<T> validationError(List<FieldError> errors) {
        return new ApiResponse<>(false, "VALIDATION_FAILED", "입력값이 올바르지 않습니다.", null, errors);
    }

    @Getter
    public static class FieldError {
        private final String field;
        private final String message;

        public FieldError(String field, String message) {
            this.field   = field;
            this.message = message;
        }
    }
}
