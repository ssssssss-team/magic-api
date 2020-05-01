package com.ssssssss.utils;

import com.ssssssss.enums.SqlMode;
import com.ssssssss.scripts.ForeachSqlNode;
import com.ssssssss.scripts.IfSqlNode;
import com.ssssssss.scripts.SqlNode;
import com.ssssssss.scripts.TextSqlNode;
import com.ssssssss.session.SqlStatement;
import com.ssssssss.session.XMLStatement;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * xml文件解析
 */
public class S8XMLFileParser {

    private static Logger logger = LoggerFactory.getLogger(S8XMLFileParser.class);

    private static final List<String> TAG_NAMES = Arrays.asList("select", "insert", "update", "delete");

    /**
     * 解析xml文件
     */
    public static XMLStatement parse(File file) {
        XMLStatement statement = null;
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
            //解析根节点
            statement = parseRoot(document);
            // 解析select/insert/update/delete节点
            for (String tagName : TAG_NAMES) {
                statement.addSqlStatement(parseSqlStatement(statement, document.getElementsByTagName(tagName)));
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            logger.error("解析S8XML文件出错", e);
        }
        return statement;
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
    private static List<SqlStatement> parseSqlStatement(XMLStatement xmlStatement, NodeList nodeList) {
        List<SqlStatement> sqlStatements = new ArrayList<>();
        for (int i = 0, len = nodeList.getLength(); i < len; i++) {
            Node item = nodeList.item(i);
            SqlStatement sqlStatement = new SqlStatement();
            sqlStatement.setXmlStatement(xmlStatement);
            String requestMapping = getNodeAttributeValue(item, "request-mapping");
            Assert.isNotBlank(requestMapping,"请求方法不能为空！");
            // 设置请求路径
            sqlStatement.setRequestMapping(StringUtils.defaultString(xmlStatement.getRequestMapping()) + requestMapping);
            // 设置请求方法
            sqlStatement.setRequestMethod(getNodeAttributeValue(item, "request-method"));
            String returnType = getNodeAttributeValue(item, "return-type");
            if("int".equalsIgnoreCase(returnType)){
                sqlStatement.setReturnType(Integer.class);
                sqlStatement.setSqlMode(SqlMode.SELECT_NUMBER);
            }else if("double".equalsIgnoreCase(returnType)){
                sqlStatement.setSqlMode(SqlMode.SELECT_NUMBER);
                sqlStatement.setReturnType(Double.class);
            }else if("long".equalsIgnoreCase(returnType)){
                sqlStatement.setSqlMode(SqlMode.SELECT_NUMBER);
                sqlStatement.setReturnType(Long.class);
            }else if("string".equalsIgnoreCase(returnType)){
                sqlStatement.setReturnType(String.class);
            }else if("map".equalsIgnoreCase(returnType)){
                sqlStatement.setSqlMode(SqlMode.SELECT_ONE);
            }else{
                sqlStatement.setSqlMode(SqlMode.SELECT_LIST);
                //设置是否是分页
                sqlStatement.setPagination("true".equalsIgnoreCase(getNodeAttributeValue(item,"page")));
            }
            SqlNode root = new TextSqlNode("");
            // 解析sql语句
            parseNodeList(root, item.getChildNodes());
            sqlStatement.setSqlNode(root);
            sqlStatements.add(sqlStatement);
        }
        return sqlStatements;
    }

    /**
     * 递归解析子节点
     */
    private static void parseNodeList(SqlNode sqlNode, NodeList nodeList) {
        for (int i = 0, len = nodeList.getLength(); i < len; i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                sqlNode.addChildNode(new TextSqlNode(node.getNodeValue().trim()));
            } else if(node.getNodeType() != Node.COMMENT_NODE){
                String nodeName = node.getNodeName();
                SqlNode childNode = null;
                if ("foreach".equals(nodeName)) {
                    childNode = parseForeachSqlNode(node);
                } else if ("if".equals(nodeName)) {
                    childNode = parseIfSqlNode(node);
                } else {
                    logger.error("不支持的标签:[{}]", nodeName);
                    return;
                }
                sqlNode.addChildNode(childNode);
                if (node.hasChildNodes()) {
                    parseNodeList(childNode, node.getChildNodes());
                }
            }
        }
    }

    /**
     * 获取节点属性
     *
     * @param node         节点
     * @param attributeKey 属性名
     * @return 节点属性值，未设置时返回null
     */
    private static String getNodeAttributeValue(Node node, String attributeKey) {
        Node item = node.getAttributes().getNamedItem(attributeKey);
        return item != null ? item.getNodeValue() : null;
    }

    /**
     * 解析foreach节点
     */
    private static ForeachSqlNode parseForeachSqlNode(Node node) {
        ForeachSqlNode foreachSqlNode = new ForeachSqlNode();
        foreachSqlNode.setCollection(getNodeAttributeValue(node, "collection"));
        foreachSqlNode.setSeparator(getNodeAttributeValue(node, "separator"));
        foreachSqlNode.setClose(getNodeAttributeValue(node, "close"));
        foreachSqlNode.setOpen(getNodeAttributeValue(node, "open"));
        foreachSqlNode.setItem(getNodeAttributeValue(node, "item"));
        return foreachSqlNode;
    }

    /**
     * 解析if节点
     */
    private static IfSqlNode parseIfSqlNode(Node node) {
        return new IfSqlNode(getNodeAttributeValue(node, "test"));
    }
}
