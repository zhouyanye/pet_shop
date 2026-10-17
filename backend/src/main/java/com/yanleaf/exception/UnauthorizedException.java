package com.yanleaf.exception;


/**
 * 未登录 / token 无效异常
 * 抛出后由 GlobalExceptionHandler 统一转成 HTTP 401
 */
public class UnauthorizedException extends RuntimeException {
	public UnauthorizedException(String message) {
		super(message);
	}
}