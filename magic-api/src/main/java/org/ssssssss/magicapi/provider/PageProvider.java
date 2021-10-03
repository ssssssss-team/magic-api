package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.model.Page;
import org.ssssssss.script.MagicScriptContext;

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
	public Page getPage(MagicScriptContext context);
}
