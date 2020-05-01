package com.ssssssss.utils;

import com.ssssssss.exception.S8Exception;
import org.apache.commons.lang3.StringUtils;

public class Assert {

    /**
     * 断言值不能为空
     */
    public static void isNotNull(Object value,String message){
        if(value == null){
            throw new S8Exception(message);
        }
    }


    /**
     * 断言值不能为空字符串
     */
    public static void isNotBlank(String value,String message){
        if(StringUtils.isBlank(value)){
            throw new S8Exception(message);
        }
    }

}
