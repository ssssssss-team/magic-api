package org.ssssssss.magicapi.service.impl;

import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.event.FileEvent;
import org.ssssssss.magicapi.model.DataSourceInfo;
import org.ssssssss.magicapi.provider.MagicResourceStorage;
import org.ssssssss.magicapi.service.AbstractMagicDynamicRegistry;

public class DataSourceMagicDynamicRegistry extends AbstractMagicDynamicRegistry<DataSourceInfo> {

	public DataSourceMagicDynamicRegistry(MagicResourceStorage<DataSourceInfo> magicResourceStorage) {
		super(magicResourceStorage);
	}

	@EventListener(condition = "#event.type == 'datasource'")
	public void onFileEvent(FileEvent event) {
		processEvent(event);
	}

	@Override
	public boolean register(DataSourceInfo info) {
		// mapping.register(mappingNode.getRequestMappingInfo());
		System.out.println("注册数据源：" + info.getKey());
		return true;
	}


	@Override
	public boolean unregister(DataSourceInfo info) {
		System.out.println("取消注册数据源：" + info.getKey());
		return true;
	}

}
