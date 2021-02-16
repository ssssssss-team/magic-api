package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.model.FunctionInfo;

public abstract class FunctionServiceProvider extends StoreServiceProvider<FunctionInfo> {

	public FunctionServiceProvider(Resource workspace, GroupServiceProvider groupServiceProvider) {
		super(FunctionInfo.class, workspace, groupServiceProvider);
	}

	public boolean exists(String path, String groupId){
		return infos.values().stream()
				.anyMatch(it -> groupId.equals(it.getGroupId()) && path.equals(it.getPath()));
	}

	public boolean existsWithoutId(String path, String groupId, String id){
		return infos.values().stream()
				.anyMatch(it -> !id.equals(it.getId()) && groupId.equals(it.getGroupId()) && path.equals(it.getPath()));
	}

	@Override
	public byte[] serialize(FunctionInfo info) {
		return super.serialize(info);
	}
}
