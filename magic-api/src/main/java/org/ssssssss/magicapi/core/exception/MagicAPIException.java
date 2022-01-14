package org.ssssssss.magicapi.core.exception;

/**
 * magic-api异常对象
 *
 * @author mxd
 */
public class MagicAPIException extends RuntimeException {

	public MagicAPIException(String message) {
		super(message);
	}

	public MagicAPIException(String message, Throwable cause) {
		super(message, cause);
	}
}
