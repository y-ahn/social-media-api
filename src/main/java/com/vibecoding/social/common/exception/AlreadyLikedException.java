package com.vibecoding.social.common.exception;

public class AlreadyLikedException extends BusinessException {
  public AlreadyLikedException() { super(ErrorCode.ALREADY_LIKED); }
}