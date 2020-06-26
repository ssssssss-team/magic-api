package org.ssssssss.script.functions;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.ssssssss.magicapi.config.DynamicDataSource;
import org.ssssssss.magicapi.dialect.Dialect;
import org.ssssssss.magicapi.dialect.DialectUtils;
import org.ssssssss.magicapi.exception.MagicAPIException;
import org.ssssssss.magicapi.model.Page;
import org.ssssssss.magicapi.model.PageResult;
import org.ssssssss.magicapi.provider.PageProvider;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.GenericTokenParser;
import org.ssssssss.script.parsing.Parser;
import org.ssssssss.script.parsing.TokenStream;
import org.ssssssss.script.parsing.Tokenizer;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseQuery extends HashMap<String,DatabaseQuery> {

	private DynamicDataSource dataSource;

	private JdbcTemplate template;

	private PageProvider pageProvider;

	public DatabaseQuery(JdbcTemplate template,DynamicDataSource dataSource,PageProvider pageProvider) {
		this.template = template;
		this.dataSource = dataSource;
		this.pageProvider = pageProvider;
	}

	public DatabaseQuery(DynamicDataSource dataSource,PageProvider pageProvider) {
		this.dataSource = dataSource;
		this.pageProvider = pageProvider;
		this.template = dataSource.getJdbcTemplate(null);

	}

	@Override
	public DatabaseQuery get(Object key) {
		if(key == null){
			return new DatabaseQuery(dataSource.getJdbcTemplate(null),this.dataSource,this.pageProvider);
		}
		return new DatabaseQuery(dataSource.getJdbcTemplate(key.toString()),this.dataSource,this.pageProvider);
	}


	public Object select(String sql){
		BoundSql boundSql = new BoundSql(sql);
		return template.queryForList(boundSql.getSql(),boundSql.getParameters());
	}
	public Object page(String sql){
		Page page = pageProvider.getPage(MagicScriptContext.get());
		return page(sql,page.getLimit(),page.getOffset());
	}
	public Object page(String sql,long limit,long offset){
		BoundSql boundSql = new BoundSql(sql);
		Connection connection = null;
		int count;
		PageResult<Map<String,Object>> result = new PageResult<>();
		Dialect dialect;
		try {
			connection = template.getDataSource().getConnection();
			dialect = DialectUtils.getDialectFromUrl(connection.getMetaData().getURL());
			count = template.queryForObject(dialect.getCountSql(boundSql.getSql()),Integer.class,boundSql.getParameters());
			result.setTotal(count);
		} catch (SQLException e) {
			throw new MagicAPIException("自动获取数据库方言失败",e);
		} finally{
			DataSourceUtils.releaseConnection(connection,template.getDataSource());
		}
		if(count > 0){
			result.setList(template.queryForList(dialect.getPageSql(boundSql.getSql(), boundSql,offset,limit),boundSql.getParameters()));
		}
		return result;
	}
	public Integer selectInt(String sql){
		BoundSql boundSql = new BoundSql(sql);
		return template.queryForObject(boundSql.getSql(),boundSql.getParameters(),Integer.class);
	}

	public Map<String,Object> selectOne(String sql){
		BoundSql boundSql = new BoundSql(sql);
		List<Map<String, Object>> list = template.queryForList(boundSql.getSql(),boundSql.getParameters());
		return list!= null && list.size() > 0 ? list.get(0) : null;
	}

	public Object selectValue(String sql){
		BoundSql boundSql = new BoundSql(sql);
		return template.queryForObject(boundSql.getSql(),boundSql.getParameters(),Object.class);
	}

	private static Tokenizer tokenizer = new Tokenizer();

	private static GenericTokenParser concatTokenParser = new GenericTokenParser("${","}",false);

	private static GenericTokenParser replaceTokenParser = new GenericTokenParser("#{","}",true);

	private static GenericTokenParser ifTokenParser = new GenericTokenParser("?{","}",true);

	private static GenericTokenParser ifParamTokenParser = new GenericTokenParser("?{",",",true);

	public static class BoundSql{
		private String sql;
		private List<Object> parameters = new ArrayList<>();
		BoundSql(String sql){
			MagicScriptContext context = MagicScriptContext.get();
			this.sql = ifTokenParser.parse(sql,text->{
				AtomicBoolean ifTrue = new AtomicBoolean(false);
				String val = ifParamTokenParser.parse("?{" + text, param -> {
					Object result = Parser.parseExpression(new TokenStream(tokenizer.tokenize(param))).evaluate(context);
					ifTrue.set(Objects.equals(true, result));
					return null;
				});
				if(ifTrue.get()){
					return val;
				}
				return "";
			});
			this.sql = concatTokenParser.parse(this.sql,text->String.valueOf(Parser.parseExpression(new TokenStream(tokenizer.tokenize(text))).evaluate(context)));
			this.sql = replaceTokenParser.parse(this.sql,text->{
				parameters.add(Parser.parseExpression(new TokenStream(tokenizer.tokenize(text))).evaluate(context));
				return "?";
			});
		}

		public void addParameter(Object value){
			parameters.add(value);
		}

		public String getSql() {
			return sql;
		}

		public Object[] getParameters() {
			return parameters.toArray();
		}
	}

}
