package org.ssssssss.magicapi.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.dialect.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DialectAdapter {

	private static final Logger logger = LoggerFactory.getLogger(DialectAdapter.class);

	private final List<Dialect> dialectList = new ArrayList<>();

	/**
	 * 缓存已解析的方言
	 */
	private final Map<String, Dialect> dialectMap = new ConcurrentHashMap<>();

	public DialectAdapter() {
		add(new MySQLDialect());
		add(new OracleDialect());
		add(new PostgreSQLDialect());
		add(new ClickhouseDialect());
		add(new DB2Dialect());
		add(new SQLServerDialect());
		add(new SQLServer2005Dialect());
		add(new DmDialect());
	}

	public void add(Dialect dialect){
		this.dialectList.add(dialect);
	}

	/**
	 * 获取数据库方言
	 */
	public Dialect getDialectFromUrl(String fromUrl) {
		Dialect cached = dialectMap.get(fromUrl);
		if (cached == null && !dialectMap.containsKey(fromUrl)) {
			for (Dialect dialect : dialectList) {
				if (dialect.match(fromUrl)) {
					cached = dialect;
					break;
				}
			}
			if (cached == null) {
				logger.warn(String.format("magic-api在%s中无法获取dialect", fromUrl));
			}
			dialectMap.put(fromUrl, cached);
		}
		return cached;
	}
}
