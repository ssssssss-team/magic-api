package com.ssssssss.expression.parsing;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

public class ArrayLikeLambdaExecutor {

    public static final Set<String> SUPPORT_METHOD = new HashSet<>();
    public static final Map<String, Method> METHODS;


    static {
        Map<String, Method> temp = new HashMap<>();
        addSupport(temp, "map");
        METHODS = Collections.unmodifiableMap(temp);
    }

    private static void addSupport(Map<String, Method> temp, String name) {
        SUPPORT_METHOD.add(name);
        init(temp, name);
    }

    private static void init(Map<String, Method> initialMap, String name) {
        try {
            initialMap.put(name, ArrayLikeLambdaExecutor.class.getMethod(name, Object.class, Object[].class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Object map(Object arrayLike, Object... arguments) {
//        System.err.println("ArrayLikeLambdaExecutor:11 " + arrayLike);
        List<Object> results = null;
        Object argument = arguments[0];
        List<Object> args = (List<Object>) argument;
        results = new ArrayList<>(args.size());
        for (int j = 0; j < args.size(); j++) {
            Object result = ((Supplier) args.get(j)).get();
            results.add(result);
        }
        if (arrayLike instanceof Collection) {
            return results;
        } else if (arrayLike.getClass().isArray()) {
            return results.toArray();
        } else if (arrayLike instanceof Iterator) {

        } else if (arrayLike instanceof Enumeration) {

        }
        throw new RuntimeException("未实现");
    }
}
