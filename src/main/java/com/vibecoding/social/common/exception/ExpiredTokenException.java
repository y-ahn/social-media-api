package com.vibecoding.social.common.exception;

public class ExpiredTokenException extends BusinessException {
    public ExpiredTokenException() { super(ErrorCode.EXPIRED_TOKEN); }
}