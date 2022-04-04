package org.ssssssss.magicapi.core.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.config.JsonCodeConstants;
import org.ssssssss.magicapi.core.resource.Resource;
import org.ssssssss.magicapi.core.resource.ZipResource;
import org.ssssssss.magicapi.core.event.EventAction;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.event.GroupEvent;
import org.ssssssss.magicapi.core.event.MagicEvent;
import org.ssssssss.magicapi.core.exception.InvalidArgumentException;
import org.ssssssss.magicapi.core.model.*;
import org.ssssssss.magicapi.core.service.AbstractPathMagicResourceStorage;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.utils.IoUtils;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.magicapi.utils.WebUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DefaultMagicResourceService implements MagicResourceService, JsonCodeConstants, ApplicationListener<ApplicationStartedEvent> {

	private final Resource root;

	private final Map<String, Resource> groupMappings = new HashMap<>(16);

	private final Map<String, Group> groupCache = new HashMap<>(16);

	private final Map<String, Resource> fileMappings = new HashMap<>(32);

	private final Map<String, MagicEntity> fileCache = new HashMap<>(32);

	private final Map<String, Map<String, String>> pathCache = new HashMap<>(16);

	private final Map<String, MagicResourceStorage<? extends MagicEntity>> storages;

	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private final ApplicationEventPublisher publisher;

	private final Logger logger = LoggerFactory.getLogger(DefaultMagicResourceService.class);

	public DefaultMagicResourceService(Resource resource, List<MagicResourceStorage<? extends MagicEntity>> storages, ApplicationEventPublisher publisher) {
		this.root = resource;
		this.storages = storages.stream()
				.peek(it -> it.setMagicResourceService(this))
				.collect(Collectors.toMap(MagicResourceStorage::folder, it -> it));
		this.publisher = publisher;
	}

	public boolean processNotify(MagicNotify notify) {
		if (Constants.EVENT_TYPE_FILE.equals(notify.getType())) {
			return processFileNotify(notify.getId(), notify.getAction());
		}
		if (notify.getAction() == EventAction.CLEAR) {
			this.read(false);
			return true;
		}
		return processGroupNotify(notify.getId(), notify.getAction());
	}

	private boolean processGroupNotify(String id, EventAction action) {
		Group group = groupCache.get(id);
		if (group == null) {
			// create
			this.readAll();
			group = groupCache.get(id);
		}
		TreeNode<Group> treeNode = tree(group.getType()).findTreeNode(it -> it.getId().equals(id));
		if (treeNode != null) {
			GroupEvent event = new GroupEvent(group.getType(), action, group);
			event.setSource(Constants.EVENT_SOURCE_NOTIFY);
			if (event.getAction() == EventAction.DELETE) {
				event.setEntities(deleteGroup(id));
			} else if (action != EventAction.CREATE) {
				Resource folder = groupMappings.get(id);
				folder.readAll();
				if (folder.exists()) {
					// 刷新分组缓存
					refreshGroup(folder, storages.get(group.getType()));
				} else {
					this.readAll();
					treeNode = tree(group.getType()).findTreeNode(it -> it.getId().equals(id));
				}
				event.setGroup(groupCache.get(id));
				event.setEntities(treeNode
						.flat()
						.stream()
						.flatMap(g -> listFiles(g.getId()).stream())
						.collect(Collectors.toList()));
			}
			publisher.publishEvent(event);
			return true;
		}
		return false;
	}

	private boolean processFileNotify(String id, EventAction action) {
		MagicEntity entity = fileCache.get(id);
		if (entity == null) {    // create
			this.readAll();
			entity = fileCache.get(id);
		}
		if (entity != null) {
			Group group = groupCache.get(entity.getGroupId());
			if (group != null) {
				MagicResourceStorage<? extends MagicEntity> storage = storages.get(group.getType());
				Map<String, String> pathCacheMap = storage.requirePath() ? pathCache.get(storage.folder()) : null;
				if (action == EventAction.DELETE) {
					fileMappings.remove(id);
					entity = fileCache.remove(id);
					if (pathCacheMap != null) {
						pathCacheMap.remove(id);
					}
				} else {
					Resource resource = fileMappings.get(id);
					resource.readAll();
					if (resource.exists()) {
						entity = storage.read(resource.read());
						putFile(storage, entity, resource);
					} else {
						this.readAll();
						entity = fileCache.get(id);
					}
				}
				publisher.publishEvent(new FileEvent(group.getType(), action, entity, Constants.EVENT_SOURCE_NOTIFY));
			}
		}
		return false;
	}

	private void init() {
		groupMappings.clear();
		groupCache.clear();
		fileMappings.clear();
		fileCache.clear();
		pathCache.clear();
		storages.forEach((key, registry) -> {
			if (registry.requirePath()) {
				pathCache.put(registry.folder(), new HashMap<>(32));
			}
			Resource folder = root.getDirectory(key);
			if (registry.allowRoot()) {
				String rootId = key + ":0";
				Group group = new Group();
				group.setId(rootId);
				group.setType(key);
				group.setParentId("0");
				putGroup(group, folder);
			}
			if (!folder.exists()) {
				folder.mkdir();
			}
		});
	}

	private void read(boolean triggerEvent) {
		writeLock(() -> {
			if (triggerEvent) {
				publisher.publishEvent(new MagicEvent("clear", EventAction.CLEAR));
			}
			this.readAll();
			fileCache.values().forEach(entity -> {
				Group group = groupCache.get(entity.getGroupId());
				publisher.publishEvent(new FileEvent(group.getType(), EventAction.LOAD, entity));
			});
			return null;
		});
	}

	private void readAll() {
		writeLock(() -> {
			this.init();
			this.root.readAll();
			storages.forEach((key, registry) -> refreshGroup(root.getDirectory(key), registry));
			return null;
		});
	}

	@Override
	public void refresh() {
		this.read(true);
	}

	@Override
	public Resource getResource() {
		return root;
	}

	private void refreshGroup(Resource folder, MagicResourceStorage<? extends MagicEntity> storage) {
		if (storage.allowRoot()) {
			folder.files(storage.suffix()).forEach(file -> putFile(storage, storage.readResource(file), file));
		} else {
			folder.dirs().forEach(dir -> {
				Resource meta = dir.getResource(Constants.GROUP_METABASE);
				if (meta.exists()) {
					putGroup(JsonUtils.readValue(meta.read(), Group.class), dir);
					dir.files(storage.suffix()).forEach(file -> putFile(storage, storage.readResource(file), file));
				}
			});
		}
	}

	@Override
	public boolean saveGroup(Group group) {
		isTrue(!root.readonly(), IS_READ_ONLY);
		// 类型校验
		isTrue(storages.containsKey(group.getType()), NOT_SUPPORTED_GROUP_TYPE);
		// 名字校验
		notNull(group.getName(), NAME_REQUIRED);
		notNull(IoUtils.validateFileName(group.getName()), NAME_INVALID);
		// 需要填写parentId
		notNull(group.getParentId(), GROUP_ID_REQUIRED);
		MagicResourceStorage<? extends MagicEntity> storage = storages.get(group.getType());
		return writeLock(() -> {
			Resource resource;
			// 判断是否要保存到根节点下
			if (Constants.ROOT_ID.equals(group.getParentId())) {
				resource = root.getDirectory(group.getType());
			} else {
				// 找到上级分组
				resource = getGroupResource(group.getParentId());
				// 上级分组需要存在
				isTrue(resource != null && resource.exists(), GROUP_NOT_FOUND);
			}
			Resource groupResource;
			GroupEvent event = new GroupEvent(group.getType(), group.getId() == null ? EventAction.CREATE : EventAction.SAVE, group);
			if (group.getId() == null || !groupCache.containsKey(group.getId())) {
				// 添加分组
				if (group.getId() == null) {
					group.setId(UUID.randomUUID().toString().replace("-", ""));
				}
				group.setCreateTime(System.currentTimeMillis());
				group.setCreateBy(WebUtils.currentUserName());
				groupResource = resource.getDirectory(group.getName());
				// 判断分组文件夹需要不存在
				isTrue(!groupResource.exists(), FILE_SAVE_FAILURE);
				// 创建文件夹
				groupResource.mkdir();
			} else {
				Group oldGroup = groupCache.get(group.getId());
				if (storage.requirePath() && !Objects.equals(oldGroup.getPath(), group.getPath())) {
					TreeNode<Group> treeNode = tree(group.getType());
					String oldPath = oldGroup.getPath();
					oldGroup.setPath(group.getPath());
					// 递归找出该组下的文件
					List<MagicEntity> entities = treeNode.findTreeNode(it -> it.getId().equals(group.getId()))
							.flat()
							.stream()
							.flatMap(it -> fileCache.values().stream().filter(f -> f.getGroupId().equals(it.getId())))
							.collect(Collectors.toList());
					for (MagicEntity entity : entities) {
						String newMappingKey = storage.buildKey(entity);
						if (pathCache.get(group.getType()).entrySet().stream().anyMatch(entry -> entry.getValue().equals(newMappingKey) && !entry.getKey().equals(entity.getId()))) {
							// 还原path
							oldGroup.setPath(oldPath);
							throw new InvalidArgumentException(SAVE_GROUP_PATH_CONFLICT);
						}
					}
				}
				Resource oldResource = getGroupResource(group.getId());
				groupResource = resource.getDirectory(group.getName());
				isTrue(oldResource != null && oldResource.exists(), GROUP_NOT_FOUND);
				// 设置修改时间，修改人
				group.setUpdateBy(WebUtils.currentUserName());
				group.setUpdateTime(System.currentTimeMillis());
				// 名字不一样时重命名
				if (!Objects.equals(oldGroup.getName(), group.getName())) {
					// 判断分组文件夹需要不存在
					isTrue(!groupResource.exists(), FILE_SAVE_FAILURE);
					isTrue(oldResource.renameTo(groupResource), FILE_SAVE_FAILURE);
				}
			}
			// 写入分组信息
			if (groupResource.getResource(Constants.GROUP_METABASE).write(JsonUtils.toJsonString(group))) {
				putGroup(group, groupResource);
				TreeNode<Group> treeNode = tree(group.getType()).findTreeNode(it -> it.getId().equals(group.getId()));
				// 刷新分组缓存
				refreshGroup(groupResource, storage);
				if (event.getAction() != EventAction.CREATE) {
					event.setEntities(treeNode
							.flat()
							.stream()
							.flatMap(g -> listFiles(g.getId()).stream())
							.collect(Collectors.toList()));
				}
				publisher.publishEvent(event);
				return true;
			}
			return false;
		});
	}


	@Override
	public boolean move(String src, String groupId) {
		isTrue(!root.readonly(), IS_READ_ONLY);
		Group group = groupCache.get(groupId);
		isTrue("0".equals(groupId) || group != null, GROUP_NOT_FOUND);
		isTrue(!Objects.equals(src, groupId), MOVE_NAME_CONFLICT);
		return writeLock(() -> {
			Group srcGroup = groupCache.get(src);
			if (srcGroup != null) {
				// 移动分组
				return moveGroup(srcGroup, groupId);
			} else {
				// 不能将文件移动至根节点下
				notNull(group, GROUP_NOT_FOUND);
				MagicEntity entity = fileCache.get(src);
				notNull(entity, FILE_NOT_FOUND);
				// 移动文件
				return moveFile(entity.copy(), group);
			}
		});
	}

	@Override
	public String copyGroup(String src, String groupId) {
		isTrue(!root.readonly(), IS_READ_ONLY);
		Group group = groupCache.get(groupId);
		isTrue("0".equals(groupId) || group != null, GROUP_NOT_FOUND);
		isTrue(!Objects.equals(src, groupId), SRC_GROUP_CONFLICT);
		Group srcGroup = groupCache.get(src);
		notNull(srcGroup, GROUP_NOT_FOUND);
		Group newGroup = new Group();
		newGroup.setType(srcGroup.getType());
		newGroup.setParentId(groupId);
		newGroup.setName(srcGroup.getName() + "(Copy)");
		newGroup.setPath(Objects.toString(srcGroup.getPath(), "") + "_copy");
		newGroup.setOptions(srcGroup.getOptions());
		newGroup.setPaths(srcGroup.getPaths());
		newGroup.setProperties(srcGroup.getProperties());
		saveGroup(newGroup);
		listFiles(src).stream()
				.map(MagicEntity::copy)
				.peek(it -> it.setGroupId(newGroup.getId()))
				.peek(it -> it.setId(null))
				.forEach(this::saveFile);
		return newGroup.getId();
	}

	/**
	 * 移动分组
	 *
	 * @param src    分组信息
	 * @param target 目标分组ID
	 */
	private boolean moveGroup(Group src, String target) {
		isTrue(!root.readonly(), IS_READ_ONLY);
		MagicResourceStorage<?> storage = storages.get(src.getType());
		Resource targetResource = Constants.ROOT_ID.equals(target) ? this.root.getDirectory(storage.folder()) : groupMappings.get(target);
		// 校验分组名称是否有冲突
		isTrue(!targetResource.getDirectory(src.getName()).exists(), MOVE_NAME_CONFLICT);
		targetResource = targetResource.getDirectory(src.getName());
		TreeNode<Group> treeNode = tree(storage.folder());
		String oldParentId = src.getParentId();
		src.setParentId(target);
		// 递归找出要移动的文件
		List<MagicEntity> entities = treeNode.findTreeNode(it -> it.getId().equals(src.getId()))
				.flat()
				.stream()
				.flatMap(it -> fileCache.values().stream().filter(f -> f.getGroupId().equals(it.getId())))
				.collect(Collectors.toList());
		if (storage.requirePath()) {
			for (MagicEntity entity : entities) {
				String newMappingKey = storage.buildKey(entity);
				if (pathCache.get(src.getType()).entrySet().stream().anyMatch(entry -> entry.getValue().equals(newMappingKey) && !entry.getKey().equals(entity.getId()))) {
					// 还原parentId
					src.setParentId(oldParentId);
					throw new InvalidArgumentException(MOVE_PATH_CONFLICT);
				}
			}
		}
		// 设置修改时间，修改人
		src.setUpdateBy(WebUtils.currentUserName());
		src.setUpdateTime(System.currentTimeMillis());
		Resource oldResource = groupMappings.get(src.getId());
		if (oldResource.renameTo(targetResource)) {
			Resource resource = targetResource.getResource(Constants.GROUP_METABASE);
			if (resource.write(JsonUtils.toJsonString(src))) {
				// 更新group缓存
				putGroup(src, targetResource);
				// 更新mapping缓存
				if (storage.requirePath()) {
					Map<String, String> selfPathCache = pathCache.get(storage.folder());
					entities.forEach(entity -> selfPathCache.put(entity.getId(), storage.buildKey(entity)));
				}
				// 刷新缓存
				refreshGroup(targetResource, storage);
				publisher.publishEvent(new GroupEvent(src.getType(), EventAction.MOVE, src, entities));
				return true;
			}
		}
		return false;
	}

	/**
	 * 移动文件
	 *
	 * @param entity 文件信息
	 * @param group  目标分组
	 */
	private <T extends MagicEntity> boolean moveFile(T entity, Group group) {
		isTrue(!root.readonly(), IS_READ_ONLY);
		// 判断是否被锁定
		isTrue(!Constants.LOCK.equals(entity.getLock()), RESOURCE_LOCKED);
		// 设置新的分组ID
		entity.setGroupId(group.getId());
		MagicResourceStorage<?> storage = storages.get(group.getType());
		// 计算mappingKey
		String newMappingKey = storage.buildKey(entity);
		Resource resource = groupMappings.get(group.getId());
		// 判断名字和路径是否有冲突
		Resource newResource = resource.getResource(entity.getName() + storage.suffix());
		isTrue(!newResource.exists(), MOVE_NAME_CONFLICT);
		isTrue(!storage.requirePath() || pathCache.get(storage.folder()).entrySet().stream().noneMatch(entry -> entry.getValue().equals(newMappingKey) && !entry.getKey().equals(entity.getId())), MOVE_PATH_CONFLICT);
		// 设置修改时间，修改人
		entity.setUpdateBy(WebUtils.currentUserName());
		entity.setUpdateTime(System.currentTimeMillis());
		// 写入新文件
		if (newResource.write(storage.write(entity))) {
			// 删除旧文件
			fileMappings.remove(entity.getId()).delete();
			// 写入缓存
			putFile(storage, entity, newResource);
			publisher.publishEvent(new FileEvent(group.getType(), EventAction.MOVE, entity));
			return true;
		}
		return false;
	}

	@Override
	public TreeNode<Group> tree(String type) {
		return readLock(() -> groupCache.values().stream().filter(it -> type.equals(it.getType())).collect(Collectors.collectingAndThen(Collectors.toList(), this::convertToTree)));
	}

	@Override
	public Map<String, TreeNode<Group>> tree() {
		return readLock(() -> groupCache.values().stream().collect(Collectors.groupingBy(Group::getType, Collectors.collectingAndThen(Collectors.toList(), this::convertToTree))));
	}

	@Override
	public List<Group> getGroupsByFileId(String id) {
		return readLock(() -> {
			List<Group> groups = new ArrayList<>();
			MagicEntity entity = fileCache.get(id);
			if (entity != null) {
				Group group = groupCache.get(entity.getGroupId());
				while (group != null) {
					groups.add(group);
					group = groupCache.get(group.getParentId());
				}
			}
			return groups;
		});
	}

	private TreeNode<Group> convertToTree(List<Group> groups) {
		TreeNode<Group> root = new TreeNode<>();
		root.setNode(new Group("0", "root"));
		convertToTree(groups, root);
		return root;
	}

	private void convertToTree(List<Group> remains, TreeNode<Group> current) {
		Group temp;
		List<TreeNode<Group>> childNodes = new LinkedList<>();
		Iterator<Group> iterator = remains.iterator();
		while (iterator.hasNext()) {
			temp = iterator.next();
			if (current.getNode().getId().equals(temp.getParentId())) {
				childNodes.add(new TreeNode<>(temp));
				iterator.remove();
			}
		}
		current.setChildren(childNodes);
		childNodes.forEach(it -> convertToTree(remains, it));
	}


	@Override
	public Resource getGroupResource(String id) {
		return groupMappings.get(id);
	}

	@Override
	public <T extends MagicEntity> boolean saveFile(T entity) {
		isTrue(!root.readonly(), IS_READ_ONLY);
		// 校验必填信息
		notNull(entity.getGroupId(), GROUP_ID_REQUIRED);
		// 校验名字
		notBlank(entity.getName(), NAME_REQUIRED);
		isTrue(IoUtils.validateFileName(entity.getName()), NAME_INVALID);
		return writeLock(() -> {
			EventAction action = entity.getId() == null || !fileCache.containsKey(entity.getId()) ? EventAction.CREATE : EventAction.SAVE;
			// 获取所在分组
			Resource groupResource = getGroupResource(entity.getGroupId());
			// 分组需要存在
			notNull(groupResource, GROUP_NOT_FOUND);
			MagicResourceStorage<T> storage;
			if (entity.getGroupId().contains(":")) {
				storage = (MagicResourceStorage<T>) this.storages.get(entity.getGroupId().split(":")[0]);
			} else {
				// 读取分组信息
				Group group = groupCache.get(entity.getGroupId());
				storage = (MagicResourceStorage<T>) this.storages.get(group.getType());
			}

			// 检查脚本
			if (storage.requiredScript()) {
				notBlank(entity.getScript(), SCRIPT_REQUIRED);
			}
			// 路径检查
			if (storage.requirePath()) {
				notBlank(((PathMagicEntity) entity).getPath(), PATH_REQUIRED);
				String newMappingKey = storage.buildKey(entity);
				isTrue(pathCache.get(storage.folder()).entrySet().stream().noneMatch(entry -> entry.getValue().equals(newMappingKey) && !entry.getKey().equals(entity.getId())), PATH_CONFLICT);
			}
			storage.validate(entity);
			// 拼接文件名
			String filename = entity.getName() + storage.suffix();
			// 获取修改前的信息
			Resource fileResource = groupResource.getResource(filename);
			if (action == EventAction.CREATE) {
				if (entity.getId() == null) {
					isTrue(!fileResource.exists(), FILE_SAVE_FAILURE);
					// 新增操作赋值
					entity.setId(UUID.randomUUID().toString().replace("-", ""));
				}
				entity.setCreateTime(System.currentTimeMillis());
				entity.setCreateBy(WebUtils.currentUserName());
			} else {
				// 修改操作赋值
				entity.setUpdateTime(System.currentTimeMillis());
				entity.setUpdateBy(WebUtils.currentUserName());
				isTrue(!Constants.LOCK.equals(fileCache.get(entity.getId()).getLock()), RESOURCE_LOCKED);
				Resource oldFileResource = fileMappings.get(entity.getId());
				if (!oldFileResource.name().equals(fileResource.name())) {
					// 重命名
					isTrue(oldFileResource.renameTo(fileResource), FILE_SAVE_FAILURE);
				}
			}
			boolean flag = fileResource.write(storage.write(entity));
			if (flag) {
				publisher.publishEvent(new FileEvent(storage.folder(), action, entity));
				putFile(storage, entity, fileResource);
			}
			return flag;
		});
	}

	private List<MagicEntity> deleteGroup(String id) {
		isTrue(!root.readonly(), IS_READ_ONLY);
		Group group = groupCache.get(id);
		List<MagicEntity> entities = new ArrayList<>();
		// 递归删除分组和文件
		tree(group.getType())
				.findTreeNode(it -> it.getId().equals(id))
				.flat()
				.forEach(g -> {
					groupCache.remove(g.getId());
					groupMappings.remove(g.getId());
					fileCache.values().stream()
							.filter(f -> f.getGroupId().equals(g.getId())).peek(entities::add)
							.collect(Collectors.toList())
							.forEach(file -> {
								fileCache.remove(file.getId());
								fileMappings.remove(file.getId());
								Map<String, String> map = pathCache.get(g.getType());
								if (map != null) {
									map.remove(file.getId());
								}
							});
				});
		groupMappings.remove(id);
		groupCache.remove(id);
		return entities;
	}

	@Override
	public boolean delete(String id) {
		isTrue(!root.readonly(), IS_READ_ONLY);
		return writeLock(() -> {
			Resource resource = getGroupResource(id);
			if (resource != null) {
				// 删除分组
				if (resource.exists() && resource.delete()) {
					Group group = groupCache.get(id);
					GroupEvent event = new GroupEvent(groupCache.get(group.getId()).getType(), EventAction.DELETE, group);
					event.setEntities(deleteGroup(id));
					publisher.publishEvent(event);
					return true;
				}
			}
			resource = fileMappings.get(id);
			// 删除文件
			if (resource != null && resource.exists() && resource.delete()) {
				MagicEntity entity = fileCache.remove(id);
				String type = groupCache.get(entity.getGroupId()).getType();
				publisher.publishEvent(new FileEvent(type, EventAction.DELETE, entity));
				fileMappings.remove(id);
				fileCache.remove(id);
				Map<String, String> map = pathCache.get(type);
				if (map != null) {
					map.remove(id);
				}

			}
			return true;
		});
	}

	@Override
	public <T extends MagicEntity> List<T> listFiles(String groupId) {
		return readLock(() -> {
			Group group = groupCache.get(groupId);
			notNull(group, GROUP_NOT_FOUND);
			return fileCache.values().stream()
					.filter(it -> it.getGroupId().equals(groupId))
					.map(it -> (T) it)
					.collect(Collectors.toList());
		});
	}

	@Override
	public <T extends MagicEntity> List<T> files(String type) {
		MagicResourceStorage<? extends MagicEntity> storage = storages.get(type);
		Resource directory = root.getDirectory(type);
		if (directory.exists()) {
			return directory.files(storage.suffix()).stream()
					.map(storage::readResource)
					.map(it -> (T) it)
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public <T extends MagicEntity> T file(String id) {
		return (T) fileCache.get(id);
	}

	@Override
	public Group getGroup(String id) {
		return groupCache.get(id);
	}

	@Override
	public void export(String groupId, List<SelectedResource> resources, OutputStream os) throws IOException {
		if (StringUtils.isNotBlank(groupId)) {
			Resource resource = getGroupResource(groupId);
			notNull(resource, GROUP_NOT_FOUND);
			resource.export(os);
		} else if (resources == null || resources.isEmpty()) {
			root.export(os);
		} else {
			ZipOutputStream zos = new ZipOutputStream(os);
			for (SelectedResource item : resources) {
				if ("root".equals(item.getType())) {
					zos.putNextEntry(new ZipEntry(item.getId() + "/"));
				} else if ("group".equals(item.getType())) {
					Resource resource = getGroupResource(item.getId());
					notNull(resource, GROUP_NOT_FOUND);
					zos.putNextEntry(new ZipEntry(resource.getFilePath()));
					zos.closeEntry();
					resource = resource.getResource(Constants.GROUP_METABASE);
					zos.putNextEntry(new ZipEntry(resource.getFilePath()));
					zos.write(resource.read());
					zos.closeEntry();
				} else {
					Resource resource = fileMappings.get(item.getId());
					MagicEntity entity = fileCache.get(item.getId());
					notNull(entity, FILE_NOT_FOUND);
					Resource groupResource = groupMappings.get(entity.getGroupId());
					Group group = groupCache.get(entity.getGroupId());
					MagicResourceStorage<? extends MagicEntity> storage = storages.get(group.getType());
					zos.putNextEntry(new ZipEntry(groupResource.getFilePath() + entity.getName() + storage.suffix()));
					zos.write(resource.read());
					zos.closeEntry();
				}
			}
			zos.flush();
			zos.close();
		}
	}

	@Override
	public boolean lock(String id) {
		return doLockResource(id, Constants.LOCK);
	}

	private boolean doLockResource(String id, String lockState) {
		isTrue(!root.readonly(), IS_READ_ONLY);
		return writeLock(() -> {
			MagicEntity entity = fileCache.get(id);
			Resource resource = fileMappings.get(id);
			notNull(entity, FILE_NOT_FOUND);
			notNull(resource, FILE_NOT_FOUND);
			Group group = groupCache.get(entity.getGroupId());
			notNull(group, GROUP_NOT_FOUND);
			entity.setLock(lockState);
			entity.setUpdateTime(System.currentTimeMillis());
			entity.setUpdateBy(WebUtils.currentUserName());
			MagicResourceStorage<? extends MagicEntity> storage = storages.get(group.getType());
			boolean flag = resource.write(storage.write(entity));
			if (flag) {
				putFile(storage, entity, resource);
			}
			return flag;
		});
	}

	@Override
	public boolean unlock(String id) {
		return doLockResource(id, Constants.UNLOCK);
	}

	@Override
	public boolean upload(InputStream inputStream, boolean full) throws IOException {
		isTrue(!root.readonly(), IS_READ_ONLY);
		try {
			ZipResource zipResource = new ZipResource(inputStream);
			Set<Group> groups = new LinkedHashSet<>();
			Set<MagicEntity> entities = new LinkedHashSet<>();
			return writeLock(() -> {
				readAllResource(zipResource, groups, entities, !full);
				if (full) {
					// 全量模式先删除处理。
					root.delete();
					this.init();
					publisher.publishEvent(new MagicEvent("clear", EventAction.CLEAR));
				}
				for (Group group : groups) {
					saveGroup(group);
				}
				for (MagicEntity entity : entities) {
					saveFile(entity);
				}
				return true;
			});
		} finally {
			IoUtils.close(inputStream);
		}

	}

	private void readAllResource(Resource root, Set<Group> groups, Set<MagicEntity> entities, boolean checked) {
		Resource resource = root.getResource(Constants.GROUP_METABASE);
		MagicResourceStorage<? extends MagicEntity> storage = null;
		if (resource.exists()) {
			Group group = JsonUtils.readValue(resource.read(), Group.class);
			group.setType(mappingV1Type(group.getType()));
			storage = storages.get(group.getType());
			notNull(storage, NOT_SUPPORTED_GROUP_TYPE);
		}
		readAllResource(root, storage, groups, entities, null, "/", checked);
	}

	private void readAllResource(Resource root, MagicResourceStorage<? extends MagicEntity> storage, Set<Group> groups, Set<MagicEntity> entities, Set<String> mappingKeys, String path, boolean checked) {
		storage = storage == null ? storages.get(root.name()) : storage;
		if (storage != null) {
			mappingKeys = mappingKeys == null ? new HashSet<>() : mappingKeys;
			if (!storage.allowRoot()) {
				Resource resource = root.getResource(Constants.GROUP_METABASE);
				// 分组信息不存在时，直接返回不处理
				if (resource.exists()) {
					// 读取分组信息
					Group group = JsonUtils.readValue(resource.read(), Group.class);
					group.setType(mappingV1Type(group.getType()));
					groups.add(group);
					if (storage.requirePath()) {
						path = path + Objects.toString(group.getPath(), "") + "/";
					}
				}

			}
			for (Resource file : root.files(storage.suffix())) {
				MagicEntity entity = storage.read(file.read());
				if (storage.allowRoot()) {
					entity.setGroupId(storage.folder() + ":0");
				}
				String mappingKey;
				if (storage instanceof AbstractPathMagicResourceStorage) {
					mappingKey = ((AbstractPathMagicResourceStorage) storage).buildMappingKey((PathMagicEntity) entity, path);
				} else {
					mappingKey = storage.buildKey(entity);
				}
				if (checked) {
					String groupId = entity.getGroupId();
					// 名字和路径冲突检查
					fileCache.values().stream()
							// 同一分组
							.filter(it -> Objects.equals(it.getGroupId(), groupId))
							// 非自身
							.filter(it -> !it.getId().equals(entity.getId()))
							.forEach(it -> isTrue(!Objects.equals(it.getName(), entity.getName()), RESOURCE_PATH_CONFLICT.format(entity.getName())));

				}
				// 自检
				isTrue(mappingKeys.add(mappingKey), RESOURCE_PATH_CONFLICT.format(mappingKey));
				entities.add(entity);
			}

		}
		for (Resource directory : root.dirs()) {
			readAllResource(directory, storage, groups, entities, mappingKeys, path, checked);
		}
	}

	/**
	 * 兼容1.x版本
	 */
	private String mappingV1Type(String type) {
		if ("1".equals(type)) {
			return "api";
		} else if ("2".equals(type)) {
			return "function";
		}
		return type;

	}

	@Override
	public String getGroupName(String groupId) {
		return findGroups(groupId).stream()
				.map(Group::getName)
				.collect(Collectors.joining("/"));
	}

	@Override
	public String getGroupPath(String groupId) {
		return findGroups(groupId).stream()
				.map(Group::getPath)
				.filter(StringUtils::isNotBlank)
				.collect(Collectors.joining("/"));
	}

	private List<Group> findGroups(String groupId) {
		return readLock(() -> {
			List<Group> groups = new ArrayList<>();
			String key = groupId;
			while (groupCache.containsKey(key)) {
				Group group = groupCache.get(key);
				groups.add(0, group);
				key = group.getParentId();
			}
			return groups;
		});
	}

	private void putGroup(Group group, Resource resource) {
		groupMappings.put(group.getId(), resource);
		groupCache.put(group.getId(), group);
	}

	private void putFile(MagicResourceStorage<?> storage, MagicEntity entity, Resource resource) {
		fileMappings.put(entity.getId(), resource);
		fileCache.put(entity.getId(), entity);
		if (storage.requirePath()) {
			pathCache.get(storage.folder()).put(entity.getId(), storage.buildKey(entity));
		}
	}

	private <R> R readLock(Supplier<R> supplier) {
		try {
			lock.readLock().lock();
			return supplier.get();
		} finally {
			lock.readLock().unlock();
		}
	}

	private <R> R writeLock(Supplier<R> supplier) {
		try {
			lock.writeLock().lock();
			return supplier.get();
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
		try {
			this.read(false);
		} catch (Exception e) {
			logger.error("启动过程中发生异常", e);
		}
	}
}
