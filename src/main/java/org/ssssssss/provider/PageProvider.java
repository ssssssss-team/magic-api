package org.ssssssss.provider;

import org.ssssssss.model.Page;

import javax.servlet.http.HttpServletRequest;

/**
 * 分页对象提取接口
 */
public interface PageProvider {

    public Page getPage(HttpServletRequest request);
}
