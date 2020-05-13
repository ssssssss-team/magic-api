package org.ssssssss.expression.parsing;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

public class ArrayLikeLambdaExecutor {

    public static final Set<String> SUPPORT_METHOD;
    public static final Map<String, Method> METHODS;


    static {
        Map<String, Method> temp = new HashMap<>();
        Set<String> set = new HashSet<>();
        addSupport(temp, set, "map");
        addSupport(temp, set, "filter");
//        addSupport(temp, set, "reduce");
        SUPPORT_METHOD = Collections.unmodifiableSet(set);
        METHODS = Collections.unmodifiableMap(temp);
    }

    private static void addSupport(Map<String, Method> temp, Set<String> set, String name) {
        set.add(name);
        addMethod(temp, name);
    }

    private static void addMethod(Map<String, Method> initialMap, String name) {
        try {
            initialMap.put(name, ArrayLikeLambdaExecutor.class.getMethod(name, Object.class, Object[].class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Object eachParse(Object arrayLike, Object argument, SPConsumer spConsumer) {
        List<Object> results = null;
        List<Object> args = (List<Object>) argument;
        results = new ArrayList<>(args.size());
        for (int j = 0; j < args.size(); j++) {
            SourceAndParsed<Object, Object> result = (SourceAndParsed<Object, Object>) ((Supplier) args.get(j)).get();
            spConsumer.accept(results, result);
        }
        if (arrayLike instanceof Collection) {
            return results;
        } else if (arrayLike.getClass().isArray()) {
            return results.toArray();
        } else if (arrayLike instanceof Iterator) {
            return results;
        } else if (arrayLike instanceof Enumeration) {
            return results;
        }
        throw new RuntimeException("未实现");
    }


    @SuppressWarnings("unchecked")
    public static Object reduce(Object arrayLike, Object... arguments) {
        eachParse(arrayLike, arguments[0], (list, sp) -> list.add(sp.getParsed()));
        System.out.println();
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Object map(Object arrayLike, Object... arguments) {
        return eachParse(arrayLike, arguments[0], (list, sp) -> list.add(sp.getParsed()));
    }

    @SuppressWarnings("unchecked")
    public static Object filter(Object arrayLike, Object... arguments) {
        return eachParse(arrayLike, arguments[0], (list, sp) -> {
            if (sp.getParsed() instanceof Boolean) {
                if ((Boolean)sp.getParsed()) {
                    list.add(sp.getSource());
                }
            } else {
                throw new RuntimeException("lambda函数filter的结果非布尔类型");
            }
        });
    }

    public interface SPConsumer {
        void accept(List<Object> list, SourceAndParsed<Object, Object> sp);
    }

    public static class SourceAndParsed<S, P> {
        private S source;
        private P parsed;

        public SourceAndParsed(S source, P parsed) {
            this.source = source;
            this.parsed = parsed;
        }

        public P getParsed() {
            return parsed;
        }

        public void setParsed(P parsed) {
            this.parsed = parsed;
        }

        public S getSource() {
            return source;
        }

        public void setSource(S source) {
            this.source = source;
        }
    }
}
