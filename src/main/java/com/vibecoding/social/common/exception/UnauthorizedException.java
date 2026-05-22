package com.vibecoding.social.common.exception;

public class UnauthorizedException extends BusinessException {
  public UnauthorizedException() { super(ErrorCode.UNAUTHORIZED); }
}