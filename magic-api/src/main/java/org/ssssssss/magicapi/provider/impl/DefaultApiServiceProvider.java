package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.provider.ApiServiceProvider;
import org.ssssssss.magicapi.provider.GroupServiceProvider;

public class DefaultApiServiceProvider extends ApiServiceProvider {

	public DefaultApiServiceProvider(GroupServiceProvider groupServiceProvider, Resource workspace) {
		super(workspace.getResource(Constants.PATH_API), groupServiceProvider);
	}
}
