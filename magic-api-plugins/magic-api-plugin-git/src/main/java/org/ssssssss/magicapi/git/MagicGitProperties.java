package org.ssssssss.magicapi.git;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "magic-api.resource.git")
public class MagicGitProperties {
    /**
     * git仓库地址
     */
    private String url;
    /**
     * git分支
     */
    private String branch;
    /**
     * ssh 密钥地址
     * 仅支持-m PEM参数生产的ssh key
     */
    private String privateKey;
    /**
     * git账号
     */
    private String username;
    /**
     * git密码
     */
    private String password;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
