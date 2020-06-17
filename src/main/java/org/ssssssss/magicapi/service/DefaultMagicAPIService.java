package org.ssssssss.magicapi.service;

import org.apache.commons.beanutils.BeanUtils;
import org.ssssssss.magicapi.context.RequestContext;
import org.ssssssss.magicapi.exception.MagicAPIException;
import org.ssssssss.magicapi.executor.StatementExecutor;
import org.ssssssss.magicapi.expression.ExpressionEngine;
import org.ssssssss.magicapi.model.Page;
import org.ssssssss.magicapi.model.PageResult;
import org.ssssssss.magicapi.session.Configuration;
import org.ssssssss.magicapi.session.Statement;
import org.ssssssss.magicapi.utils.Assert;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultMagicAPIService implements MagicAPIService {

    private Configuration configuration;

    private StatementExecutor statementExecutor;

    private ExpressionEngine expressionEngine;

    public DefaultMagicAPIService(Configuration configuration, StatementExecutor statementExecutor, ExpressionEngine expressionEngine) {
        this.configuration = configuration;
        this.statementExecutor = statementExecutor;
        this.expressionEngine = expressionEngine;
    }

    private Statement getStatement(String statementId) {
        Statement statement = configuration.getStatementById(statementId);
        Assert.isNotNull(statement, String.format("找不到%s", statementId));
        return statement;
    }

    private RequestContext createRequestContext(Map<String, Object> params) {
        return new RequestContext(params, this.expressionEngine);
    }

    private RequestContext createRequestContext(Map<String, Object> params, Page page) {
        return new RequestContext(params, page, this.expressionEngine);
    }

    private <T> T convertToObject(Object source, Class<T> clazz) {
        try {
            if (source == null) {
                return null;
            }
            if (clazz.isAssignableFrom(String.class)) {
                return (T) source.toString();
            } else if (source instanceof Map) {
                T target = clazz.newInstance();
                Map<String, ? extends Object> sourceMap = (Map<String, ? extends Object>) source;
                BeanUtils.populate(target, sourceMap);
                return target;
            }
            return (T) source;
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
            throw new MagicAPIException("类型转换失败", e);
        }
    }


    @Override
    public Object execute(String statementId, Map<String, Object> params) {
        try {
            return this.statementExecutor.execute(getStatement(statementId), createRequestContext(params));
        } catch (SQLException | ClassNotFoundException e) {
            throw new MagicAPIException("执行出错", e);
        }
    }

    @Override
    public <T> T queryForObject(String statementId, Map<String, Object> params, Class<T> clazz) {
        try {
            Object object = this.statementExecutor.execute(getStatement(statementId), createRequestContext(params));
            return convertToObject(object, clazz);
        } catch (SQLException | ClassNotFoundException e) {
            throw new MagicAPIException("执行出错", e);
        }
    }

    @Override
    public <T> List<T> queryForList(String statementId, Map<String, Object> params, Class<T> clazz) {
        try {
            Object object = this.statementExecutor.execute(getStatement(statementId), createRequestContext(params));
            if (object == null) {
                return null;
            }
            Assert.isTrue(object instanceof List, String.format("%s返回值类型为%s,应为List", statementId, object.getClass()));
            return ((List<Object>) object).stream().map(item -> convertToObject(item, clazz)).collect(Collectors.toList());
        } catch (SQLException | ClassNotFoundException e) {
            throw new MagicAPIException("执行出错", e);
        }
    }

    @Override
    public <T> PageResult<T> queryForPage(String statementId, Page page, Map<String, Object> params, Class<T> clazz) {
        try {
            Object object = this.statementExecutor.execute(getStatement(statementId), createRequestContext(params, page));
            if (object == null) {
                return new PageResult<>(0, Collections.emptyList());
            }
            Assert.isTrue(object instanceof PageResult, String.format("%s返回值类型为%s,应为PageResult", statementId, object.getClass()));
            PageResult<Object> pageResult = (PageResult<Object>) object;
            return new PageResult<>(pageResult.getTotal(), pageResult.getList().stream().map(item -> convertToObject(item, clazz)).collect(Collectors.toList()));
        } catch (SQLException | ClassNotFoundException e) {
            throw new MagicAPIException("执行出错", e);
        }
    }

    @Override
    public int update(String statementId, Map<String, Object> params) {
        try {
            Object object = this.statementExecutor.execute(getStatement(statementId), createRequestContext(params));
            Assert.isTrue(object != null, String.format("%s返回值为null,应为Number", statementId));
            Assert.isTrue(object instanceof Number, String.format("%s返回值类型为%s,应为Number", statementId, object.getClass()));
            return ((Number) object).intValue();
        } catch (SQLException | ClassNotFoundException e) {
            throw new MagicAPIException("执行出错", e);
        }
    }
}
