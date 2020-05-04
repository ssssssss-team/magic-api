package com.ssssssss.executor;

import com.ssssssss.context.RequestContext;
import com.ssssssss.expression.ExpressionEngine;
import com.ssssssss.model.JsonBean;
import com.ssssssss.session.Configuration;
import com.ssssssss.session.Statement;
import com.ssssssss.session.ValidateStatement;
import com.ssssssss.session.XMLStatement;
import com.ssssssss.utils.Assert;
import com.ssssssss.utils.DomUtils;
import com.ssssssss.validator.IValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPathConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestExecutor {

    private Configuration configuration;

    private StatementExecutor statementExecutor;

    private ExpressionEngine expressionEngine;

    private static Logger logger = LoggerFactory.getLogger(RequestExecutor.class);
    private Map<String, IValidator> validators = new HashMap<>();

    public void addValidator(IValidator validator) {
        this.validators.put(validator.support(), validator);
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public void setStatementExecutor(StatementExecutor statementExecutor) {
        this.statementExecutor = statementExecutor;
    }

    public void setExpressionEngine(ExpressionEngine expressionEngine) {
        this.expressionEngine = expressionEngine;
    }

    /**
     * http请求入口
     */
    @ResponseBody
    public Object invoke(HttpServletRequest request, @RequestBody(required = false) Object requestBody) {
        try {
            NativeWebRequest webRequest = new ServletWebRequest(request);
            // 解析requestMapping
            String requestMapping = (String) webRequest.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            // 解析pathVariable
            Map<String, String> pathVariables = (Map<String, String>) webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            // 创建RequestContex对象，供后续使用
            RequestContext requestContext = new RequestContext(request, expressionEngine);
            if (!requestContext.containsKey("body")) {
                requestContext.put("body", requestBody);
            }
            if (pathVariables != null) {
                requestContext.putAll(pathVariables);
            }
            Statement statement = configuration.getStatement(requestMapping);
            // 执行校验
            Object value = validate(statement, requestContext);
            if (value != null) {
                return value;
            }
            // 执行SQL
            value = statementExecutor.execute(statement, requestContext);
            return new JsonBean<>(value);
        } catch (Exception e) {
            logger.error("系统出现错误", e);
            return new JsonBean<>(-1, e.getMessage());
        }
    }

    /**
     * 验证节点
     *
     * @param statement
     * @param requestContext
     * @return
     */
    private JsonBean<Void> validate(Statement statement, RequestContext requestContext) {
        List<String> validates = statement.getValidates();
        XMLStatement xmlStatement = statement.getXmlStatement();
        for (String validateId : validates) {
            ValidateStatement validateStatement = xmlStatement.getValidateStatement(validateId);
            NodeList nodeList = validateStatement.getNodes();
            for (int i = 0, len = nodeList.getLength(); i < len; i++) {
                Node node = nodeList.item(i);
                // 获取name值
                String name = DomUtils.getNodeAttributeValue(node, "name");
                Object value = null;
                // 如果name值填了，则取其表达式值
                if (StringUtils.isNotBlank(name)) {
                    value = requestContext.evaluate(name);
                }
                NodeList ruleList = (NodeList) DomUtils.evaluate("*", node, XPathConstants.NODESET);
                for (int j = 0, l = ruleList.getLength(); j < l; j++) {
                    Node rule = ruleList.item(j);
                    // 如果验证失败，返回自定义code
                    String nodeName = rule.getNodeName();
                    IValidator validator = validators.get(nodeName);
                    Assert.isNotNull(validator, String.format("找不到验证器:%s", nodeName));
                    if (!validator.validate(value, rule)) {
                        // rule->param->validate
                        int defaultCode = NumberUtils.toInt(DomUtils.getNodeAttributeValue(node, "code"), validateStatement.getCode());
                        int code = NumberUtils.toInt(DomUtils.getNodeAttributeValue(rule, "code"), defaultCode);
                        String message = rule.getTextContent();
                        if (StringUtils.isNotBlank(message)) {
                            message = message.trim();
                        } else {
                            message = DomUtils.getNodeAttributeValue(node, "message");
                            if (StringUtils.isNotBlank(message)) {
                                message = message.trim();
                            } else {
                                message = validateStatement.getMessage();
                            }
                        }
                        return new JsonBean<>(code, message);
                    }
                }
            }
        }
        // 验证通过
        return null;
    }
}
