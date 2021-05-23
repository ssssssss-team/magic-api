package org.ssssssss.magicapi.provider;


import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.model.ApiInfo;

/**
 * API存储接口
 */
public abstract class ApiServiceProvider extends StoreServiceProvider<ApiInfo> {


	public ApiServiceProvider(Resource workspace, GroupServiceProvider groupServiceProvider) {
		super(ApiInfo.class, workspace, groupServiceProvider);
	}

	/**
	 * 判断接口是否存在
	 */
	public boolean exists(ApiInfo info) {
		return infos.values().stream()
				.anyMatch(it -> info.getGroupId().equals(it.getGroupId()) && (info.getName().equals(it.getName()) || (info.getMethod().equals(it.getMethod()) && info.getPath().equals(it.getPath()))));
	}

	/**
	 * 判断接口是否存在
	 */
	public boolean existsWithoutId(ApiInfo info) {
		return infos.values().stream()
				.anyMatch(it -> !info.getId().equals(it.getId()) && info.getGroupId().equals(it.getGroupId()) && (info.getName().equals(it.getName()) || (info.getMethod().equals(it.getMethod()) && info.getPath().equals(it.getPath()))));
	}

	@Override
	public byte[] serialize(ApiInfo info) {
		return super.serialize(info);
	}
}
