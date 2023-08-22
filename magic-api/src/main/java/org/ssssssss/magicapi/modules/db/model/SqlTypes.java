package org.ssssssss.magicapi.modules.db.model;

import org.ssssssss.script.reflection.JavaReflection;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SqlTypes {

    private static final Map<String, Integer> SQL_TYPE_MAPPINGS;

    static {
        Field[] fields = Types.class.getFields();
        SQL_TYPE_MAPPINGS = Stream.of(fields)
                .collect(Collectors.toMap(field -> field.getName().toLowerCase(), field -> (Integer) JavaReflection.getFieldValue(Types.class, field)));
    }


    public static Integer getSqlType(String type){
        return SQL_TYPE_MAPPINGS.get(type.toLowerCase());
    }
}
