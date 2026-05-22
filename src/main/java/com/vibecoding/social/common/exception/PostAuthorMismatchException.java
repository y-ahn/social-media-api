package com.vibecoding.social.common.exception;

public class PostAuthorMismatchException extends BusinessException {
  public PostAuthorMismatchException() { super(ErrorCode.POST_AUTHOR_MISMATCH); }
}