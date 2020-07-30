package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.model.Page;
import org.ssssssss.script.MagicScriptContext;

/**
 * 分页对象提取接口
 */
public interface PageProvider {

    /**
     * 从请求中获取分页对象
     */
    public Page getPage(MagicScriptContext context);
}
