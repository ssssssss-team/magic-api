package com.ssssssss.provider;

import com.ssssssss.model.Page;

import javax.servlet.http.HttpServletRequest;

public interface PageProvider {

    public Page getPage(HttpServletRequest request);
}
