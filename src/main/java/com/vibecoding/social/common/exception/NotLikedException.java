package com.vibecoding.social.common.exception;

public class NotLikedException extends BusinessException {
    public NotLikedException() { super(ErrorCode.NOT_LIKED); }
}