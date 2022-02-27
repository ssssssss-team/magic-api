package org.ssssssss.magicapi.modules.db.model;

/**
 * 分页对象
 *
 * @author mxd
 */
public class Page {

	private long limit;

	private long offset;

	public Page() {
	}

	public Page(long limit, long offset) {
		this.limit = limit;
		this.offset = offset;
	}

	public long getLimit() {
		return limit;
	}

	public void setLimit(long limit) {
		this.limit = limit;
	}

	public long getOffset() {
		return offset;
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}
}
