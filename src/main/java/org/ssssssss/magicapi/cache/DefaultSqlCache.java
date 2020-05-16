package org.ssssssss.magicapi.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultSqlCache extends LinkedHashMap<String, DefaultSqlCache.ExpireNode<Object>> implements SqlCache {

    private String separator = ":";

    private int capacity;

    private long expire;

    public DefaultSqlCache(int capacity, long expire) {
        super((int) Math.ceil(capacity / 0.75) + 1, 0.75f, true);
        // 容量
        this.capacity = capacity;
        // 固定过期时间
        this.expire = expire;
    }

    @Override
    public void put(String name, String key, Object value) {
        // 封装成过期时间节点
        put(name + separator + key, new ExpireNode<>(System.currentTimeMillis() + this.expire, value));
    }

    @Override
    public Object get(String name, String key) {
        key = name + separator + key;
        ExpireNode<Object> expireNode = super.get(key);
        if (expireNode == null) {
            return null;
        }
        // 惰性删除过期的
        if (this.expire > -1L && expireNode.expire < System.currentTimeMillis()) {
            super.remove(key);
            return null;
        }
        return expireNode.value;
    }

    @Override
    public void delete(String name) {
        Iterator<Map.Entry<String, ExpireNode<Object>>> iterator = super.entrySet().iterator();
        String prefix = name + separator;
        // 清除所有key前缀为name + separator的缓存
        while (iterator.hasNext()) {
            Map.Entry<String, ExpireNode<Object>> entry = iterator.next();
            if (entry.getKey().startsWith(prefix)) {
                iterator.remove();
            }
        }
    }


    @Override
    protected boolean removeEldestEntry(Map.Entry<String, ExpireNode<Object>> eldest) {
        if (this.expire > -1L && size() > capacity) {
            clean();
        }
        // lru淘汰
        return size() > this.capacity;
    }

    /**
     * 清理已过期的数据
     */
    private void clean() {
        Iterator<Map.Entry<String, ExpireNode<Object>>> iterator = super.entrySet().iterator();
        long now = System.currentTimeMillis();
        while (iterator.hasNext()) {
            Map.Entry<String, ExpireNode<Object>> next = iterator.next();
            // 判断是否过期
            if (next.getValue().expire < now) {
                iterator.remove();
            }
        }
    }


    /**
     * 过期时间节点
     */
    static class ExpireNode<V> {
        long expire;
        Object value;

        public ExpireNode(long expire, Object value) {
            this.expire = expire;
            this.value = value;
        }
    }
}
