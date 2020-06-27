package org.ssssssss.script.functions;

import org.ssssssss.script.exception.MagicScriptException;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

public class StreamExtension {

    private static Object toOriginType(Object target, List<Object> results) {
        if (target instanceof Collection) {
            return results;
        } else if (target.getClass().isArray()) {
            return results.toArray();
        } else if (target instanceof Iterator) {
            return results;
        } else if (target instanceof Enumeration) {
            return results;
        }
        return null;
    }

    private static List<Object> arrayLikeToList(Object arrayLike) {
        if (arrayLike instanceof Collection) {
            return new ArrayList<>((Collection<?>) arrayLike);
        } else if (arrayLike.getClass().isArray()) {
            List<Object> list = new ArrayList<>(Array.getLength(arrayLike));
            IntStream.range(0, Array.getLength(arrayLike)).forEach(i -> list.add(Array.get(arrayLike, i)));
            return list;
        } else if (arrayLike instanceof Iterator) {
            List<Object> list = new ArrayList<>();
            Iterator<Object> it = (Iterator<Object>) arrayLike;
            it.forEachRemaining(list::add);
            return list;
        } else if (arrayLike instanceof Enumeration) {
            Enumeration<Object> en = (Enumeration<Object>) arrayLike;
            return Collections.list(en);
        }
		throw new MagicScriptException("不支持的类型:" + arrayLike.getClass());
    }

    public static Object map(Object target, Function<Object[], Object> function) {
        List<Object> objects = arrayLikeToList(target);
        List<Object> results = new ArrayList<>(objects.size());
        for (int i = 0,len = objects.size(); i < len; i++) {
            Object object = objects.get(i);
            results.add(function.apply(new Object[]{object,i,len}));
        }
        return toOriginType(target, results);
    }

    public static Object filter(Object target, Function<Object[], Object> function){
        List<Object> objects = arrayLikeToList(target);
        List<Object> results = new ArrayList<>(objects.size());
        for (int i = 0,len = objects.size(); i < len; i++) {
            Object object = objects.get(i);
            if(Objects.equals(true,function.apply(new Object[]{object,i,len}))){
                results.add(object);
            }
        }
        return toOriginType(target, results);
    }
}
