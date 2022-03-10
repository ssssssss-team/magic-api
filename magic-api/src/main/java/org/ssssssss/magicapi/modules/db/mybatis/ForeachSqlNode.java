package org.ssssssss.magicapi.modules.db.mybatis;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.magicapi.utils.ScriptManager;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 对应XML中 <foreach>
 *
 * @author jmxd
 * @version : 2020-05-18
 */
public class ForeachSqlNode extends SqlNode {
	/**
	 * 数据集合，支持Collection、数组
	 */
	private String collection;
	/**
	 * item 变量名
	 */
	private String item;
	/**
	 * 拼接起始SQL
	 */
	private String open;
	/**
	 * 拼接结束SQL
	 */
	private String close;
	/**
	 * 分隔符
	 */
	private String separator;

	/**
	 * 序号
	 */
	private String index;

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public void setItem(String item) {
		this.item = item;
	}

	public void setOpen(String open) {
		this.open = open;
	}

	public void setClose(String close) {
		this.close = close;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	@Override
	public String getSql(Map<String, Object> paramMap, List<Object> parameters) {
		// 提取集合
		Object value = ScriptManager.executeExpression(this.collection, paramMap);
		// 如果集合为空，则过滤该节点
		if (value == null) {
			return "";
		}
		// 如果集合是Collection对象或其子类，则转成数组
		if (value instanceof Collection) {
			value = ((Collection) value).toArray();
		}
		// 判断不是数组，则过滤子节点并返回
		if (!value.getClass().isArray()) {
			return "";
		}
		// 开始拼接SQL,
		StringBuilder sqlBuilder = new StringBuilder(StringUtils.defaultString(this.open));
		boolean hasIndex = index != null && index.length() > 0;
		// 获取数组长度
		int len = Array.getLength(value);
		for (int i = 0; i < len; i++) {
			// 存入item对象
			paramMap.put(this.item, Array.get(value, i));
			if (hasIndex) {
				paramMap.put(this.index, i);
			}
			// 拼接子节点
			sqlBuilder.append(executeChildren(paramMap, parameters));
			// 拼接分隔符
			if (i + 1 < len) {
				sqlBuilder.append(StringUtils.defaultString(this.separator));
			}
		}
		// 拼接结束SQL
		sqlBuilder.append(StringUtils.defaultString(this.close));
		return sqlBuilder.toString();
	}
}
