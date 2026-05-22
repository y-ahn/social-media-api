package com.vibecoding.social.common.exception;

public class InvalidTokenException extends BusinessException {
  public InvalidTokenException() { super(ErrorCode.INVALID_TOKEN); }
}