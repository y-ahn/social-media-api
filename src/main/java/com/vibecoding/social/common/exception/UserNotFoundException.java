package com.vibecoding.social.common.exception;

public class UserNotFoundException extends BusinessException {
  public UserNotFoundException(Long userId) { super(ErrorCode.USER_NOT_FOUND); }
  public UserNotFoundException(String email) { super(ErrorCode.USER_NOT_FOUND); }
}