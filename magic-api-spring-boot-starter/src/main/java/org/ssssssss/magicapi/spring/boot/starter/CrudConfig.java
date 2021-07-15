package org.ssssssss.magicapi.spring.boot.starter;

/**
 * CRUD 配置
 * @author 冰点
 * @date 2021-7-15 09:26:17
 * @since 1.3.3
 */
public class CrudConfig {
	/**
	 * 逻辑删除列
	 */
	private String logicDeleteColumn="is_valid";
	/**
	 * 逻辑删除值
	 */
	private String logicDeleteValue="0";
	/**
	 * 是否控制并发插入
	 */
	private boolean isLimitParallel=false;

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

	public boolean isLimitParallel() {
		return isLimitParallel;
	}

	public void setLimitParallel(boolean limitParallel) {
		isLimitParallel = limitParallel;
	}
}
