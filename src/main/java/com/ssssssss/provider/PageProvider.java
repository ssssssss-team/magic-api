package com.ssssssss.provider;

import com.ssssssss.model.Page;

import javax.servlet.http.HttpServletRequest;

/**
 * 分页对象提取接口
 */
public interface PageProvider {

    public Page getPage(HttpServletRequest request);
}
