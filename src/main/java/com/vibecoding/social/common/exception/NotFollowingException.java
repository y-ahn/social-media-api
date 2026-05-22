package com.vibecoding.social.common.exception;

public class NotFollowingException extends BusinessException {
    public NotFollowingException() { super(ErrorCode.NOT_FOLLOWING); }
}