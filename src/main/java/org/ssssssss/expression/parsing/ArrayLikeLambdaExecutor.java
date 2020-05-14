package org.ssssssss.expression.parsing;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ArrayLikeLambdaExecutor {

    public static final Set<String> SUPPORT_METHOD;
    public static final Map<String, Method> METHODS;


    static {
        Map<String, Method> temp = new HashMap<>();
        Set<String> set = new HashSet<>();
        addSupport(temp, set, "map");
        addSupport(temp, set, "filter");
        addSupport(temp, set, "reduce");
        addSupport(temp, set, "sort");
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
        Object result = toOriginType(arrayLike, results);
        if (result != null) {
            return result;
        }
        throw new RuntimeException("未实现");
    }

    private static Object toOriginType(Object arrayLike, List<Object> results) {
        if (arrayLike instanceof Collection) {
            return results;
        } else if (arrayLike.getClass().isArray()) {
            return results.toArray();
        } else if (arrayLike instanceof Iterator) {
            return results;
        } else if (arrayLike instanceof Enumeration) {
            return results;
        }
        return null;
    }

    private static List<Object> arrayLikeToList(Object arrayLike) {
        if (arrayLike instanceof Collection) {
            return new ArrayList<>((Collection<?>) arrayLike);
        } else if (arrayLike.getClass().isArray()) {
            List<Object> list = new ArrayList<>(Array.getLength(arrayLike));
            IntStream.range(0, Array.getLength(arrayLike)).forEach(i->list.add(Array.get(arrayLike, i)));
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
        throw new RuntimeException("未实现");
    }

    @SuppressWarnings("unchecked")
    public static Object sort(Object arrayLike, Object... arguments) {
        List<Object> results = null;
        MultipleArgumentsLambda mal = (MultipleArgumentsLambda) arguments[0];
        Function<Object[], Object> handler = mal.getHandler();
        List<Object> coll = arrayLikeToList(arrayLike);
        if (coll.isEmpty() || coll.size() == 1) {
            return toOriginType(arrayLike, coll);
        }
        coll.sort((o1, o2) -> {
            Object val = handler.apply(new Object[]{o1, o2});
            if (!(val instanceof Integer)) {
                throw new IllegalStateException("lambda 函数 sort 必须返回int类型结果");
            }
            return (Integer) val;
        });
        return toOriginType(arrayLike, coll);
    }


    @SuppressWarnings("unchecked")
    public static Object reduce(Object arrayLike, Object... arguments) {
        MultipleArgumentsLambda mal = (MultipleArgumentsLambda) arguments[0];
        Function<Object[], Object> handler = mal.getHandler();
        List<?> coll = arrayLikeToList(arrayLike);
        if (coll.isEmpty()) {
            return null;
        }
        if (coll.size() == 1) {
            return coll.get(0);
        }
        Object result = coll.get(0);
        for (int i = 1; i < coll.size(); i++) {
            result = handler.apply(new Object[]{result, coll.get(i)});
        }
        return result;
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


    public static class MultipleArgumentsLambda {
        private List<Ast.Expression> args;
        private Function<Object[], Object> handler;

        public MultipleArgumentsLambda(Function<Object[], Object> handler) {
            this.handler = handler;
        }
        public MultipleArgumentsLambda(List<Ast.Expression> args, Function<Object[], Object> handler) {
            this.args = args;
            this.handler = handler;
        }

        public List<Ast.Expression> getArgs() {
            return args;
        }

        public void setArgs(List<Ast.Expression> args) {
            this.args = args;
        }

        public Function<Object[], Object> getHandler() {
            return handler;
        }

        public void setHandler(Function<Object[], Object> handler) {
            this.handler = handler;
        }
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
