package org.ssssssss.magicapi.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.config.Valid;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.model.JsonBean;
import org.ssssssss.magicapi.provider.MagicAPIService;

import java.util.List;
import java.util.Map;

public class MagicDataSourceController extends MagicController implements MagicExceptionHandler {

	private final MagicAPIService magicAPIService;

	public MagicDataSourceController(MagicConfiguration configuration) {
		super(configuration);
		this.magicAPIService = configuration.getMagicAPIService();
	}

	/**
	 * 查询数据源列表
	 */
	@RequestMapping("/datasource/list")
	@ResponseBody
	@Valid(authorization = Authorization.VIEW)
	public JsonBean<List<Map<String, Object>>> list() {
		return new JsonBean<>(magicAPIService.datasourceList());
	}

	@RequestMapping("/datasource/test")
	@ResponseBody
	public JsonBean<String> test(@RequestBody Map<String, String> properties) {
		return new JsonBean<>(magicAPIService.testDataSource(properties));
	}

	/**
	 * 保存数据源
	 *
	 * @param properties 数据源配置信息
	 */
	@RequestMapping("/datasource/save")
	@Valid(readonly = false, authorization = Authorization.DATASOURCE_SAVE)
	@ResponseBody
	public JsonBean<String> save(@RequestBody Map<String, String> properties) {
		return new JsonBean<>(magicAPIService.saveDataSource(properties));
	}

	/**
	 * 删除数据源
	 *
	 * @param id 数据源ID
	 */
	@RequestMapping("/datasource/delete")
	@Valid(readonly = false, authorization = Authorization.DATASOURCE_DELETE)
	@ResponseBody
	public JsonBean<Boolean> delete(String id) {
		return new JsonBean<>(magicAPIService.deleteDataSource(id));
	}

	@RequestMapping("/datasource/detail")
	@Valid(authorization = Authorization.DATASOURCE_VIEW)
	@ResponseBody
	public JsonBean<Object> detail(String id) {
		return new JsonBean<>(magicAPIService.getDataSource(id));
	}
}
