package org.ssssssss.magicapi.core.config;

/**
 * CRUD 配置
 *
 * @author 冰点
 * @date 2021-7-15 09:26:17
 * @since 1.3.4
 */
public class Crud {
	/**
	 * 逻辑删除列
	 */
	private String logicDeleteColumn = "is_valid";
	/**
	 * 逻辑删除值
	 */
	private String logicDeleteValue = "0";

	public String getLogicDeleteColumn() {
		return logicDeleteColumn;
	}

	public void setLogicDeleteColumn(String logicDeleteColumn) {
		this.logicDeleteColumn = logicDeleteColumn;
	}

	public String getLogicDeleteValue() {
		return logicDeleteValue;
	}

	public void setLogicDeleteValue(String logicDeleteValue) {
		this.logicDeleteValue = logicDeleteValue;
	}

}
