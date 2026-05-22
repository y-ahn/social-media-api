package com.vibecoding.social.common.exception;

public class DuplicateUsernameException extends BusinessException {
    public DuplicateUsernameException() {
        super(ErrorCode.DUPLICATE_USERNAME);
    }
}