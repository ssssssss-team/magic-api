package org.ssssssss.magicapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.context.RequestContext;
import org.ssssssss.magicapi.model.JsonBean;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

public class RequestExecutor {

    private static Logger logger = LoggerFactory.getLogger(RequestExecutor.class);

    private List<RequestInterceptor> requestInterceptors = new ArrayList<>();

    public void addRequestInterceptor(RequestInterceptor requestInterceptor){
        this.requestInterceptors.add(requestInterceptor);
    }

    /**
     * http请求入口
     *
     * @param request
     * @return
     */
    @ResponseBody
    public Object invoke(HttpServletRequest request) {
        return invoke(request, null);
    }

    /**
     * http请求入口(带RequestBody)
     */
    @ResponseBody
    public Object invoke(HttpServletRequest request, @RequestBody(required = false) Object requestBody) {
        try {
            // 创建RequestContex对象，供后续使用
            RequestContext requestContext = new RequestContext(request);
            if (!requestContext.containsKey("body")) {
                requestContext.setRequestBody(requestBody);
            }
            // 执行前置拦截器
            for (RequestInterceptor requestInterceptor : requestInterceptors) {
                Object value = requestInterceptor.preHandle(requestContext);
                if (value != null) {
                    return value;
                }
            }
            Object value = null;
            // 执行后置拦截器
            for (RequestInterceptor requestInterceptor : requestInterceptors) {
                Object target = requestInterceptor.postHandle(requestContext, value);
                if (target != null) {
                    return target;
                }
            }
            return value;
        } catch (Exception e) {
            logger.error("系统出现错误", e);
            return new JsonBean<>(-1, e.getMessage());
        }
    }

}
