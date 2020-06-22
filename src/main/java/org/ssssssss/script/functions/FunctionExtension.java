package org.ssssssss.script.functions;

import java.util.function.Function;

public class FunctionExtension {

    public static Object select(Function<Object[],Object> function){
        Object sql = function.apply(new Object[0]);
        return "执行SQL:" + sql;
    }
    public static Object page(Function<Object[],Object> function){
        Object sql = function.apply(new Object[0]);
        return "执行分页SQL:" + sql;
    }
    public static Object page(Function<Object[],Object> function,int page,int size){
        Object sql = function.apply(new Object[0]);
        return "执行分页SQL("+page+","+size+"):" + sql;
    }
    public static Object selectInt(Function<Object[],Object> function){
        Object sql = function.apply(new Object[0]);
        return "执行selectInt:" + sql;
    }
    public static Object selectOne(Function<Object[],Object> function){
        Object sql = function.apply(new Object[0]);
        return "执行selectOne:" + sql;
    }
}
