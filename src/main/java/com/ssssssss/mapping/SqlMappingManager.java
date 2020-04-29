package com.ssssssss.mapping;

import com.ssssssss.handler.RequestHandler;
import com.ssssssss.model.SqlMapping;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
public class SqlMappingManager {

    private static Logger logger = LoggerFactory.getLogger(SqlMappingManager.class);

    private Map<String, SqlMapping> sqlMaps = new HashMap<>();

    private Method requestInvokeMethod = RequestHandler.class.getDeclaredMethod("request", HttpServletRequest.class);

    @Autowired
    private RequestHandler requestInvokeHandler;

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    public SqlMappingManager() throws NoSuchMethodException {
    }


    public void register(String directory,File file){
        String mapping = convertToRequestMapping(directory, file.getPath());
        try {
            String sql = FileUtils.readFileToString(file,"UTF-8");
            boolean hasRegisted = sqlMaps.containsKey(mapping);
            sqlMaps.put(mapping,new SqlMapping(sql,mapping,file.getPath()));
            if(!hasRegisted){
                logger.info("注册{}",mapping);
                requestMappingHandlerMapping.registerMapping(RequestMappingInfo.paths(mapping).build(),requestInvokeHandler,requestInvokeMethod);
            }else{
                logger.info("刷新{}",mapping);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void unregister(String directory,File file){
        String mapping = convertToRequestMapping(directory, file.getPath());
        logger.info("取消注册{}",mapping);
        sqlMaps.remove(mapping);
        requestMappingHandlerMapping.unregisterMapping(RequestMappingInfo.paths(mapping).build());
    }

    public SqlMapping getSqlMapping(String mapping){
        return sqlMaps.get(mapping);
    }

    private String convertToRequestMapping(String dir,String file){
        String mapping = file.substring(dir.length());
        int index = mapping.lastIndexOf(".");
        if(index > -1){
            mapping = mapping.substring(0,index);
        }
        mapping = mapping.replace("\\","/");
        return mapping;
    }

}
