package org.ssssssss.magicapi.modules.db.table;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.functions.StreamExtension;
import org.ssssssss.script.runtime.RuntimeContext;

import java.beans.Transient;
import java.util.*;
import java.util.function.Function;

/**
 * 单表API的Where
 *
 * @author mxd
 */
public class Where {

	private final List<String> tokens = new ArrayList<>();

	private final List<Object> params = new ArrayList<>();

	private final NamedTable namedTable;

	private final boolean needWhere;

	private boolean notNull = false;

	private boolean notBlank = false;

	public Where(NamedTable namedTable) {
		this(namedTable, true);
	}

	public Where(NamedTable namedTable, boolean needWhere) {
		this.namedTable = namedTable;
		this.needWhere = needWhere;
	}

	@Override
	@Comment("克隆")
	public Where clone() {
		Where where = new Where(this.namedTable, this.needWhere);
		where.tokens.addAll(this.tokens);
		where.params.addAll(this.params);
		where.notNull = this.notNull;
		where.notBlank = this.notBlank;
		return where;
	}

	@Comment("过滤`null`的参数")
	public Where notNull() {
		return notNull(true);
	}

	@Comment("过滤`blank`的参数")
	public Where notBlank() {
		return notBlank(true);
	}

	@Comment("是否过滤`null`的参数")
	public Where notNull(boolean flag) {
		this.notNull = flag;
		return this;
	}

	@Comment("是否过滤`blank`的参数")
	public Where notBlank(boolean flag) {
		this.notBlank = flag;
		return this;
	}

	@Comment("等于`=`,如：`eq('name', '老王') ---> name = '老王'`")
	public Where eq(@Comment(name = "column", value = "数据库中的列名") String column,
					@Comment(name = "value", value = "值") Object value) {
		return eq(true, column, value);
	}

	@Comment("等于`=`,如：`eq('name', '老王') ---> name = '老王'`")
	public Where eq(@Comment(name = "condition", value = "判断表达式，当为true时拼接条件") boolean condition,
					@Comment(name = "column", value = "数据库中的列名") String column,
					@Comment(name = "value", value = "值") Object value) {
		if (condition && filterNullAndBlank(value)) {
			tokens.add(namedTable.rowMapColumnMapper.apply(column));
			if (value == null) {
				append(" is null");
			} else {
				params.add(value);
				append(" = ?");
			}
			appendAnd();
		}
		return this;
	}

	@Comment("不等于`<>`,如：`ne('name', '老王') ---> name <> '老王'`")
	public Where ne(@Comment(name = "column", value = "数据库中的列名") String column,
					@Comment(name = "value", value = "值") Object value) {
		return ne(true, column, value);
	}

	@Comment("不等于`<>`,如：`ne('name', '老王') ---> name <> '老王'`")
	public Where ne(@Comment(name = "condition", value = "判断表达式，当为true时拼接条件") boolean condition,
					@Comment(name = "column", value = "数据库中的列名") String column,
					@Comment(name = "value", value = "值") Object value) {
		if (condition && filterNullAndBlank(value)) {
			append(namedTable.rowMapColumnMapper.apply(column));
			if (value == null) {
				append("is not null");
			} else {
				params.add(value);
				append("<> ?");
			}
			appendAnd();
		}
		return this;
	}

	@Comment("小于`<`,如：`lt('age', 18) ---> age < 18")
	public Where lt(@Comment(name = "column", value = "数据库中的列名") String column,
					@Comment(name = "value", value = "值") Object value) {
		return lt(true, column, value);
	}

	@Comment("小于`<`,如：`lt('age', 18) ---> age < 18")
	public Where lt(@Comment(name = "condition", value = "判断表达式，当为true时拼接条件") boolean condition,
					@Comment(name = "column", value = "数据库中的列名") String column,
					@Comment(name = "value", value = "值") Object value) {
		return append(condition, column, " < ?", value);
	}

	@Comment("小于等于`<=`,如：`lte('age', 18) ---> age <= 18")
	public Where lte(@Comment(name = "column", value = "数据库中的列名") String column,
					 @Comment(name = "value", value = "值") Object value) {
		return lte(true, column, value);
	}

	@Comment("小于等于`<=`,如：`lte('age', 18) ---> age <= 18")
	public Where lte(@Comment(name = "condition", value = "判断表达式，当为true时拼接条件") boolean condition,
					 @Comment(name = "column", value = "数据库中的列名") String column,
					 @Comment(name = "value", value = "值") Object value) {
		return append(condition, column, " <= ?", value);
	}

	@Comment("大于`>`,如：`get('age', 18) ---> age > 18")
	public Where gt(@Comment(name = "column", value = "数据库中的列名") String column,
					@Comment(name = "value", value = "值") Object value) {
		return gt(true, column, value);
	}

	@Comment("大于`>`,如：`get('age', 18) ---> age > 18")
	public Where gt(@Comment(name = "condition", value = "判断表达式，当为true时拼接条件") boolean condition,
					@Comment(name = "column", value = "数据库中的列名") String column,
					@Comment(name = "value", value = "值") Object value) {
		return append(condition, column, " > ?", value);
	}

	@Comment("大于等于`>=`,如：`get('age', 18) ---> age >= 18")
	public Where gte(@Comment(name = "column", value = "数据库中的列名") String column,
					 @Comment(name = "value", value = "值") Object value) {
		return gte(true, column, value);
	}

	@Comment("大于等于`>=`,如：`get('age', 18) ---> age >= 18")
	public Where gte(@Comment(name = "condition", value = "判断表达式，当为true时拼接条件") boolean condition,
					 @Comment(name = "column", value = "数据库中的列名") String column,
					 @Comment(name = "value", value = "值") Object value) {
		return append(condition, column, " >= ?", value);
	}

	@Comment("`in`,如：`in('age', [1,2,3]) ---> age in (1,2,3)")
	public Where in(@Comment(name = "column", value = "数据库中的列名") String column,
					@Comment(name = "value", value = "值") Object value) {
		return in(true, column, value);
	}

	@Comment("`in`,如：`in('age', [1,2,3]) ---> age in (1,2,3)")
	public Where in(@Comment(name = "condition", value = "判断表达式，当为true时拼接条件") boolean condition,
					@Comment(name = "column", value = "数据库中的列名") String column,
					@Comment(name = "value", value = "值") Object value) {
		if (condition && value != null) {
			List<Object> objects = StreamExtension.arrayLikeToList(value);
			if (objects.size() > 0) {
				append(namedTable.rowMapColumnMapper.apply(column));
				append(" in (");
				append(String.join(",", Collections.nCopies(objects.size(), "?")));
				append(")");
				appendAnd();
				params.addAll(objects);
			}
		}
		return this;
	}

	@Comment("`not in`,如：`notIn('age', [1,2,3]) ---> age not in (1,2,3)")
	public Where notIn(@Comment(name = "column", value = "数据库中的列名") String column,
					   @Comment(name = "value", value = "值") Object value) {
		return notIn(true, column, value);
	}

	@Comment("`not in`,如：`notIn('age', [1,2,3]) ---> age not in (1,2,3)")
	public Where notIn(@Comment(name = "condition", value = "判断表达式，当为true时拼接条件") boolean condition,
					   @Comment(name = "column", value = "数据库中的列名") String column,
					   @Comment(name = "value", value = "值") Object value) {
		if (condition && value != null) {
			List<Object> objects = StreamExtension.arrayLikeToList(value);
			if (objects.size() > 0) {
				append(namedTable.rowMapColumnMapper.apply(column));
				append("not in (");
				append(String.join(",", Collections.nCopies(objects.size(), "?")));
				append(")");
				appendAnd();
				params.addAll(objects);
			}
		}
		return this;
	}

	@Comment("`like`,如：`like('name', '%王%') ---> name like '%王%'")
	public Where like(@Comment(name = "column", value = "数据库中的列名") String column,
					  @Comment(name = "value", value = "值") Object value) {
		return like(true, column, value);
	}

	@Comment("`like`,如：`like('name', '%王%') ---> name like '%王%'")
	public Where like(@Comment(name = "condition", value = "判断表达式，当为true时拼接条件") boolean condition,
					  @Comment(name = "column", value = "数据库中的列名") String column,
					  @Comment(name = "value", value = "值") Object value) {
		return append(condition, column, "like ?", value);
	}

	@Comment("`not like`,如：`notLike('name', '%王%') ---> name not like '%王%'")
	public Where notLike(@Comment(name = "column", value = "数据库中的列名") String column,
						 @Comment(name = "value", value = "值") Object value) {
		return notLike(true, column, value);
	}

	@Comment("`not like` ,如：`notLike('name', '%王%') ---> name not like '%王%'")
	public Where notLike(@Comment(name = "condition", value = "判断表达式，当为true时拼接条件") boolean condition,
						 @Comment(name = "column", value = "数据库中的列名") String column,
						 @Comment(name = "value", value = "值") Object value) {
		return append(condition, column, "not like ?", value);
	}

	@Comment("`is null`,如：`isNull('name') ---> name is null")
	public Where isNull(@Comment(name = "column", value = "数据库中的列名") String column) {
		return isNull(true, column);
	}

	@Comment("`is null`,如：`isNull('name') ---> name is null")
	public Where isNull(@Comment(name = "condition", value = "判断表达式，当为true时拼接条件") boolean condition,
						@Comment(name = "column", value = "数据库中的列名") String column) {
		if (condition) {
			append(namedTable.rowMapColumnMapper.apply(column));
			append("is null");
			appendAnd();
		}
		return this;
	}

	@Comment("`is not null`,如：`isNotNull('name') ---> name is not null")
	public Where isNotNull(@Comment(name = "column", value = "数据库中的列名") String column) {
		return isNotNull(true, column);
	}

	@Comment("`is not null`,如：`isNotNull('name') ---> name is not null")
	public Where isNotNull(@Comment(name = "condition", value = "判断表达式，当为true时拼接条件") boolean condition,
						   @Comment(name = "column", value = "数据库中的列名") String column) {
		if (condition) {
			append(namedTable.rowMapColumnMapper.apply(column));
			append("is not null");
			appendAnd();
		}
		return this;
	}

	@Comment("拼接`or`")
	public Where or() {
		appendOr();
		return this;
	}

	@Comment("拼接`and`")
	public Where and() {
		appendAnd();
		return this;
	}

	@Comment("`and`嵌套，如and(it => it.eq('name','李白').ne('status','正常') --> and (name = '李白' and status <> '正常')")
	public Where and(@Comment(name = "function", value = "回调函数") Function<Object[], Where> function) {
		return and(true, function);
	}

	@Comment("`and`嵌套，如and(it => it.eq('name','李白').ne('status','正常') --> and (name = '李白' and status <> '正常')")
	public Where and(@Comment(name = "condition", value = "判断表达式，当为true时拼接条件") boolean condition,
					 @Comment(name = "function", value = "回调函数") Function<Object[], Where> function) {
		if (condition) {
			Where expr = function.apply(new Object[]{new Where(this.namedTable, false)});
			this.params.addAll(expr.params);
			append("(");
			append(expr.getSql());
			append(")");
			appendAnd();
		}
		return this;
	}

	@Comment("拼接`order by xxx asc/desc`")
	public Where orderBy(@Comment(name = "column", value = "要排序的列") String column,
						 @Comment(name = "sort", value = "`asc`或`desc`") String sort) {
		this.namedTable.orderBy(column, sort);
		return this;
	}

	@Comment("拼接`order by xxx asc`")
	public Where orderBy(@Comment(name = "column", value = "要排序的列") String column) {
		return orderBy(column, "asc");
	}

	@Comment("拼接`order by xxx desc`")
	public Where orderByDesc(@Comment(name = "column", value = "要排序的列") String column) {
		return orderBy(column, "desc");
	}

	@Comment("拼接`group by`")
	public Where groupBy(@Comment("要分组的列") String... columns) {
		this.namedTable.groupBy(columns);
		return this;
	}

	@Comment("保存到表中，当主键有值时则修改，否则插入")
	public Object save(RuntimeContext runtimeContext) {
		return namedTable.save(runtimeContext);
	}

	@Comment("保存到表中，当主键有值时则修改，否则插入")
	public Object save(RuntimeContext runtimeContext,
					   @Comment(name = "beforeQuery", value = "是否根据id查询有没有数据") boolean beforeQuery) {
		return namedTable.save(runtimeContext, beforeQuery);
	}

	@Comment("保存到表中，当主键有值时则修改，否则插入")
	public Object save(RuntimeContext runtimeContext,
					   @Comment(name = "data", value = "各项列和值") Map<String, Object> data) {
		return namedTable.save(runtimeContext, data);
	}

	@Comment("保存到表中，当主键有值时则修改，否则插入")
	public Object save(RuntimeContext runtimeContext,
					   @Comment(name = "data", value = "各项列和值") Map<String, Object> data,
					   @Comment(name = "beforeQuery", value = "是否根据id查询有没有数据") boolean beforeQuery) {
		return namedTable.save(runtimeContext, data, beforeQuery);
	}

	@Comment("执行插入语句，返回主键")
	public Object insert(RuntimeContext runtimeContext) {
		return namedTable.insert(runtimeContext);
	}

	@Comment("执行插入语句，返回主键")
	public Object insert(RuntimeContext runtimeContext,
						 @Comment(name = "data", value = "各项列和值") Map<String, Object> data) {
		return namedTable.insert(runtimeContext, data);
	}

	@Comment("执行update语句")
	public int update(RuntimeContext runtimeContext) {
		return namedTable.update(runtimeContext);
	}

	@Comment("执行delete语句")
	public int delete(RuntimeContext runtimeContext) {
		return namedTable.delete(runtimeContext);
	}

	@Comment("执行update语句")
	public int update(RuntimeContext runtimeContext,
					  @Comment(name = "data", value = "各项列和值") Map<String, Object> data) {
		return namedTable.update(runtimeContext, data);
	}

	@Comment("执行update语句")
	public int update(RuntimeContext runtimeContext,
					  @Comment(name = "data", value = "各项列和值") Map<String, Object> data,
					  @Comment(name = "isUpdateBlank", value = "是否更新空值字段") boolean isUpdateBlank) {
		return namedTable.update(runtimeContext, data, isUpdateBlank);
	}

	@Comment("执行分页查询")
	public Object page(RuntimeContext runtimeContext) {
		return namedTable.page(runtimeContext);
	}

	@Comment("执行分页查询，分页条件手动传入")
	public Object page(RuntimeContext runtimeContext,
					   @Comment(name = "limit", value = "限制条数") long limit,
					   @Comment(name = "offset", value = "跳过条数") long offset) {
		return namedTable.page(runtimeContext, limit, offset);
	}

	@Comment("执行select查询")
	public List<Map<String, Object>> select(RuntimeContext runtimeContext) {
		return namedTable.select(runtimeContext);
	}

	@Comment("执行selectOne查询")
	public Map<String, Object> selectOne(RuntimeContext runtimeContext) {
		return namedTable.selectOne(runtimeContext);
	}

	@Comment("查询条数")
	public int count(RuntimeContext runtimeContext) {
		return namedTable.count(runtimeContext);
	}

	@Comment("查询是否存在")
	public boolean exists(RuntimeContext runtimeContext) {
		return namedTable.exists(runtimeContext);
	}

	@Transient
	public void appendAnd() {
		remove();
		tokens.add("and");
	}

	@Transient
	public void appendOr() {
		remove();
		tokens.add("or");
	}

	List<Object> getParams() {
		return params;
	}

	void remove() {
		int size = tokens.size();
		while (size > 0) {
			String token = tokens.get(size - 1);
			if ("and".equalsIgnoreCase(token) || "or".equalsIgnoreCase(token)) {
				tokens.remove(size - 1);
				size--;
			} else {
				break;
			}
		}
		while (size > 0) {
			String token = tokens.get(0);
			if ("and".equalsIgnoreCase(token) || "or".equalsIgnoreCase(token)) {
				tokens.remove(0);
				size--;
			} else {
				break;
			}
		}
	}

	boolean isEmpty() {
		return tokens.isEmpty();
	}

	@Transient
	public void append(String value) {
		tokens.add(value);
	}

	@Transient
	public void append(String sql, Object value) {
		tokens.add(sql);
		params.add(value);
	}

	String getSql() {
		remove();
		if (isEmpty()) {
			return "";
		}
		return (needWhere ? " where " : "") + String.join(" ", tokens);
	}

	boolean filterNullAndBlank(Object value) {
		if (notNull && value == null) {
			return false;
		}
		return !notBlank || !StringUtils.isEmpty(Objects.toString(value, ""));
	}

	private Where append(boolean append, String column, String condition, Object value) {
		if (append && filterNullAndBlank(value)) {
			append(namedTable.rowMapColumnMapper.apply(column));
			append(condition);
			appendAnd();
			params.add(value);
		}
		return this;
	}
}
