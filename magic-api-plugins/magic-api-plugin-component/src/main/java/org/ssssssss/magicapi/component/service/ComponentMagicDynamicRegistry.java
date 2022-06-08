package org.ssssssss.magicapi.component.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.component.model.ComponentInfo;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.event.GroupEvent;
import org.ssssssss.magicapi.core.service.AbstractMagicDynamicRegistry;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;

public class ComponentMagicDynamicRegistry extends AbstractMagicDynamicRegistry<ComponentInfo> {

	public ComponentMagicDynamicRegistry(MagicResourceStorage<ComponentInfo> magicResourceStorage) {
		super(magicResourceStorage);
	}

	@EventListener(condition = "#event.type == 'component'")
	public void onFileEvent(FileEvent event) {
		processEvent(event);
	}

	@EventListener(condition = "#event.type == 'component'")
	public void onGroupEvent(GroupEvent event) {
		processEvent(event);
	}

}
