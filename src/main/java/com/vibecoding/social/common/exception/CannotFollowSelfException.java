package com.vibecoding.social.common.exception;

public class CannotFollowSelfException extends BusinessException {
    public CannotFollowSelfException() { super(ErrorCode.CANNOT_FOLLOW_SELF); }
}