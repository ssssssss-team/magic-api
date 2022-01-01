package org.ssssssss.magicapi.controller;

import org.springframework.web.bind.annotation.*;
import org.ssssssss.magicapi.config.MagicConfiguration;
import org.ssssssss.magicapi.exception.InvalidArgumentException;
import org.ssssssss.magicapi.interceptor.Authorization;
import org.ssssssss.magicapi.model.*;
import org.ssssssss.magicapi.service.MagicDynamicRegistry;
import org.ssssssss.magicapi.service.MagicResourceService;
import org.ssssssss.magicapi.utils.IoUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class MagicResourceController extends MagicController implements MagicExceptionHandler {

	private MagicResourceService service;

	public MagicResourceController(MagicConfiguration configuration) {
		super(configuration);
		this.service = MagicConfiguration.getMagicResourceService();
	}

	@PostMapping("/resource/folder/save")
	@ResponseBody
	public JsonBean<String> saveFolder(@RequestBody Group group) {
		if (service.saveGroup(group)) {
			return new JsonBean<>(group.getId());
		}
		return new JsonBean<>((String) null);
	}

	@PostMapping("/resource/folder/copy")
	@ResponseBody
	public JsonBean<String> saveFolder(String src, String target) {
		return new JsonBean<>(service.copyGroup(src, target));
	}

	@PostMapping("/resource/delete")
	@ResponseBody
	public JsonBean<Boolean> delete(String id, HttpServletRequest request) {
		return new JsonBean<>(service.delete(id));
	}

	@PostMapping("/resource/file/{folder}/save")
	@ResponseBody
	public JsonBean<String> saveFile(@PathVariable("folder") String folder, HttpServletRequest request) throws IOException {
		byte[] bytes = IoUtils.bytes(request.getInputStream());
		MagicEntity entity = configuration.getMagicDynamicRegistries().stream()
				.map(MagicDynamicRegistry::getMagicResourceStorage)
				.filter(it -> Objects.equals(it.folder(), folder))
				.findFirst()
				.orElseThrow(() -> new InvalidArgumentException(GROUP_NOT_FOUND))
				.read(bytes);
		isTrue(allowVisit(request, Authorization.SAVE, entity), PERMISSION_INVALID);
		if (MagicConfiguration.getMagicResourceService().saveFile(entity)) {
			return new JsonBean<>(entity.getId());
		}
		return new JsonBean<>(null);
	}

	@GetMapping("/resource/file/{id}")
	@ResponseBody
	public JsonBean<MagicEntity> detail(@PathVariable("id") String id, HttpServletRequest request) {
		MagicEntity entity = MagicConfiguration.getMagicResourceService().file(id);
		isTrue(allowVisit(request, Authorization.VIEW, entity), PERMISSION_INVALID);
		return new JsonBean<>(MagicConfiguration.getMagicResourceService().file(id));
	}

	@PostMapping("/resource/move")
	@ResponseBody
	public JsonBean<Boolean> move(String src, String groupId) {
		return new JsonBean<>(service.move(src, groupId));
	}

	@PostMapping("/resource/lock")
	@ResponseBody
	public JsonBean<Boolean> lock(String id, HttpServletRequest request) {
		MagicEntity entity = service.file(id);
		isTrue(allowVisit(request, Authorization.LOCK, entity), PERMISSION_INVALID);
		return new JsonBean<>(service.lock(id));
	}

	@PostMapping("/resource/unlock")
	@ResponseBody
	public JsonBean<Boolean> unlock(String id, HttpServletRequest request) {
		MagicEntity entity = service.file(id);
		isTrue(allowVisit(request, Authorization.UNLOCK, entity), PERMISSION_INVALID);
		return new JsonBean<>(service.unlock(id));
	}

	@GetMapping("/resource")
	@ResponseBody
	public JsonBean<Map<String, TreeNode<Attributes<Object>>>> resources() {
		Map<String, TreeNode<Group>> tree = service.tree();
		Map<String, TreeNode<Attributes<Object>>> result = new HashMap<>();
		tree.forEach((key, value) -> result.put(key, process(value)));
		return new JsonBean<>(result);
	}

	private TreeNode<Attributes<Object>> process(TreeNode<Group> groupNode) {
		TreeNode<Attributes<Object>> value = new TreeNode<>();
		value.setNode(groupNode.getNode());
		groupNode.getChildren().stream().map(this::process).forEach(value::addChild);
		if (!Constants.ROOT_ID.equals(groupNode.getNode().getId())) {
			service
					.listFiles(groupNode.getNode().getId())
					.stream()
					.map(MagicEntity::simple)
					.map((Function<MagicEntity, TreeNode<Attributes<Object>>>) TreeNode::new)
					.forEach(value::addChild);
		}
		return value;
	}
}
