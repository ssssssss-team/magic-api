package org.ssssssss.magicapi.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.exception.MagicLoginException;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.interceptor.AuthorizationInterceptor;
import org.ssssssss.magicapi.interceptor.MagicUser;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 自定义多用户名密码登录，以及权限控制
 * #magic-api.ext.auth.enable=true
 * #magic-api.ext.auth.users.zhangsan=123456
 * #magic-api.ext.auth.denyOptions.zhangsan=DELETE
 * #magic-api.ext.auth.users.wangwu=123456
 * #magic-api.ext.auth.denyOptions.wangwu=DELETE
 * @author 冰点
 * @date 2021-5-11 17:17:52
 */

@Configuration
@ConditionalOnProperty(prefix = "magic-api.ext.auth", name = "enable", havingValue = "true", matchIfMissing = false)
@ConfigurationProperties(prefix = "magic-api.ext.auth")
public class CustomAuthorizationInterceptor implements AuthorizationInterceptor {
    private static final Logger log = LoggerFactory.getLogger(CustomAuthorizationInterceptor.class);
    /**
     * 加密因子
     */
    @Value("${magic-api.ext.auth.encryIndex:1}")
    private int encryIndex;
    /**
     * 用户信息
     */
    private Map<String, String> users;
    /**
     * 用户权限
     */
    private Map<String, String> denyOptions;


    public CustomAuthorizationInterceptor() {
        log.info("已启用多用户登录扩展，如需关闭请magic-api.ext.auth.enable=false");
    }

    /**
     * 配置是否需要登录
     */
    @Override
    public boolean requireLogin() {
        return true;
    }

    /**
     * 根据Token获取User
     */
    @Override
    public MagicUser getUserByToken(String token) throws MagicLoginException {
        String[] userInfo = getUserInfoByToken(token);
        MagicUser magicUser = new MagicUser(userInfo[0], userInfo[0], getToken(userInfo[0], userInfo[1]));
        if (users.containsKey(magicUser.getUsername()) && users.get(magicUser.getUsername()).equals(userInfo[1])) {
            return magicUser;
        }
        throw new MagicLoginException("token无效");
    }

    @Override
    public MagicUser login(String username, String password) throws MagicLoginException {
        // 根据实际情况进行修改。。
        if (users.containsKey(username) && users.get(username).equals(password)) {
            return new MagicUser(username, username, getToken(username, password));
        }
        throw new MagicLoginException("用户名或密码不正确");
    }

    /**
     * 验证是否有权限访问功能
     */
    @Override
    public boolean allowVisit(MagicUser magicUser, HttpServletRequest request, Authorization authorization) {
        String[] denyOption = denyOptions.get(magicUser.getUsername()).split(",");
        List<String> list = Arrays.asList(denyOption);
        return !list.contains(authorization.name());
    }

    public String getToken(String username, String password) throws MagicLoginException {
        String token = null;
        try {
            byte[] b = (username + ";" + password).getBytes("utf-8");
            for (int i = 0; i < b.length; i++) {
                b[i] += encryIndex;
            }
            token = new String(b);
            log.debug("本次登录token:[{}]", token);
        } catch (UnsupportedEncodingException e) {
            log.info("生成token失败,可能字符集不合法。[{}={}]",username,password);
            throw new MagicLoginException("用户名或密码配置不合法");
        }
        return token;
    }

    public String[] getUserInfoByToken(String token) throws MagicLoginException {
        try {
            byte[] b = token.getBytes();
            for (int i = 0; i < b.length; i++) {
                b[i] -= encryIndex;
            }
            return new String(b).split(";");
        } catch (Exception e) {
            log.error("根据token:[{}]获取用户信息失败", token, e);
            throw new MagicLoginException("用户名或密码不正确");
        }
    }

    public void setUsers(Map<String, String> users) {
        this.users = users;
    }

    public void setDenyOptions(Map<String, String> denyOptions) {
        this.denyOptions = denyOptions;
    }
}