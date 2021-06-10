package org.ssssssss.magicapi.modules.table;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.functions.StreamExtension;

import java.util.*;
import java.util.function.Function;

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

	void appendAnd() {
		remove();
		tokens.add("and");
	}

	void appendOr() {
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

	void append(String value) {
		tokens.add(value);
	}

	String getSql() {
		remove();
		if (isEmpty()) {
			return "";
		}
		return (needWhere ? " where " : "") + StringUtils.join(tokens, " ");
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
		this.notNull = flag;
		return this;
	}

	boolean filterNullAndBlank(Object value) {
		if (notNull && value == null) {
			return false;
		}
		return !notBlank || !StringUtils.isEmpty(Objects.toString(value, ""));
	}

	@Comment("等于`=`,如：`eq('name', '老王') ---> name = '老王'`")
	public Where eq(@Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return eq(true, column, value);
	}

	@Comment("等于`=`,如：`eq('name', '老王') ---> name = '老王'`")
	public Where eq(@Comment("判断表达式，当为true时拼接条件") boolean condition, @Comment("数据库中的列名") String column, @Comment("值") Object value) {
		if (condition && filterNullAndBlank(value)) {
			tokens.add(column);
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
	public Where ne(@Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return ne(true, column, value);
	}

	@Comment("不等于`<>`,如：`ne('name', '老王') ---> name <> '老王'`")
	public Where ne(@Comment("判断表达式，当为true时拼接条件") boolean condition, @Comment("数据库中的列名") String column, @Comment("值") Object value) {
		if (condition && filterNullAndBlank(value)) {
			append(column);
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

	private Where append(boolean append, String column, String condition, Object value) {
		if (append && filterNullAndBlank(value)) {
			append(column);
			append(condition);
			appendAnd();
			params.add(value);
		}
		return this;
	}

	@Comment("小于`<`,如：`lt('age', 18) ---> age < 18")
	public Where lt(@Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return lt(true, column, value);
	}

	@Comment("小于`<`,如：`lt('age', 18) ---> age < 18")
	public Where lt(@Comment("判断表达式，当为true时拼接条件") boolean condition, @Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return append(condition, column, " < ?", value);
	}

	@Comment("小于等于`<=`,如：`lte('age', 18) ---> age <= 18")
	public Where lte(@Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return lte(true, column, value);
	}

	@Comment("小于等于`<=`,如：`lte('age', 18) ---> age <= 18")
	public Where lte(@Comment("判断表达式，当为true时拼接条件") boolean condition, @Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return append(condition, column, " <= ?", value);
	}

	@Comment("大于`>`,如：`get('age', 18) ---> age > 18")
	public Where gt(@Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return gt(true, column, value);
	}

	@Comment("大于`>`,如：`get('age', 18) ---> age > 18")
	public Where gt(@Comment("判断表达式，当为true时拼接条件") boolean condition, @Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return append(condition, column, " > ?", value);
	}

	@Comment("大于等于`>=`,如：`get('age', 18) ---> age >= 18")
	public Where gte(@Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return gte(true, column, value);
	}

	@Comment("大于等于`>=`,如：`get('age', 18) ---> age >= 18")
	public Where gte(@Comment("判断表达式，当为true时拼接条件") boolean condition, @Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return append(condition, column, " >= ?", value);
	}

	@Comment("`in`,如：`in('age', [1,2,3]) ---> age in (1,2,3)")
	public Where in(@Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return in(true, column, value);
	}

	@Comment("`in`,如：`in('age', [1,2,3]) ---> age in (1,2,3)")
	public Where in(@Comment("判断表达式，当为true时拼接条件") boolean condition, @Comment("数据库中的列名") String column, @Comment("值") Object value) {
		if (condition && value != null) {
			List<Object> objects = StreamExtension.arrayLikeToList(value);
			if (objects.size() > 0) {
				append(column);
				append(" in (");
				append(StringUtils.join(",", Collections.nCopies(objects.size(), "?")));
				append(")");
				appendAnd();
				params.addAll(objects);
			}
		}
		return this;
	}

	@Comment("`not in`,如：`notIn('age', [1,2,3]) ---> age not in (1,2,3)")
	public Where notIn(@Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return notIn(true, column, value);
	}

	@Comment("`not in`,如：`notIn('age', [1,2,3]) ---> age not in (1,2,3)")
	public Where notIn(@Comment("判断表达式，当为true时拼接条件") boolean condition, @Comment("数据库中的列名") String column, @Comment("值") Object value) {
		if (condition && value != null) {
			List<Object> objects = StreamExtension.arrayLikeToList(value);
			if (objects.size() > 0) {
				append(column);
				append("not in (");
				append(StringUtils.join(",", Collections.nCopies(objects.size(), "?")));
				append(")");
				appendAnd();
				params.addAll(objects);
			}
		}
		return this;
	}

	@Comment("`like`,如：`like('name', '%王%') ---> name like '%王%'")
	public Where like(@Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return like(true, column, value);
	}

	@Comment("`like`,如：`like('name', '%王%') ---> name like '%王%'")
	public Where like(@Comment("判断表达式，当为true时拼接条件") boolean condition, @Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return append(condition, column, "like ?", value);
	}

	@Comment("`not like`,如：`notLike('name', '%王%') ---> name not like '%王%'")
	public Where notLike(@Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return notLike(true, column, value);
	}

	@Comment("`not like` ,如：`notLike('name', '%王%') ---> name not like '%王%'")
	public Where notLike(@Comment("判断表达式，当为true时拼接条件") boolean condition, @Comment("数据库中的列名") String column, @Comment("值") Object value) {
		return append(condition, column, "not like ?", value);
	}

	@Comment("`is null`,如：`isNull('name') ---> name is null")
	public Where isNull(@Comment("数据库中的列名") String column) {
		return isNull(true, column);
	}

	@Comment("`is null`,如：`isNull('name') ---> name is null")
	public Where isNull(@Comment("判断表达式，当为true时拼接条件") boolean condition, @Comment("数据库中的列名") String column) {
		if (condition) {
			append(column);
			append("is null");
			appendAnd();
		}
		return this;
	}

	@Comment("`is not null`,如：`isNotNull('name') ---> name is not null")
	public Where isNotNull(@Comment("数据库中的列名") String column) {
		return isNotNull(true, column);
	}

	@Comment("`is not null`,如：`isNotNull('name') ---> name is not null")
	public Where isNotNull(@Comment("判断表达式，当为true时拼接条件") boolean condition, @Comment("数据库中的列名") String column) {
		if (condition) {
			append(column);
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
	public Where and(Function<Object[], Where> function) {
		return and(true, function);
	}

	@Comment("`and`嵌套，如and(it => it.eq('name','李白').ne('status','正常') --> and (name = '李白' and status <> '正常')")
	public Where and(@Comment("判断表达式，当为true时拼接条件") boolean condition, Function<Object[], Where> function) {
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
	public Where orderBy(@Comment("要排序的列") String column, @Comment("`asc`或`desc`") String sort) {
		this.namedTable.orderBy(column, sort);
		return this;
	}

	@Comment("拼接`order by xxx asc`")
	public Where orderBy(@Comment("要排序的列") String column) {
		return orderBy(column, "asc");
	}

	@Comment("拼接`order by xxx desc`")
	public Where orderByDesc(@Comment("要排序的列") String column) {
		return orderBy(column, "desc");
	}

	@Comment("拼接`group by`")
	public Where groupBy(@Comment("要分组的列") String... columns) {
		this.namedTable.groupBy(columns);
		return this;
	}

	@Comment("保存到表中，当主键有值时则修改，否则插入")
	public Object save() {
		return namedTable.save();
	}

	@Comment("保存到表中，当主键有值时则修改，否则插入")
	public Object save(@Comment("各项列和值") Map<String, Object> data) {
		return namedTable.save(data);
	}

	@Comment("执行插入语句，返回主键")
	public Object insert() {
		return namedTable.insert();
	}

	@Comment("执行插入语句，返回主键")
	public Object insert(@Comment("各项列和值") Map<String, Object> data) {
		return namedTable.insert(data);
	}

	@Comment("执行update语句")
	public int update() {
		return namedTable.update();
	}

	@Comment("执行delete语句")
	public int delete() {
		return namedTable.delete();
	}

	@Comment("执行update语句")
	public int update(@Comment("各项列和值") Map<String, Object> data) {
		return namedTable.update(data);
	}

	@Comment("执行分页查询")
	public Object page() {
		return namedTable.page();
	}

	@Comment("执行select查询")
	public List<Map<String, Object>> select() {
		return namedTable.select();
	}

	@Comment("执行selectOne查询")
	public Map<String, Object> selectOne() {
		return namedTable.selectOne();
	}

}
