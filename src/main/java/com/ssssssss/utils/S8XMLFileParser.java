package com.ssssssss.utils;

import com.ssssssss.enums.SqlMode;
import com.ssssssss.scripts.ForeachSqlNode;
import com.ssssssss.scripts.IfSqlNode;
import com.ssssssss.scripts.SqlNode;
import com.ssssssss.scripts.TextSqlNode;
import com.ssssssss.session.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * xml文件解析
 */
public class S8XMLFileParser {

    private static Logger logger = LoggerFactory.getLogger(S8XMLFileParser.class);

    private static final List<String> TAG_NAMES = Arrays.asList("select-list", "select-one", "insert", "update", "delete");

    /**
     * 解析xml文件
     */
    static XMLStatement parse(File file) {
        XMLStatement statement = null;
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            // 解析根节点
            statement = parseRoot(document);
            // 解析验证节点
            parseValidateStatement(document.getElementsByTagName("validate"), statement);
            // 解析select/insert/update/delete节点
            for (String tagName : TAG_NAMES) {
                statement.addStatement(parseSqlStatement(statement, tagName, document));
            }
            // 解析functionStatement
            statement.addStatement(parseFunctionStatement(statement, document.getElementsByTagName("function")));
        } catch (SAXException | IOException | ParserConfigurationException e) {
            logger.error("解析S8XML文件出错", e);
        }
        return statement;
    }

    private static List<Statement> parseFunctionStatement(XMLStatement statement, NodeList nodeList) {
        List<Statement> statements = new ArrayList<>();
        for (int i = 0, len = nodeList.getLength(); i < len; i++) {
            Node node = nodeList.item(i);
            FunctionStatement functionStatement = new FunctionStatement();
            // 设置请求路径
            functionStatement.setRequestMapping(DomUtils.getNodeAttributeValue(node, "request-mapping"));
            // 设置请求方法
            functionStatement.setRequestMethod(DomUtils.getNodeAttributeValue(node, "request-method"));
            // 设置ID
            functionStatement.setId(DomUtils.getNodeAttributeValue(node, "id"));

            // TODO 这里后续需要改进
            // 设置子节点，不进行深层解析，执行时在解析
            functionStatement.setNodeList((NodeList) DomUtils.evaluate("*", node, XPathConstants.NODESET));

            functionStatement.setXmlStatement(statement);
            statements.add(functionStatement);
        }
        return statements;
    }

    /**
     * 解析Validate节点
     */
    private static void parseValidateStatement(NodeList nodeList, XMLStatement xmlStatement) {
        for (int i = 0, len = nodeList.getLength(); i < len; i++) {
            Node node = nodeList.item(i);
            String id = DomUtils.getNodeAttributeValue(node, "id");
            Assert.isNotBlank(id, "validate节点必须要有id属性");
            String code = DomUtils.getNodeAttributeValue(node, "code");
            String message = DomUtils.getNodeAttributeValue(node, "message");
            message = StringUtils.isBlank(message) ? "参数校验失败" : message;
            xmlStatement.addValidateStatement(new ValidateStatement(id, NumberUtils.toInt(code, 0), message, (NodeList) DomUtils.evaluate("param", node, XPathConstants.NODESET)));
        }
    }

    /**
     * 解析根节点
     */
    private static XMLStatement parseRoot(Document document) {
        XMLStatement statement = new XMLStatement();
        //解析请求路径
        statement.setRequestMapping(document.getDocumentElement().getAttribute("request-mapping"));
        return statement;
    }

    /**
     * 解析节点
     */
    private static List<Statement> parseSqlStatement(XMLStatement xmlStatement, String tagName, Document document) {
        List<Statement> sqlStatements = new ArrayList<>();
        NodeList nodeList = document.getElementsByTagName(tagName);
        for (int i = 0, len = nodeList.getLength(); i < len; i++) {
            Node item = nodeList.item(i);
            SqlStatement sqlStatement = new SqlStatement();
            sqlStatement.setId(DomUtils.getNodeAttributeValue(item, "id"));
            sqlStatement.setXmlStatement(xmlStatement);
            String validate = DomUtils.getNodeAttributeValue(item, "validate");
            if (StringUtils.isNotBlank(validate)) {
                // 支持多个验证
                for (String validateId : validate.split(",")) {
                    Assert.isTrue(xmlStatement.containsValidateStatement(validateId), String.format("找不到验证节点[%s]", validateId));
                    sqlStatement.addValidate(validateId);
                }
            }
            // 设置SqlMode
            sqlStatement.setSqlMode(SqlMode.valueOf(item.getNodeName().toUpperCase().replace("-", "_")));

            String requestMapping = DomUtils.getNodeAttributeValue(item, "request-mapping");
            String id = DomUtils.getNodeAttributeValue(item, "id");
            if (StringUtils.isNotBlank(requestMapping)) {
                // 设置请求路径
                sqlStatement.setRequestMapping(StringUtils.defaultString(xmlStatement.getRequestMapping()) + requestMapping);
                // 设置请求方法
                sqlStatement.setRequestMethod(DomUtils.getNodeAttributeValue(item, "request-method"));
            }
            String returnType = DomUtils.getNodeAttributeValue(item, "return-type");
            if ("int".equalsIgnoreCase(returnType)) {
                sqlStatement.setReturnType(Integer.class);
            } else if ("double".equalsIgnoreCase(returnType)) {
                sqlStatement.setReturnType(Double.class);
            } else if ("long".equalsIgnoreCase(returnType)) {
                sqlStatement.setReturnType(Long.class);
            } else if ("string".equalsIgnoreCase(returnType)) {
                sqlStatement.setReturnType(String.class);
            } else if ("boolean".equalsIgnoreCase(returnType)) {
                sqlStatement.setReturnType(Boolean.class);
            } else {
                sqlStatement.setReturnType(Map.class);
            }
            if (SqlMode.SELECT_LIST == sqlStatement.getSqlMode()) {
                //设置是否是分页
                sqlStatement.setPagination("true".equalsIgnoreCase(DomUtils.getNodeAttributeValue(item, "page")));
            }
            SqlNode root = new TextSqlNode("");
            // 解析sql语句
            parseNodeList(root, document, item.getChildNodes());
            sqlStatement.setSqlNode(root);
            sqlStatements.add(sqlStatement);
        }
        return sqlStatements;
    }

    /**
     * 递归解析子节点
     */
    private static void parseNodeList(SqlNode sqlNode, Document document, NodeList nodeList) {
        for (int i = 0, len = nodeList.getLength(); i < len; i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                sqlNode.addChildNode(new TextSqlNode(node.getNodeValue().trim()));
            } else if (node.getNodeType() != Node.COMMENT_NODE) {
                String nodeName = node.getNodeName();
                SqlNode childNode;
                if ("foreach".equals(nodeName)) {
                    childNode = parseForeachSqlNode(node);
                } else if ("if".equals(nodeName)) {
                    childNode = parseIfSqlNode(node);
                } else if ("include".equalsIgnoreCase(nodeName)) {
                    String refId = DomUtils.getNodeAttributeValue(node, "refid");
                    Assert.isNotBlank(refId, "refid 不能为空！");
                    Node refSqlNode = (Node) DomUtils.evaluate(String.format("//sql[@id=\"%s\"]", refId), document, XPathConstants.NODE);
                    Assert.isNotNull(refSqlNode, "找不到sql[" + refId + "]");
                    childNode = new TextSqlNode(refSqlNode.getTextContent().trim());
                } else {
                    logger.error("不支持的标签:[{}]", nodeName);
                    return;
                }
                sqlNode.addChildNode(childNode);
                if (node.hasChildNodes()) {
                    parseNodeList(childNode, document, node.getChildNodes());
                }
            }
        }
    }

    /**
     * 解析foreach节点
     */
    private static ForeachSqlNode parseForeachSqlNode(Node node) {
        ForeachSqlNode foreachSqlNode = new ForeachSqlNode();
        foreachSqlNode.setCollection(DomUtils.getNodeAttributeValue(node, "collection"));
        foreachSqlNode.setSeparator(DomUtils.getNodeAttributeValue(node, "separator"));
        foreachSqlNode.setClose(DomUtils.getNodeAttributeValue(node, "close"));
        foreachSqlNode.setOpen(DomUtils.getNodeAttributeValue(node, "open"));
        foreachSqlNode.setItem(DomUtils.getNodeAttributeValue(node, "item"));
        return foreachSqlNode;
    }

    /**
     * 解析if节点
     */
    private static IfSqlNode parseIfSqlNode(Node node) {
        return new IfSqlNode(DomUtils.getNodeAttributeValue(node, "test"));
    }
}
