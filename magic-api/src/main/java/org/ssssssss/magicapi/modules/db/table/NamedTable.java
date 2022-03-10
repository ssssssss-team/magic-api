package org.ssssssss.magicapi.modules.db.table;

import org.apache.commons.lang3.StringUtils;
import org.ssssssss.magicapi.core.context.RequestContext;
import org.ssssssss.magicapi.core.exception.MagicAPIException;
import org.ssssssss.magicapi.core.model.Attributes;
import org.ssssssss.magicapi.core.context.RequestEntity;
import org.ssssssss.magicapi.modules.db.BoundSql;
import org.ssssssss.magicapi.modules.db.inteceptor.NamedTableInterceptor;
import org.ssssssss.magicapi.modules.db.SQLModule;
import org.ssssssss.magicapi.modules.db.model.Page;
import org.ssssssss.magicapi.modules.db.model.SqlMode;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.runtime.RuntimeContext;

import java.beans.Transient;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 单表操作API
 *
 * @author mxd
 */
public class NamedTable extends Attributes<Object> {

	String tableName;

	SQLModule sqlModule;

	String primary;

	String logicDeleteColumn;

	Object logicDeleteValue;

	Map<String, Object> columns = new HashMap<>();

	List<String> fields = new ArrayList<>();

	List<String> groups = new ArrayList<>();

	List<String> orders = new ArrayList<>();

	Set<String> excludeColumns = new HashSet<>();

	Function<String, String> rowMapColumnMapper;

	Object defaultPrimaryValue;

	boolean useLogic = false;

	boolean withBlank = false;

	List<NamedTableInterceptor> namedTableInterceptors;

	Where where = new Where(this);

	public NamedTable(String tableName, SQLModule sqlModule, Function<String, String> rowMapColumnMapper, List<NamedTableInterceptor> namedTableInterceptors) {
		this.tableName = tableName;
		this.sqlModule = sqlModule;
		this.rowMapColumnMapper = rowMapColumnMapper;
		this.namedTableInterceptors = namedTableInterceptors;
		this.logicDeleteColumn = sqlModule.getLogicDeleteColumn();
		String deleteValue = sqlModule.getLogicDeleteValue();
		this.logicDeleteValue = deleteValue;
		if (deleteValue != null) {
			boolean isString = deleteValue.startsWith("'") || deleteValue.startsWith("\"");
			if (isString && deleteValue.length() > 2) {
				this.logicDeleteValue = deleteValue.substring(1, deleteValue.length() - 1);
			} else {
				try {
					this.logicDeleteValue = Integer.parseInt(deleteValue);
				} catch (NumberFormatException e) {
					this.logicDeleteValue = deleteValue;
				}
			}
		}
	}

	private NamedTable() {
	}

	@Override
	@Comment("克隆")
	public NamedTable clone() {
		NamedTable namedTable = new NamedTable();
		namedTable.tableName = this.tableName;
		namedTable.sqlModule = this.sqlModule;
		namedTable.primary = this.primary;
		namedTable.logicDeleteValue = this.logicDeleteValue;
		namedTable.logicDeleteColumn = this.logicDeleteColumn;
		namedTable.columns = new HashMap<>(this.columns);
		namedTable.fields = new ArrayList<>(fields);
		namedTable.groups = new ArrayList<>(groups);
		namedTable.orders = new ArrayList<>(orders);
		namedTable.excludeColumns = new HashSet<>(excludeColumns);
		namedTable.rowMapColumnMapper = this.rowMapColumnMapper;
		namedTable.defaultPrimaryValue = this.defaultPrimaryValue;
		namedTable.useLogic = this.useLogic;
		namedTable.withBlank = this.withBlank;
		namedTable.where = this.where == null ? null : this.where.clone();
		namedTable.namedTableInterceptors = this.namedTableInterceptors;
		namedTable.properties = this.properties;
		return namedTable;
	}

	@Comment("使用逻辑删除")
	public NamedTable logic() {
		this.useLogic = true;
		return this;
	}

	@Comment("更新空值")
	public NamedTable withBlank() {
		this.withBlank = true;
		return this;
	}

	@Comment("设置主键名，update时使用")
	public NamedTable primary(@Comment(name = "primary", value = "主键列") String primary) {
		this.primary = rowMapColumnMapper.apply(primary);
		return this;
	}

	@Comment("设置主键名，并设置默认主键值(主要用于insert)")
	public NamedTable primary(@Comment(name = "primary", value = "主键列") String primary,
							  @Comment(name = "defaultPrimaryValue", value = "默认值") Serializable defaultPrimaryValue) {
		this.primary = rowMapColumnMapper.apply(primary);
		this.defaultPrimaryValue = defaultPrimaryValue;
		return this;
	}

	@Comment("设置主键名，并设置默认主键值(主要用于insert)")
	public NamedTable primary(@Comment(name = "primary", value = "主键列") String primary,
							  @Comment(name = "defaultPrimaryValue", value = "默认值") Supplier<Object> defaultPrimaryValue) {
		this.primary = rowMapColumnMapper.apply(primary);
		this.defaultPrimaryValue = defaultPrimaryValue;
		return this;
	}

	@Comment("拼接where")
	public Where where() {
		return where;
	}

	@Comment("设置单列的值")
	public NamedTable column(@Comment(name = "property", value = "列名") String property,
							 @Comment(name = "value", value = "值") Object value) {
		this.columns.put(rowMapColumnMapper.apply(property), value);
		return this;
	}

	@Comment("设置查询的列，如`columns('a','b','c')`")
	public NamedTable columns(@Comment(name = "properties", value = "各项列") String... properties) {
		if (properties != null) {
			for (String property : properties) {
				column(property);
			}
		}
		return this;
	}

	@Comment("设置要排除的列")
	public NamedTable exclude(@Comment(name = "property", value = "排除的列") String property) {
		if (property != null) {
			excludeColumns.add(rowMapColumnMapper.apply(property));
		}
		return this;
	}

	@Comment("设置要排除的列")
	public NamedTable excludes(@Comment(name = "properties", value = "排除的列") String... properties) {
		if (columns != null) {
			excludeColumns.addAll(Arrays.stream(properties).map(rowMapColumnMapper).collect(Collectors.toList()));
		}
		return this;
	}

	@Comment("设置要排除的列")
	public NamedTable excludes(@Comment(name = "properties", value = "排除的列") List<String> properties) {
		if (columns != null) {
			excludeColumns.addAll(properties.stream().map(rowMapColumnMapper).collect(Collectors.toList()));
		}
		return this;
	}

	@Comment("设置查询的列，如`columns(['a','b','c'])`")
	public NamedTable columns(@Comment(name = "properties", value = "查询的列") Collection<String> properties) {
		if (properties != null) {
			properties.stream().filter(StringUtils::isNotBlank).map(rowMapColumnMapper).forEach(this.fields::add);
		}
		return this;
	}

	@Comment("设置查询的列，如`column('a')`")
	public NamedTable column(@Comment(name = "property", value = "查询的列") String property) {
		if (StringUtils.isNotBlank(property)) {
			this.fields.add(this.rowMapColumnMapper.apply(property));
		}
		return this;
	}

	@Comment("拼接`order by xxx asc/desc`")
	public NamedTable orderBy(@Comment(name = "property", value = "要排序的列") String property,
							  @Comment(name = "sort", value = "`asc`或`desc`") String sort) {
		this.orders.add(rowMapColumnMapper.apply(property) + " " + sort);
		return this;
	}

	@Comment("拼接`order by xxx asc`")
	public NamedTable orderBy(@Comment(name = "property", value = "要排序的列") String property) {
		return orderBy(property, "asc");
	}

	@Comment("拼接`order by xxx desc`")
	public NamedTable orderByDesc(@Comment(name = "property", value = "要排序的列") String property) {
		return orderBy(property, "desc");
	}

	@Comment("拼接`group by`")
	public NamedTable groupBy(@Comment(name = "properties", value = "要分组的列") String... properties) {
		this.groups.addAll(Arrays.stream(properties).map(rowMapColumnMapper).collect(Collectors.toList()));
		return this;
	}

	@Comment("执行插入,返回主键")
	public Object insert(RuntimeContext runtimeContext) {
		return insert(runtimeContext, null);
	}

	@Comment("执行插入,返回主键")
	public Object insert(RuntimeContext runtimeContext,
						 @Comment(name = "data", value = "各项列和值") Map<String, Object> data) {
		if (data != null) {
			data.forEach((key, value) -> this.columns.put(rowMapColumnMapper.apply(key), value));
		}
		if (this.defaultPrimaryValue != null && StringUtils.isBlank(Objects.toString(this.columns.getOrDefault(this.primary, "")))) {
			if (this.defaultPrimaryValue instanceof Supplier) {
				this.columns.put(this.primary, ((Supplier<?>) this.defaultPrimaryValue).get());
			} else {
				this.columns.put(this.primary, this.defaultPrimaryValue);
			}
		}
		preHandle(SqlMode.INSERT);
		Collection<Map.Entry<String, Object>> entries = filterNotBlanks();
		if (entries.isEmpty()) {
			throw new MagicAPIException("参数不能为空");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("insert into ");
		builder.append(tableName);
		builder.append("(");
		builder.append(StringUtils.join(entries.stream().map(Map.Entry::getKey).toArray(), ","));
		builder.append(") values (");
		builder.append(StringUtils.join(Collections.nCopies(entries.size(), "?"), ","));
		builder.append(")");
		Object value = sqlModule.insert(new BoundSql(runtimeContext, builder.toString(), entries.stream().map(Map.Entry::getValue).collect(Collectors.toList()), sqlModule), this.primary);
		if(value == null && StringUtils.isNotBlank(this.primary)){
			return this.columns.get(this.primary);
		}
		return value;
	}

	@Comment("执行delete语句")
	public int delete(RuntimeContext runtimeContext) {
		preHandle(SqlMode.DELETE);
		if (useLogic) {
			Map<String, Object> dataMap = new HashMap<>();
			dataMap.put(logicDeleteColumn, logicDeleteValue);
			return update(runtimeContext, dataMap);
		}
		if (where.isEmpty()) {
			throw new MagicAPIException("delete语句不能没有条件");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("delete from ");
		builder.append(tableName);
		builder.append(where.getSql());
		return sqlModule.update(new BoundSql(runtimeContext, builder.toString(), where.getParams(), sqlModule));
	}

	@Comment("保存到表中，当主键有值时则修改，否则插入")
	public Object save(RuntimeContext runtimeContext) {
		return this.save(runtimeContext, null, false);
	}

	@Comment("保存到表中，当主键有值时则修改，否则插入")
	public Object save(RuntimeContext runtimeContext,
					   @Comment(name = "data", value = "各项列和值") Map<String, Object> data,
					   @Comment(name = "beforeQuery", value = "是否根据id查询有没有数据") boolean beforeQuery) {
		if (StringUtils.isBlank(this.primary)) {
			throw new MagicAPIException("请设置主键");
		}
		if (data != null) {
			data.forEach((key, value) -> this.columns.put(rowMapColumnMapper.apply(key), value));
		}
		String primaryValue = Objects.toString(this.columns.get(this.primary), "");
		if (StringUtils.isBlank(primaryValue) && data != null) {
			primaryValue = Objects.toString(data.get(this.primary), "");
		}
		if (beforeQuery) {
			if (StringUtils.isNotBlank(primaryValue)) {
				List<Object> params = new ArrayList<>();
				params.add(primaryValue);
				Integer count = sqlModule.selectInt(new BoundSql(runtimeContext, "select count(*) count from " + this.tableName + " where " + this.primary + " = ?", params, sqlModule));
				if (count == 0) {
					return insert(runtimeContext, data);
				}
				return update(runtimeContext, data);
			} else {
				return insert(runtimeContext, data);
			}
		}

		if (StringUtils.isNotBlank(primaryValue)) {
			return update(runtimeContext, data);
		}
		return insert(runtimeContext, data);
	}

	@Comment("保存到表中，当主键有值时则修改，否则插入")
	public Object save(RuntimeContext runtimeContext,
					   @Comment(name = "beforeQuery", value = "是否根据id查询有没有数据") boolean beforeQuery) {
		return this.save(runtimeContext, null, beforeQuery);
	}

	@Comment("保存到表中，当主键有值时则修改，否则插入")
	public Object save(RuntimeContext runtimeContext,
					   @Comment(name = "data", value = "各项列和值") Map<String, Object> data) {
		return this.save(runtimeContext, data, false);
	}

	@Comment("批量插入")
	public int batchInsert(@Comment(name = "collection", value = "各项列和值") Collection<Map<String, Object>> collection, @Comment("batchSize") int batchSize) {
		Set<String> keys = collection.stream().flatMap(it -> it.keySet().stream()).collect(Collectors.toSet());
		if (keys.isEmpty()) {
			throw new MagicAPIException("要插入的列不能为空");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("insert into ");
		builder.append(tableName);
		builder.append("(");
		builder.append(StringUtils.join(keys.stream().map(rowMapColumnMapper).collect(Collectors.toList()), ","));
		builder.append(") values (");
		builder.append(StringUtils.join(Collections.nCopies(keys.size(), "?"), ","));
		builder.append(")");
		return this.sqlModule.batchUpdate(builder.toString(), batchSize, collection.stream()
				.map(it -> keys.stream().map(it::get).toArray())
				.collect(Collectors.toList()));
	}

	@Comment("批量插入")
	public int batchInsert(@Comment(name = "collection", value = "各项列和值") Collection<Map<String, Object>> collection) {
		return batchInsert(collection, 100);
	}

	@Comment("执行`select`查询")
	public List<Map<String, Object>> select(RuntimeContext runtimeContext) {
		preHandle(SqlMode.SELECT);
		return sqlModule.select(buildSelect(runtimeContext));
	}

	@Comment("执行`selectOne`查询")
	public Map<String, Object> selectOne(RuntimeContext runtimeContext) {
		preHandle(SqlMode.SELECT_ONE);
		return sqlModule.selectOne(buildSelect(runtimeContext));
	}

	@Comment("执行分页查询")
	public Object page(RuntimeContext runtimeContext) {
		preHandle(SqlMode.PAGE);
		return sqlModule.page(buildSelect(runtimeContext));
	}

	@Comment("执行分页查询，分页条件手动传入")
	public Object page(RuntimeContext runtimeContext,
					   @Comment(name = "limit", value = "限制条数") long limit,
					   @Comment(name = "offset", value = "跳过条数") long offset) {
		preHandle(SqlMode.PAGE);
		return sqlModule.page(buildSelect(runtimeContext), new Page(limit, offset));
	}

	@Comment("执行update语句")
	public int update(RuntimeContext runtimeContext) {
		return update(runtimeContext, null);
	}

	@Comment("执行update语句")
	public int update(RuntimeContext runtimeContext,
					  @Comment(name = "data", value = "各项列和值") Map<String, Object> data,
					  @Comment(name = "isUpdateBlank", value = "是否更新空值字段") boolean isUpdateBlank) {
		if (null != data) {
			data.forEach((key, value) -> this.columns.put(rowMapColumnMapper.apply(key), value));
		}
		preHandle(SqlMode.UPDATE);
		Object primaryValue = null;
		if (StringUtils.isNotBlank(this.primary)) {
			primaryValue = this.columns.remove(this.primary);
		}
		this.withBlank = isUpdateBlank;
		List<Map.Entry<String, Object>> entries = new ArrayList<>(filterNotBlanks());
		if (entries.isEmpty()) {
			throw new MagicAPIException("要修改的列不能为空");
		}
		StringBuilder builder = new StringBuilder();
		builder.append("update ");
		builder.append(tableName);
		builder.append(" set ");
		List<Object> params = new ArrayList<>();
		for (int i = 0, size = entries.size(); i < size; i++) {
			Map.Entry<String, Object> entry = entries.get(i);
			builder.append(entry.getKey()).append(" = ?");
			params.add(entry.getValue());
			if (i + 1 < size) {
				builder.append(",");
			}
		}
		if (!where.isEmpty()) {
			builder.append(where.getSql());
			params.addAll(where.getParams());
		} else if (primaryValue != null) {
			builder.append(" where ").append(this.primary).append(" = ?");
			params.add(primaryValue);
		} else {
			throw new MagicAPIException("主键值不能为空");
		}
		return sqlModule.update(new BoundSql(runtimeContext, builder.toString(), params, sqlModule));
	}

	@Comment("执行update语句")
	public int update(RuntimeContext runtimeContext,
					  @Comment(name = "data", value = "各项列和值") Map<String, Object> data) {
		return update(runtimeContext, data, this.withBlank);
	}

	@Comment("查询条数")
	public int count(RuntimeContext runtimeContext) {
		preHandle(SqlMode.COUNT);
		StringBuilder builder = new StringBuilder();
		builder.append("select count(1) from ").append(tableName);
		List<Object> params = buildWhere(builder);
		return sqlModule.selectInt(new BoundSql(runtimeContext, builder.toString(), params, sqlModule));
	}

	@Comment("判断是否存在")
	public boolean exists(RuntimeContext runtimeContext) {
		return count(runtimeContext) > 0;
	}

	private Collection<Map.Entry<String, Object>> filterNotBlanks() {
		if (this.withBlank) {
			return this.columns.entrySet()
					.stream()
					.filter(it -> !excludeColumns.contains(it.getKey()))
					.collect(Collectors.toList());
		}
		return this.columns.entrySet()
				.stream()
				.filter(it -> StringUtils.isNotBlank(Objects.toString(it.getValue(), "")))
				.filter(it -> !excludeColumns.contains(it.getKey()))
				.collect(Collectors.toList());
	}

	private void preHandle(SqlMode sqlMode) {
		if (this.namedTableInterceptors != null) {
			this.namedTableInterceptors.forEach(interceptor -> interceptor.preHandle(sqlMode, this));
		}
	}

	private BoundSql buildSelect(RuntimeContext runtimeContext) {
		StringBuilder builder = new StringBuilder();
		builder.append("select ");
		List<String> fields = this.fields.stream()
				.filter(it -> !excludeColumns.contains(it))
				.collect(Collectors.toList());
		if (fields.isEmpty()) {
			builder.append("*");
		} else {
			builder.append(StringUtils.join(fields, ","));
		}
		builder.append(" from ").append(tableName);
		List<Object> params = buildWhere(builder);
		if (!orders.isEmpty()) {
			builder.append(" order by ");
			builder.append(String.join(",", orders));
		}
		if (!groups.isEmpty()) {
			builder.append(" group by ");
			builder.append(String.join(",", groups));
		}
		BoundSql boundSql = new BoundSql(runtimeContext, builder.toString(), params, sqlModule);
		boundSql.setExcludeColumns(excludeColumns);
		return boundSql;
	}


	private List<Object> buildWhere(StringBuilder builder) {
		List<Object> params = new ArrayList<>();
		if (!where.isEmpty()) {
			where.and();
			where.ne(useLogic, logicDeleteColumn, logicDeleteValue);
			builder.append(where.getSql());
			params.addAll(where.getParams());
		} else if (useLogic) {
			where.ne(logicDeleteColumn, logicDeleteValue);
			builder.append(where.getSql());
			params.addAll(where.getParams());
		}
		return params;
	}


	/**
	 * 获取查询的表名
	 *
	 * @return 表名
	 */
	@Transient
	public String getTableName() {
		return tableName;
	}

	/**
	 * 设置表名
	 *
	 * @param tableName 表名
	 */
	@Transient
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * 获取SQL模块
	 */
	@Transient
	public SQLModule getSqlModule() {
		return sqlModule;
	}

	/**
	 * 获取主键列
	 */
	@Transient
	public String getPrimary() {
		return primary;
	}

	/**
	 * 获取逻辑删除列
	 */
	@Transient
	public String getLogicDeleteColumn() {
		return logicDeleteColumn;
	}

	/**
	 * 获取逻辑删除值
	 */
	@Transient
	public Object getLogicDeleteValue() {
		return logicDeleteValue;
	}

	/**
	 * 获取设置的columns
	 */
	@Transient
	public Map<String, Object> getColumns() {
		return columns;
	}

	/**
	 * 设置columns
	 */
	@Transient
	public void setColumns(Map<String, Object> columns) {
		this.columns = columns;
	}

	/**
	 * 获取设置的fields
	 */
	@Transient
	public List<String> getFields() {
		return fields;
	}

	/**
	 * 设置 fields
	 */
	@Transient
	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	/**
	 * 获取设置的group
	 */
	@Transient
	public List<String> getGroups() {
		return groups;
	}

	/**
	 * 设置 group
	 */
	@Transient
	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	/**
	 * 获取设置的order
	 */
	@Transient
	public List<String> getOrders() {
		return orders;
	}

	/**
	 * 设置 order
	 */
	@Transient
	public void setOrders(List<String> orders) {
		this.orders = orders;
	}

	/**
	 * 获取设置的排除的列
	 */
	@Transient
	public Set<String> getExcludeColumns() {
		return excludeColumns;
	}

	/**
	 * 设置排除的列
	 */
	@Transient
	public void setExcludeColumns(Set<String> excludeColumns) {
		this.excludeColumns = excludeColumns;
	}

	/**
	 * 主键默认值
	 *
	 * @return
	 */
	@Transient
	public Object getDefaultPrimaryValue() {
		return defaultPrimaryValue;
	}

	/**
	 * 是否设逻辑了逻辑删除
	 */
	@Transient
	public boolean isUseLogic() {
		return useLogic;
	}

	/**
	 * 设置是否使用逻辑删除
	 */
	@Transient
	public void setUseLogic(boolean useLogic) {
		this.useLogic = useLogic;
	}

	/**
	 * 获取是否不过滤空参数
	 */
	@Transient
	public boolean isWithBlank() {
		return withBlank;
	}

	/**
	 * 设置是否不过滤空参数
	 */
	@Transient
	public void setWithBlank(boolean withBlank) {
		this.withBlank = withBlank;
	}

	/**
	 * 获取where
	 */
	@Transient
	public Where getWhere() {
		return where;
	}

	/**
	 * 获取RequestEntity
	 */
	@Transient
	public RequestEntity getRequestEntity() {
		return RequestContext.getRequestEntity();
	}
}
