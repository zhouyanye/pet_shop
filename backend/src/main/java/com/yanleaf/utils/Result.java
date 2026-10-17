package com.yanleaf.utils;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

	private boolean success;
	private int code;
	private String message;
	private T data;

	public static <T> Result<T> ok(T data) {
		return new Result<>(true, 200, "ok", data);
	}

	public static <T> Result<T> ok() {
		return new Result<>(true, 200, "ok", null);
	}

	public static <T> Result<T> fail(int code, String message) {
		return new Result<>(false, code, message, null);
	}

	public static <T> Result<T> fail(String message) {
		return new Result<>(false, 500, message, null);
	}
}
