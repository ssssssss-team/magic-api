package org.ssssssss.magicapi.config;

/**
 * 消息类型
 * */
public enum MessageType {
	/* S -> C message */
	/* 日志消息 */
	LOG,
	/* 进入断点 */
	BREAKPOINT,

	/* C -> S message */
	/* 设置断点 */
	SET_BREAKPOINT,
	/* 恢复断点 */
	RESUME_BREAKPOINT,
	/* 设置 Session ID */
	SET_SESSION_ID,
}
