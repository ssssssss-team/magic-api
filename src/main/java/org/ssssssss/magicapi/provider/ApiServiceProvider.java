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
	 *
	 * @param groupId 分组Id
	 * @param method  请求方法
	 * @param path    请求路径
	 */
	public boolean exists(String groupId, String method, String path){
		return infos.values().stream()
				.anyMatch(it -> groupId.equals(it.getGroupId()) && method.equals(it.getMethod()) && path.equals(it.getPath()));
	}

	/**
	 * 判断接口是否存在
	 *
	 * @param groupId 分组ID
	 * @param method  请求方法
	 * @param path    请求路径
	 * @param id      排除接口
	 */
	public boolean existsWithoutId(String groupId, String method, String path, String id){
		return infos.values().stream()
				.anyMatch(it -> !id.equals(it.getId()) && groupId.equals(it.getGroupId()) && method.equals(it.getMethod()) && path.equals(it.getPath()));
	}

	@Override
	public byte[] serialize(ApiInfo info) {
		return super.serialize(info);
	}
}
