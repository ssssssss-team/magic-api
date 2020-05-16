package org.ssssssss.magicapi.interceptor;

import org.ssssssss.magicapi.context.RequestContext;

/**
 * 请求拦截器
 */
public interface RequestInterceptor {

    /**
     * 请求之前执行
     *
     * @return 当返回对象时，直接将此对象返回到页面，返回null时，继续执行后续操作
     * @throws Exception
     */
    default Object preHandle(RequestContext context) throws Exception {
        return null;
    }


    /**
     * 执行完毕之后执行
     * @param value 即将要返回到页面的值
     * @return 返回到页面的对象,当返回null时执行后续拦截器，否则直接返回该值，不执行后续拦截器
     * @throws Exception
     */
    default Object postHandle(RequestContext context, Object value) throws Exception {
        return null;
    }

}
