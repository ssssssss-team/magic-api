package org.ssssssss.magicapi.provider;

import org.ssssssss.magicapi.model.FunctionInfo;

public interface FunctionServiceProvider extends StoreServiceProvider<FunctionInfo> {

	boolean exists(String path, String groupId);

	boolean existsWithoutId(String path, String groupId, String id);

}
