package org.ssssssss.magicapi.datasource.service;

import org.ssssssss.magicapi.datasource.model.DataSourceInfo;

/**
 * 数据源加解密
 *
 * @since 1.7.0
 */
public interface DataSourceEncryptProvider {

	/**
	 * 加密
	 * @param dataSourceInfo 数据源信息
	 */
	void encrypt(DataSourceInfo dataSourceInfo);

	/**
	 * 解密
	 * @param dataSourceInfo 数据源信息
	 */
	void decrypt(DataSourceInfo dataSourceInfo);
}
