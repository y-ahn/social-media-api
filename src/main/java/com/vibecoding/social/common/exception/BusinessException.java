package com.vibecoding.social.common.exception;

public class BusinessException extends ApplicationException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }
}