package org.ssssssss.utils;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.exception.S8Exception;

public class Assert {

    /**
     * 断言值不能为空
     */
    public static void isNotNull(Object value, String message) {
        if (value == null) {
            throw new S8Exception(message);
        }
    }

    /**
     * 验证值必须是true
     */
    public static void isTrue(boolean value, String message) {
        if (!value) {
            throw new S8Exception(message);
        }
    }


    /**
     * 断言值不能为空字符串
     */
    public static void isNotBlank(String value, String message) {
        if (StringUtils.isBlank(value)) {
            throw new S8Exception(message);
        }
    }

    /**
     * 断言值不能为空字符串
     */
    public static void isNotBlanks(String message, String... values) {
        if (values != null) {
            for (String value : values) {
                isNotBlank(value, message);
            }
        }
    }

}
