package com.ssssssss.provider.impl;

import com.ssssssss.model.Page;
import com.ssssssss.provider.PageProvider;
import org.apache.commons.lang3.math.NumberUtils;

import javax.servlet.http.HttpServletRequest;

public class DefaultPageProvider implements PageProvider {

    private String pageName;

    private String pageSize;

    private long defaultPageSize = 10;

    private long defaultPage = 1;

    public DefaultPageProvider(String pageName, String pageSize) {
        this.pageName = pageName;
        this.pageSize = pageSize;
    }

    public DefaultPageProvider(String pageName, String pageSize, long defaultPageSize, long defaultPage) {
        this.pageName = pageName;
        this.pageSize = pageSize;
        this.defaultPageSize = defaultPageSize;
        this.defaultPage = defaultPage;
    }



    @Override
    public Page getPage(HttpServletRequest request) {
        long page = NumberUtils.toLong(request.getParameter(this.pageName), this.defaultPage);
        long pageSize = NumberUtils.toLong(request.getParameter(this.pageSize), this.defaultPageSize);
        return new Page((page - 1) * pageSize, pageSize);

    }
}
