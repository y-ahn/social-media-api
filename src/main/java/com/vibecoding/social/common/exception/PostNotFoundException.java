package com.vibecoding.social.common.exception;

public class PostNotFoundException extends BusinessException {
  public PostNotFoundException(Long postId) { super(ErrorCode.POST_NOT_FOUND); }
}