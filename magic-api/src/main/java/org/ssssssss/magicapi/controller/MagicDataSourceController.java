package org.ssssssss.magicapi.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.model.DataSourceInfo;
import org.ssssssss.magicapi.model.JsonBean;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据源相关操作
 *
 * @author mxd
 */
public class MagicDataSourceController extends MagicController implements MagicExceptionHandler {

	public MagicDataSourceController(MagicConfiguration configuration) {
		super(configuration);
	}

	/**
	 * 查询数据源列表
	 */
	@RequestMapping("/datasource/list")
	@ResponseBody
	public JsonBean<List<DataSourceInfo>> list(HttpServletRequest request) {
		return new JsonBean<>(magicAPIService.datasourceList()
				.stream()
				.filter(it -> allowVisit(request, Authorization.VIEW, it))
				.collect(Collectors.toList())
		);
	}

	@RequestMapping("/datasource/test")
	@ResponseBody
	public JsonBean<String> test(@RequestBody DataSourceInfo properties) {
		return new JsonBean<>(magicAPIService.testDataSource(properties));
	}

	/**
	 * 保存数据源
	 *
	 * @param properties 数据源配置信息
	 */
	@RequestMapping("/datasource/save")
	@Valid(readonly = false)
	@ResponseBody
	public JsonBean<String> save(HttpServletRequest request, @RequestBody DataSourceInfo properties) {
		isTrue(allowVisit(request, Authorization.SAVE, properties), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.saveDataSource(properties));
	}

	/**
	 * 删除数据源
	 *
	 * @param id 数据源ID
	 */
	@RequestMapping("/datasource/delete")
	@Valid(readonly = false)
	@ResponseBody
	public JsonBean<Boolean> delete(HttpServletRequest request, String id) {
		DataSourceInfo dataSource = getDataSourceInfo(id);
		isTrue(allowVisit(request, Authorization.DELETE, dataSource), PERMISSION_INVALID);
		return new JsonBean<>(magicAPIService.deleteDataSource(id));
	}

	@RequestMapping("/datasource/detail")
	@ResponseBody
	public JsonBean<DataSourceInfo> detail(HttpServletRequest request, String id) {
		DataSourceInfo dataSource = getDataSourceInfo(id);
		isTrue(allowVisit(request, Authorization.VIEW, dataSource), PERMISSION_INVALID);
		return new JsonBean<>(dataSource);
	}

	private DataSourceInfo getDataSourceInfo(String id) {
		DataSourceInfo dataSource = magicAPIService.getDataSource(id);
		notNull(dataSource, DATASOURCE_NOT_FOUND);
		return dataSource;
	}
}
