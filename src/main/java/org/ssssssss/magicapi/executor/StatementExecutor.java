package org.ssssssss.magicapi.executor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.ssssssss.magicapi.context.RequestContext;
import org.ssssssss.magicapi.dialect.Dialect;
import org.ssssssss.magicapi.enums.SqlMode;
import org.ssssssss.magicapi.expression.interpreter.AbstractReflection;
import org.ssssssss.magicapi.model.Page;
import org.ssssssss.magicapi.model.PageResult;
import org.ssssssss.magicapi.provider.PageProvider;
import org.ssssssss.magicapi.session.*;
import org.ssssssss.magicapi.utils.Assert;
import org.ssssssss.magicapi.utils.DomUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.List;

/**
 * SqlStatement执行器
 */
public class StatementExecutor {

    private SqlExecutor sqlExecutor;

    /**
     * 分页提取器
     */
    private PageProvider pageProvider;

    private static Logger logger = LoggerFactory.getLogger(StatementExecutor.class);
    private ApplicationContext applicationContext;
    private Configuration configuration;

    public StatementExecutor(SqlExecutor sqlExecutor, PageProvider pageProvider, ApplicationContext applicationContext) {
        this.sqlExecutor = sqlExecutor;
        this.pageProvider = pageProvider;
        this.applicationContext = applicationContext;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 执行statement
     */
    public Object execute(Statement statement, RequestContext context) throws SQLException, ClassNotFoundException {
        if (statement instanceof SqlStatement) {
            return executeSqlStatement((SqlStatement) statement, context);
        } else if (statement instanceof FunctionStatement) {
            return executeFunctionStatement((FunctionStatement) statement, context);
        }
        return null;
    }

    private Object executeFunctionStatement(FunctionStatement functionStatement, RequestContext context) throws ClassNotFoundException, SQLException {
        NodeList nodeList = functionStatement.getNodeList();
        Object value = null;
        for (int i = 0, len = nodeList.getLength(); i < len; i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.COMMENT_NODE) {
                continue;
            }
            if ("java".equalsIgnoreCase(node.getNodeName())) {
                // 解析类名和方法名
                String className = DomUtils.getNodeAttributeValue(node, "class");
                Assert.isNotBlank(className, "class不能为空！");
                String method = DomUtils.getNodeAttributeValue(node, "method");
                Assert.isNotBlank(method, "method不能为空！");
                // 解析参数
                NodeList values = (NodeList) DomUtils.evaluate("value", node, XPathConstants.NODESET);
                Object[] args = new Object[0];
                if (values != null) {
                    // 取出参数值
                    args = new Object[values.getLength()];
                    for (int j = 0; j < args.length; j++) {
                        // 解析表达式
                        String expression = values.item(j).getTextContent();
                        if (StringUtils.isNotBlank(expression)) {
                            args[j] = context.evaluate(expression.trim());
                        }
                    }
                }
                // 调用java方法
                value = executeJava(className, method, args);
            } else if ("execute-sql".equalsIgnoreCase(node.getNodeName())) {
                String sqlId = DomUtils.getNodeAttributeValue(node, "id");
                Statement statement = configuration.getStatementById(sqlId);
                Assert.isNotNull(statement, String.format("找不到SQL:%s", sqlId));
                // 解析参数
                NodeList params = (NodeList) DomUtils.evaluate("param", node, XPathConstants.NODESET);
                if (params != null) {
                    for (int j = 0, l = params.getLength(); j < l; j++) {
                        Node param = params.item(j);
                        String paramName = DomUtils.getNodeAttributeValue(param, "name");
                        String paramValue = DomUtils.getNodeAttributeValue(param, "value");
                        Assert.isNotBlanks("execute-sql/param的参数名和值都不能为空", paramName, paramValue);
                        // 重新覆盖值
                        context.put(paramName, context.evaluate(paramValue));
                    }
                }
                //执行SQL
                value = executeSqlStatement((SqlStatement) statement, context);
            } else {
                logger.warn("不支持节点{}", node.getNodeName());
                continue;
            }
            // 设置返回值重置到context中
            String returnVal = DomUtils.getNodeAttributeValue(node, "return");
            if (StringUtils.isNotBlank(returnVal)) {
                context.put(returnVal, value);
            }
        }
        return value;
    }

    /**
     * 调用java方法
     *
     * @param className  类名
     * @param methodName 方法名
     * @param args       参数
     */
    private Object executeJava(String className, String methodName, Object... args) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(className);
        Object target = null;
        AbstractReflection reflection = AbstractReflection.getInstance();
        Method method = (Method) reflection.getMethod(clazz, methodName, args);
        Assert.isTrue(method != null, String.format("在%s中找不到方法%s", className, methodName));
        try {
            target = applicationContext.getBean(clazz);
        } catch (BeansException ignored) {
            Assert.isTrue(Modifier.isStatic(method.getModifiers()), String.format("%s不在spring容器中时%s必须是静态方法", className, methodName));
        }
        return reflection.callMethod(target, method, args);
    }

    private Object executeSqlStatement(SqlStatement sqlStatement, RequestContext context) throws SQLException {
        if (sqlStatement.isPagination()) {  //判断是否是分页语句
            // 获取要执行的SQL
            String sql = sqlStatement.getSqlNode().getSql(context).trim();
            // 从Request中提取Page对象
            Page page = pageProvider.getPage(context.getRequest());
            // 获取数据库方言
            Dialect dialect = sqlExecutor.getDialect(sqlStatement.getDataSourceName());
            PageResult<Object> pageResult = new PageResult<>();
            ExecuteSqlStatement statement = sqlStatement.buildExecuteSqlStatement(dialect.getCountSql(sql), context.getParameters());
            statement.setReturnType(Long.class);
            statement.setSqlMode(SqlMode.SELECT_ONE);
            // 获取总条数
            long total = (long) sqlExecutor.execute(statement);
            pageResult.setTotal(total);
            // 当条数>0时，执行查询语句，否则不查询以提高性能
            if (total > 0) {
                // 获取分页语句
                String pageSql = dialect.getPageSql(sql, context, page.getOffset(), page.getLimit());
                // 执行查询
                pageResult.setList((List) sqlExecutor.execute(sqlStatement.buildExecuteSqlStatement(pageSql, context.getParameters())));
            }
            return pageResult;
        } else if (SqlMode.INSERT_WITH_PK == sqlStatement.getSqlMode()) {   //插入返回主键
            return sqlExecutor.executeInsertWithPk(sqlStatement, context);
        } else {
            // 获取要执行的SQL
            String sql = sqlStatement.getSqlNode().getSql(context).trim();
            // 普通SQL执行
            return sqlExecutor.execute(sqlStatement.buildExecuteSqlStatement(sql, context.getParameters()));
        }
    }
}
