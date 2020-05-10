package org.ssssssss.provider;

public interface KeyProvider {

    /**
     * 策略名称，<select-key type="name"> 中的name值
     * @return
     */
    public String getName();

    /**
     * 获取主键值
     * @return
     */
    public Object getKey();
}
