package org.ssssssss.magicapi.modules.db.model;

/**
 * 单表API操作
 */
public enum SqlMode {

	/**
	 * 执行插入动作
	 */
	INSERT,
	/**
	 * 执行修改动作
	 */
	UPDATE,
	/**
	 * 执行删除动作
	 */
	DELETE,
	/**
	 * 执行查询操作
	 */
	SELECT,
	/**
	 * 执行查询单个操作
	 */
	SELECT_ONE,
	/**
	 * 执行分页查询动作
	 */
	PAGE,
	/**
	 * 执行count查询操作
	 */
	COUNT
}
