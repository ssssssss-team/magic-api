package org.ssssssss.magicapi.core.service;

import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.core.event.EventAction;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.event.GroupEvent;
import org.ssssssss.magicapi.core.event.MagicEvent;
import org.ssssssss.magicapi.core.exception.MagicAPIException;
import org.ssssssss.magicapi.core.model.MagicEntity;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class AbstractMagicDynamicRegistry<T extends MagicEntity> implements MagicDynamicRegistry<T> {

	/**
	 * 已缓存的映射信息
	 */
	private final Map<String, MappingNode<T>> mappings = new ConcurrentHashMap<>();

	protected final MagicResourceStorage<T> magicResourceStorage;

	public AbstractMagicDynamicRegistry(MagicResourceStorage<T> magicResourceStorage) {
		this.magicResourceStorage = magicResourceStorage;
	}

	@Override
	public boolean register(T entity) {
		MappingNode<T> mappingNode = buildMappingNode(entity);
		String newMappingKey = mappingNode.getMappingKey();
		MappingNode<T> oldMappingNode = mappings.get(entity.getId());
		if (oldMappingNode != null) {
			String oldMappingKey = oldMappingNode.getMappingKey();
			// mappingKey一致时 刷新即可
			if (Objects.equals(oldMappingKey, newMappingKey)) {
				if (!entity.equals(oldMappingNode.getEntity())) {
					// 刷新
					oldMappingNode.setEntity(entity);
				}
				return true;
			}
			// 不一致时，需要取消注册旧的，重新注册当前的
			mappings.remove(oldMappingKey);
			mappings.remove(entity.getId());
			unregister(oldMappingNode);
		} else if (mappings.containsKey(newMappingKey)) {
			throw new MagicAPIException(newMappingKey + " 已注册，请更换名称或路径");
		}
		if (register(mappingNode)) {
			mappings.put(entity.getId(), mappingNode);
			mappings.put(newMappingKey, mappingNode);
			return true;
		}
		return false;
	}

	@EventListener(condition = "#event.action == T(org.ssssssss.magicapi.core.event.EventAction).CLEAR")
	public void clear(MagicEvent event) {
		Iterator<Map.Entry<String, MappingNode<T>>> iterator = mappings.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, MappingNode<T>> entry = iterator.next();
			if (Objects.equals(entry.getKey(), entry.getValue().getEntity().getId())) {
				unregister(entry.getValue());
			}
			iterator.remove();
		}
	}

	protected void processEvent(FileEvent event) {
		T info = (T) event.getEntity();
		if (event.getAction() == EventAction.DELETE) {
			unregister(info);
		} else {
			register(info);
		}
	}

	protected void processEvent(GroupEvent event) {
		if (event.getAction() == EventAction.DELETE) {
			event.getEntities().forEach(entity -> unregister((T) entity));
		} else {
			event.getEntities().forEach(entity -> register((T) entity));
		}
	}

	@Override
	public T getMapping(String mappingKey) {
		MappingNode<T> node = mappings.get(mappingKey);
		return node == null ? null : node.getEntity();
	}

	@Override
	public boolean unregister(T entity) {
		MappingNode<T> mappingNode = mappings.remove(entity.getId());
		if (mappingNode != null) {
			mappings.remove(mappingNode.getMappingKey());
			unregister(mappingNode);
			return true;
		}
		return false;
	}

	@Override
	public MagicResourceStorage<T> getMagicResourceStorage() {
		return this.magicResourceStorage;
	}

	protected boolean register(MappingNode<T> mappingNode) {
		return true;
	}

	protected void unregister(MappingNode<T> mappingNode) {

	}

	public List<T> mappings(){
		return this.mappings.values().stream().map(MappingNode::getEntity).collect(Collectors.toList());
	}

	protected MappingNode<T> buildMappingNode(T entity) {
		MappingNode<T> mappingNode = new MappingNode<>(entity);
		mappingNode.setMappingKey(this.magicResourceStorage.buildKey(entity));
		return mappingNode;
	}

	protected static class MappingNode<T extends MagicEntity> {

		private T entity;

		private String mappingKey = "";

		private Object mappingData;

		public MappingNode(T entity) {
			this.entity = entity;
		}

		public T getEntity() {
			return entity;
		}

		public void setEntity(T entity) {
			this.entity = entity;
		}

		public String getMappingKey() {
			return mappingKey;
		}

		public void setMappingKey(String mappingKey) {
			this.mappingKey = mappingKey;
		}

		public Object getMappingData() {
			return mappingData;
		}

		public void setMappingData(Object mappingData) {
			this.mappingData = mappingData;
		}
	}
}
