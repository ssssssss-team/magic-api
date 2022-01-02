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
	/* 通知客户端，有用户上线 */
	USER_LOGIN,
	/* 通知客户端，有用户下线 */
	USER_LOGOUT,
	/* 通知客户端，当前机器在线人数 */
	ONLINE_USERS,

	/* C -> S message */
	/* 设置断点 */
	SET_BREAKPOINT,
	/* 恢复断点 */
	RESUME_BREAKPOINT,
	/* 登录 */
	LOGIN,
	/* 获取当前在线用户 */
	GET_ONLINE
}
