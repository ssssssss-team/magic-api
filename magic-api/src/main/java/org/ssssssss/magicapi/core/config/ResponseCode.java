package org.ssssssss.magicapi.core.config;

/**
 * json结果code配置
 *
 * @author mxd
 * @since 1.1.2
 */
public class ResponseCode {

	/**
	 * 执行成功的code值
	 */
	private int success = 1;

	/**
	 * 参数验证未通过的code值
	 */
	private int invalid = 0;

	/**
	 * 执行出现异常的code值
	 */
	private int exception = -1;

	public int getSuccess() {
		return success;
	}

	public void setSuccess(int success) {
		this.success = success;
	}

	public int getInvalid() {
		return invalid;
	}

	public void setInvalid(int invalid) {
		this.invalid = invalid;
	}

	public int getException() {
		return exception;
	}

	public void setException(int exception) {
		this.exception = exception;
	}
}
