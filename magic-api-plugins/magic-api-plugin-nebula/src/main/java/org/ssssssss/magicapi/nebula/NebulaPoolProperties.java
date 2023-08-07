package org.ssssssss.magicapi.nebula;

import com.vesoft.nebula.client.graph.data.SSLParam;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "nebula")
public class NebulaPoolProperties {

    /** nebula 服务地址, 多个则逗号分割, 格式为 ip:port */
    private String hostAddress;
    /** nebula 用户名 */
    private String userName;
    /** nebula 密码 */
    private String password;

    private boolean reconnect = true;
    /** nebula 连接池最小连接数 */
    private int minConnsSize = 0;
    /** nebula 连接池最大连接数 */
    private int maxConnsSize = 10;
    /** nebula 连接池最大等待时间 */
    private int timeout = 0;
    /** nebula 连接池空闲时间 */
    private int idleTime = 0;
    /** nebula 连接池心跳间隔 */
    private int intervalIdle = -1;

    private int waitTime = 0;

    private double minClusterHealthRate = 1.0;

    private boolean enableSsl = false;

    private SSLParam sslParam = null;

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }

    public int getMinConnsSize() {
        return minConnsSize;
    }

    public void setMinConnsSize(int minConnsSize) {
        this.minConnsSize = minConnsSize;
    }

    public int getMaxConnsSize() {
        return maxConnsSize;
    }

    public void setMaxConnsSize(int maxConnsSize) {
        this.maxConnsSize = maxConnsSize;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(int idleTime) {
        this.idleTime = idleTime;
    }

    public int getIntervalIdle() {
        return intervalIdle;
    }

    public void setIntervalIdle(int intervalIdle) {
        this.intervalIdle = intervalIdle;
    }

    public int getWaitTime() {
        return waitTime;
    }

    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }

    public double getMinClusterHealthRate() {
        return minClusterHealthRate;
    }

    public void setMinClusterHealthRate(double minClusterHealthRate) {
        this.minClusterHealthRate = minClusterHealthRate;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public void setEnableSsl(boolean enableSsl) {
        this.enableSsl = enableSsl;
    }

    public SSLParam getSslParam() {
        return sslParam;
    }

    public void setSslParam(SSLParam sslParam) {
        this.sslParam = sslParam;
    }
}
