package com.vibecoding.social.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * PART 5 — 비즈니스 오류 코드 열거형
 * 모든 에러 코드를 한 곳에서 관리한다.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 회원
    USER_NOT_FOUND       ("해당 사용자를 찾을 수 없습니다.",          HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL      ("이미 사용 중인 이메일입니다.",             HttpStatus.CONFLICT),
    DUPLICATE_USERNAME   ("이미 사용 중인 사용자명입니다.",           HttpStatus.CONFLICT),
    INVALID_PASSWORD     ("비밀번호가 올바르지 않습니다.",            HttpStatus.UNAUTHORIZED),

    // 인증
    UNAUTHORIZED         ("인증이 필요합니다.",                        HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED        ("접근 권한이 없습니다.",                     HttpStatus.FORBIDDEN),
    INVALID_TOKEN        ("유효하지 않은 토큰입니다.",                 HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN        ("만료된 토큰입니다.",                        HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND("리프레시 토큰을 찾을 수 없습니다.",       HttpStatus.UNAUTHORIZED),

    // 게시글
    POST_NOT_FOUND       ("해당 게시글을 찾을 수 없습니다.",           HttpStatus.NOT_FOUND),
    POST_AUTHOR_MISMATCH ("게시글 작성자만 수정/삭제할 수 있습니다.", HttpStatus.FORBIDDEN),

    // 팔로우
    ALREADY_FOLLOWING    ("이미 팔로우 중입니다.",                     HttpStatus.CONFLICT),
    NOT_FOLLOWING        ("팔로우 중이지 않습니다.",                   HttpStatus.BAD_REQUEST),
    CANNOT_FOLLOW_SELF   ("자기 자신을 팔로우할 수 없습니다.",        HttpStatus.BAD_REQUEST),

    // 좋아요
    ALREADY_LIKED        ("이미 좋아요를 눌렀습니다.",                HttpStatus.CONFLICT),
    NOT_LIKED            ("좋아요를 누르지 않은 게시글입니다.",        HttpStatus.BAD_REQUEST),

    // 댓글
    COMMENT_NOT_FOUND    ("해당 댓글을 찾을 수 없습니다.",            HttpStatus.NOT_FOUND),
    COMMENT_AUTHOR_MISMATCH("댓글 작성자만 수정/삭제할 수 있습니다.",HttpStatus.FORBIDDEN),

    // 서버
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다.",                HttpStatus.INTERNAL_SERVER_ERROR);

    private final String message;
    private final HttpStatus httpStatus;
}
