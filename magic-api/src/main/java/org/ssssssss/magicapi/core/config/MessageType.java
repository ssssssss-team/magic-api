package org.ssssssss.magicapi.core.config;

/**
 * 消息类型
 */
public enum MessageType {
	/* S -> C message */
	/* 日志消息 */
	LOG,
	/* 多个日志消息 */
	LOGS,
	/* 进入断点 */
	BREAKPOINT,
	/* 请求接口发生异常 */
	EXCEPTION,
	/* 登录结果 */
	LOGIN_RESPONSE,
	/* 通知客户端，有用户上线 */
	USER_LOGIN,
	/* 通知客户端，有用户下线 */
	USER_LOGOUT,
	/* 通知客户端，当前机器在线人数 */
	ONLINE_USERS,
	/* 通知客户端，他人进入文件*/
	INTO_FILE_ID,
	/* ping */
	PING,


	/* C -> S message */
	/* 设置断点 */
	SET_BREAKPOINT,
	/* 恢复断点 */
	RESUME_BREAKPOINT,
	/* 登录 */
	LOGIN,
	/* 设置当前所在文件 */
	SET_FILE_ID,
	/* PONG */
	PONG,


	/* S <-> S -> C message*/
	/* 获取当前在线用户 */
	SEND_ONLINE

}
