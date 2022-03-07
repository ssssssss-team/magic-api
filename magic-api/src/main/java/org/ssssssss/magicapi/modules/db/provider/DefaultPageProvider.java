package org.ssssssss.magicapi.modules.db.provider;

import org.apache.commons.lang3.math.NumberUtils;
import org.ssssssss.magicapi.modules.db.model.Page;
import org.ssssssss.script.runtime.RuntimeContext;

import java.util.Objects;

/**
 * 分页对象默认提取接口
 */
public class DefaultPageProvider implements PageProvider {

	/**
	 * page参数名
	 */
	private final String pageName;

	/**
	 * pageSize参数名
	 */
	private final String pageSize;

	/**
	 * 默认分页大小
	 */
	private final long defaultPageSize;

	/**
	 * 默认页数
	 */
	private final long defaultPage;

	/**
	 * 最大页数
	 */
	private final long maxPageSize;

	public DefaultPageProvider(String pageName, String pageSize, long defaultPage, long defaultPageSize, long maxPageSize) {
		this.pageName = pageName;
		this.pageSize = pageSize;
		this.defaultPageSize = defaultPageSize;
		this.defaultPage = defaultPage;
		this.maxPageSize = maxPageSize;
	}


	@Override
	public Page getPage(RuntimeContext context) {
		// 改为从脚本中获取
		long page = NumberUtils.toLong(Objects.toString(context.eval(this.pageName), null), this.defaultPage);
		long pageSize = NumberUtils.toLong(Objects.toString(context.eval(this.pageSize), null), this.defaultPageSize);
		if(maxPageSize > 0){
			pageSize = Math.min(pageSize, this.maxPageSize);
		}
		// 计算limit以及offset
		return new Page(pageSize, (page - 1) * pageSize);

	}
}
