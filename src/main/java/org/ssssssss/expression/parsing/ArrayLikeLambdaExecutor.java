package org.ssssssss.expression.parsing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ArrayLikeLambdaExecutor {



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
        }
        throw new RuntimeException("未实现");
    }
}
