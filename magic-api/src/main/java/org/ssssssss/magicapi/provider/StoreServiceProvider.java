package org.ssssssss.magicapi.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.adapter.Resource;
import org.ssssssss.magicapi.model.Constants;
import org.ssssssss.magicapi.model.MagicEntity;
import org.ssssssss.magicapi.utils.JsonUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public abstract class StoreServiceProvider<T extends MagicEntity> {

	private static final Logger logger = LoggerFactory.getLogger(StoreServiceProvider.class);
	protected Resource workspace;
	protected Resource backupResource;
	protected Map<String, Resource> mappings = new HashMap<>();
	protected Map<String, T> infos = new HashMap<>();
	protected GroupServiceProvider groupServiceProvider;
	protected Class<T> clazz;
	String separatorWithCRLF = "\r\n================================\r\n";
	String separatorWithLF = "\n================================\n";

	public StoreServiceProvider(Class<T> clazz, Resource workspace, GroupServiceProvider groupServiceProvider) {
		this.clazz = clazz;
		this.workspace = workspace;
		this.groupServiceProvider = groupServiceProvider;
		if (!this.workspace.exists()) {
			this.workspace.mkdir();
		}
		this.backupResource = this.workspace.parent().getDirectory(Constants.PATH_BACKUPS);
		if (!this.backupResource.exists()) {
			this.backupResource.mkdir();
		}
	}


	/**
	 * 添加
	 */
	public boolean insert(T info) {
		info.setId(UUID.randomUUID().toString().replace("-", ""));
		info.setUpdateTime(System.currentTimeMillis());
		info.setCreateTime(info.getUpdateTime());
		Resource dest = groupServiceProvider.getGroupResource(info.getGroupId()).getResource(info.getName() + ".ms");
		if (!dest.exists() && dest.write(serialize(info))) {
			mappings.put(info.getId(), dest);
			infos.put(info.getId(), info);
			return true;
		}
		return false;
	}

	/**
	 * 备份历史记录
	 */
	public boolean backup(T info) {
		Resource directory = this.backupResource.getDirectory(info.getId());
		if (!directory.readonly() && (directory.exists() || directory.mkdir())) {
			Resource resource = directory.getResource(String.format("%s.ms", System.currentTimeMillis()));
			try {
				return resource.write(serialize(info));
			} catch (Exception e) {
				logger.warn("保存历史记录失败,{}", e.getMessage());
			}
		}
		return false;
	}


	/**
	 * 查询历史记录
	 *
	 * @return 时间戳列表
	 */
	public List<Long> backupList(String id) {
		Resource directory = this.backupResource.getDirectory(id);
		List<Resource> resources = directory.files(".ms");
		return resources.stream()
				.map(it -> Long.valueOf(it.name().replace(".ms", "")))
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList());
	}

	/**
	 * 查询历史记录详情
	 *
	 * @param id        ID
	 * @param timestamp 时间戳
	 */
	public T backupInfo(String id, Long timestamp) {
		Resource directory = this.backupResource.getDirectory(id);
		if (directory.exists()) {
			Resource resource = directory.getResource(String.format("%s.ms", timestamp));
			if (resource.exists()) {
				return deserialize(resource.read());
			}
		}
		return null;
	}

	/**
	 * 修改
	 */
	public boolean update(T info) {
		Resource dest = groupServiceProvider.getGroupResource(info.getGroupId()).getResource(info.getName() + ".ms");
		Resource src = mappings.get(info.getId());
		if (!src.name().equals(dest.name())) {
			if (dest.exists()) {
				return false;
			}
			src.renameTo(dest);
		}
		info.setUpdateTime(System.currentTimeMillis());
		if (dest.write(serialize(info))) {
			mappings.put(info.getId(), dest);
			infos.put(info.getId(), info);
			return true;
		}
		return false;
	}

	/**
	 * 删除
	 */
	public boolean delete(String id) {
		Resource resource = mappings.get(id);
		if (resource != null && resource.delete()) {
			mappings.remove(id);
			infos.remove(id);
			return true;
		}
		return false;
	}

	/**
	 * 查询所有（提供给页面,无需带script）
	 */
	public List<T> list() {
		List<T> infos = listWithScript();
		infos.forEach(info -> info.setScript(null));
		return infos;
	}

	/**
	 * 重新加载分组
	 */
	public boolean reload(String groupId) {
		Resource dest = groupServiceProvider.getGroupResource(groupId);
		if(dest != null){
			dest.files(".ms").forEach(r -> {
				T info = deserialize(r.read());
				infos.put(info.getId(), info);
				mappings.put(info.getId(), r);
			});
			return true;
		}
		return false;
	}

	/**
	 * 查询所有（内部使用，需要带Script）
	 */
	public List<T> listWithScript() {
		workspace.readAll();
		List<Resource> resources = workspace.files(".ms");
		Map<String, Resource> mappings = new HashMap<>();
		Map<String, T> infos = new HashMap<>();
		List<T> result = resources.stream().map(r -> {
			T info = deserialize(r.read());
			infos.put(info.getId(), info);
			mappings.put(info.getId(), r);
			return (T) info.clone();
		}).collect(Collectors.toList());
		this.mappings = mappings;
		this.infos = infos;
		return result;
	}

	/**
	 * 查询详情（主要给页面使用）
	 *
	 * @param id ID
	 */
	public T get(String id) {
		return infos.get(id);
	}

	/**
	 * 判断是否允许移动
	 */
	public boolean allowMove(String id, String groupId) {
		Resource resource = mappings.get(id);
		if (resource == null) {
			return false;
		}
		return !resource.readonly() && !groupServiceProvider.getGroupResource(groupId).getResource(resource.name()).exists();
	}

	/**
	 * 移动
	 *
	 * @param id      接口ID
	 * @param groupId 分组ID
	 */
	public boolean move(String id, String groupId) {
		Resource dest = groupServiceProvider.getGroupResource(groupId);
		Resource src = mappings.get(id);
		dest = dest.getResource(src.name());
		if (dest.exists()) {
			return false;
		}
		T info = infos.get(id);
		src.renameTo(dest);
		info.setGroupId(groupId);
		mappings.put(id, dest);
		return dest.write(serialize(info));
	}

	/**
	 * 根据组ID删除
	 */
	public boolean deleteGroup(String rootId, List<String> groupIds) {
		if (!groupServiceProvider.getGroupResource(rootId).delete()) {
			return false;
		}
		for (String groupId : groupIds) {
			List<String> infoIds = infos.values().stream().filter(info -> groupId.equals(info.getGroupId()))
					.map(T::getId)
					.collect(Collectors.toList());
			infoIds.forEach(infos::remove);
			infoIds.forEach(mappings::remove);
		}
		return true;
	}

	public List<String> getIdsWithoutIds(List<String> idList){
		return this.mappings.keySet().stream()
				.filter(id -> !idList.contains(id))
				.collect(Collectors.toList());
	}

	/**
	 * 包装信息（可用于加密）
	 */
	public void wrap(T info) {
	}

	/**
	 * 解除包装信息（可用于解密）
	 */
	public void unwrap(T info) {
	}

	public byte[] serialize(T info) {
		wrap(info);
		String script = info.getScript();
		info.setScript(null);
		String content = JsonUtils.toJsonString(info) + separatorWithCRLF + script;
		info.setScript(script);
		unwrap(info);
		return content.getBytes(StandardCharsets.UTF_8);
	}

	public T deserialize(byte[] data) {
		String content = new String(data, StandardCharsets.UTF_8);
		String separator = separatorWithCRLF;
		int index = content.indexOf(separator);
		if (index == -1) {
			separator = separatorWithLF;
			index = content.indexOf(separatorWithLF);
		}
		if (index > -1) {
			T info = JsonUtils.readValue(content.substring(0, index), clazz);
			info.setScript(content.substring(index + separator.length()));
			unwrap(info);
			return info;
		}
		logger.warn("文件内容格式错误，请检查。");
		return null;
	}
}
