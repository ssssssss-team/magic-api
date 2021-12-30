package org.ssssssss.magicapi.service.impl;

import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.event.FileEvent;
import org.ssssssss.magicapi.event.GroupEvent;
import org.ssssssss.magicapi.model.FunctionInfo;
import org.ssssssss.magicapi.provider.MagicResourceStorage;
import org.ssssssss.magicapi.service.AbstractMagicDynamicRegistry;

public class FunctionMagicDynamicRegistry extends AbstractMagicDynamicRegistry<FunctionInfo> {

	public FunctionMagicDynamicRegistry(MagicResourceStorage<FunctionInfo> magicResourceStorage) {
		super(magicResourceStorage);
	}

	@EventListener(condition = "#event.type == 'function'")
	public void onFileEvent(FileEvent event) {
		processEvent(event);
	}

	@EventListener(condition = "#event.type == 'function'")
	public void onGroupEvent(GroupEvent event) {
		processEvent(event);
	}

	@Override
	public boolean register(MappingNode<FunctionInfo> mappingNode) {
		// mapping.register(mappingNode.getRequestMappingInfo());
		System.out.println("注册：" + mappingNode.getMappingKey());
		return true;
	}


	@Override
	protected void unregister(MappingNode<FunctionInfo> mappingNode) {
		System.out.println("取消注册：" + mappingNode.getMappingKey());
	}

}
