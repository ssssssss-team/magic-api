package org.ssssssss.magicapi.provider.impl;

import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.provider.FunctionServiceProvider;
import org.ssssssss.magicapi.provider.GroupServiceProvider;

public class DefaultFunctionServiceProvider extends FunctionServiceProvider {


	public DefaultFunctionServiceProvider(GroupServiceProvider groupServiceProvider, Resource workspace) {
		super(workspace.getResource("function"), groupServiceProvider);
	}


}
