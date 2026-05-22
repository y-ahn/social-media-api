package com.vibecoding.social.common.exception;

public class AlreadyFollowingException extends BusinessException {
  public AlreadyFollowingException() { super(ErrorCode.ALREADY_FOLLOWING); }
}