package org.ssssssss.magicapi.modules.db.model;

import java.util.List;

/**
 * 分页执行结果
 *
 * @author mxd
 */
public class PageResult<T> {

	/**
	 * 总条数
	 */
	private long total;

	/**
	 * 数据项
	 */
	private List<T> list;

	public PageResult(long total, List<T> list) {
		this.total = total;
		this.list = list;
	}

	public PageResult() {
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public List<T> getList() {
		return list;
	}

	public void setList(List<T> list) {
		this.list = list;
	}
}
