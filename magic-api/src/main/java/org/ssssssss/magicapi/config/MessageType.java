package org.ssssssss.magicapi.config;

/**
 * 消息类型
 */
public enum MessageType {
	/* S -> C message */
	/* 日志消息 */
	LOG,
	/* 进入断点 */
	BREAKPOINT,
	/* 请求接口发生异常 */
	EXCEPTION,
	/* 发送给客户端的sessionId */
	SESSION_ID,

	/* C -> S message */
	/* 设置断点 */
	SET_BREAKPOINT,
	/* 恢复断点 */
	RESUME_BREAKPOINT,
	/* 登录 */
	LOGIN
}
