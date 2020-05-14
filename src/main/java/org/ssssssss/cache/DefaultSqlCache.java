package org.ssssssss.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultSqlCache implements SqlCache {

    private String separator = ":";

    private LinkedHashMap<String, Object> cached;

    public DefaultSqlCache(int maxSize) {
        this.cached = new LinkedHashMap<String, Object>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
                return size() > maxSize;
            }
        };
    }
    @Override
    public void put(String name, String key, Object value) {
        this.cached.put(name + separator + key,value);
    }
    @Override
    public Object get(String name, String key) {
        return cached.get(name + separator + key);
    }
    @Override
    public void remove(String name) {
        Iterator<Map.Entry<String, Object>> iterator = cached.entrySet().iterator();
        String prefix = name + separator;
        while(iterator.hasNext()){
            Map.Entry<String, Object> entry = iterator.next();
            if(entry.getKey().startsWith(prefix)){
                iterator.remove();
            }
        }
    }
}
