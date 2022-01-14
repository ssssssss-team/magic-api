package org.ssssssss.magicapi.modules.db.provider;

import org.ssssssss.magicapi.modules.db.model.Page;
import org.ssssssss.script.runtime.RuntimeContext;

/**
 * 分页对象提取接口
 *
 * @author mxd
 */
public interface PageProvider {

	/**
	 * 从请求中获取分页对象
	 *
	 * @param context 脚本上下文
	 * @return 返回分页对象
	 */
	public Page getPage(RuntimeContext context);
}
