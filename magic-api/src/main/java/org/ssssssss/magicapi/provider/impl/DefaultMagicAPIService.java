package org.ssssssss.magicapi.provider.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.ssssssss.magicapi.config.WebSocketSessionManager;
import org.ssssssss.magicapi.controller.MagicWebSocketDispatcher;
import org.ssssssss.magicapi.event.EventAction;
import org.ssssssss.magicapi.event.MagicEvent;
import org.ssssssss.magicapi.exception.MagicResourceNotFoundException;
import org.ssssssss.magicapi.model.*;
import org.ssssssss.magicapi.provider.MagicAPIService;
import org.ssssssss.magicapi.provider.ResultProvider;
import org.ssssssss.magicapi.service.MagicResourceService;
import org.ssssssss.magicapi.utils.SignUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

public class DefaultMagicAPIService implements MagicAPIService, JsonCodeConstants {

	private final static Logger logger = LoggerFactory.getLogger(DefaultMagicAPIService.class);
	private final boolean throwException;
	private final ResultProvider resultProvider;
	private final String instanceId;
	private final MagicResourceService resourceService;

	private final ApplicationEventPublisher publisher;

	public DefaultMagicAPIService(ResultProvider resultProvider,
								  String instanceId,
								  MagicResourceService resourceService,
								  boolean throwException,
								  ApplicationEventPublisher publisher) {
		this.resultProvider = resultProvider;
		this.throwException = throwException;
		this.resourceService = resourceService;
		this.instanceId = instanceId;
		this.publisher = publisher;
	}

	private <T> T execute(ApiInfo info, Map<String, Object> context) {

		// 获取原上下文
//		final MagicScriptContext magicScriptContext = MagicScriptContext.get();
//		MagicScriptContext scriptContext = new MagicScriptContext();
//		scriptContext.setScriptName(groupServiceProvider.getScriptName(info.getGroupId(), info.getName(), info.getPath()));
//		scriptContext.putMapIntoContext(context);
//		final Object evalVal;
//		try {
//			evalVal = ScriptManager.executeScript(info.getScript(), scriptContext);
//		} finally {
//			// 恢复原接口上下文，修复当前调完其它接口后原接口上下文丢失的问题
//			MagicScriptContext.set(magicScriptContext);
//		}
//		return evalVal;
		return null;
	}

	@Override
	public <T> T execute(String method, String path, Map<String, Object> context) {
//		ApiInfo info = this.mappingRegistry.getApiInfo(method, path);
//		if (info == null) {
//			throw new MagicServiceException(String.format("找不到对应接口 [%s:%s]", method, path));
//		}
//		return execute(info, context);
		return null;
	}

	@Override
	public <T> T call(String method, String path, Map<String, Object> context) {
		RequestEntity requestEntity = RequestEntity.create();
		try {
			return (T) resultProvider.buildResult(requestEntity, (Object) execute(method, path, context));
		} catch (MagicResourceNotFoundException e) {
			return null;    //找不到对应接口
		} catch (Throwable root) {
			if (throwException) {
				throw root;
			}
			return (T) resultProvider.buildResult(requestEntity, root);
		}
	}

	@Override
	public <T> T invoke(String path, Map<String, Object> context) {
//		FunctionInfo functionInfo = functionRegistry.getFunctionInfo(path);
//		if (functionInfo == null) {
//			throw new MagicServiceException(String.format("找不到对应函数 [%s]", path));
//		}
//		MagicScriptContext scriptContext = new MagicScriptContext(context);
//		scriptContext.setScriptName(groupServiceProvider.getScriptName(functionInfo.getGroupId(), functionInfo.getName(), functionInfo.getPath()));
//		scriptContext.putMapIntoContext(context);
//		return ScriptManager.executeScript(functionInfo.getScript(), scriptContext);
		return null;
	}

	@Override
	public boolean save(Group group) {
		return resourceService.saveGroup(group);
	}

	@Override
	public boolean delete(String src) {
		return resourceService.delete(src);
	}

	@Override
	public Map<String, TreeNode<Object>> resources() {
		return null;
	}

	@Override
	public Group getGroup(String id) {
		return null;
	}

	@Override
	public <T extends MagicEntity> T getDetail(String id) {
		return null;
	}

	@Override
	public boolean move(String src, String groupId) {
		return resourceService.move(src, groupId);
	}

	@Override
	public boolean upload(InputStream inputStream, String mode) throws IOException {
		return resourceService.upload(inputStream, Constants.UPLOAD_MODE_FULL.equals(mode));
	}

	@Override
	public void download(String groupId, List<SelectedResource> resources, OutputStream os) throws IOException {
		resourceService.export(groupId, resources, os);
	}

	@Override
	public JsonBean<?> push(String target, String secretKey, String mode, List<SelectedResource> resources) {
		notBlank(target, TARGET_IS_REQUIRED);
		notBlank(secretKey, SECRET_KEY_IS_REQUIRED);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			download(null, resources, baos);
		} catch (IOException e) {
			return new JsonBean<>(-1, e.getMessage());
		}
		byte[] bytes = baos.toByteArray();
		long timestamp = System.currentTimeMillis();
		RestTemplate restTemplate = new RestTemplate();
		MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
		param.add("timestamp", timestamp);
		param.add("mode", mode);
		param.add("sign", SignUtils.sign(timestamp, secretKey, mode, bytes));
		param.add("file", new InputStreamResource(new ByteArrayInputStream(bytes)) {
			@Override
			public String getFilename() {
				return "magic-api.zip";
			}

			@Override
			public long contentLength() {
				return bytes.length;
			}
		});
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		return restTemplate.postForObject(target, new HttpEntity<>(param, headers), JsonBean.class);
	}

	@Override
	public boolean processNotify(MagicNotify magicNotify) {
		if (magicNotify == null || instanceId.equals(magicNotify.getFrom())) {
			return false;
		}
		logger.info("收到通知消息:{}", magicNotify);
		switch (magicNotify.getAction()) {
			case WS_C_S:
				return processWebSocketMessageReceived(magicNotify.getClientId(), magicNotify.getContent());
			case WS_S_C:
				return processWebSocketSendMessage(magicNotify.getClientId(), magicNotify.getContent());
			case CLEAR:
				publisher.publishEvent(new MagicEvent("clear", EventAction.CLEAR, Constants.EVENT_SOURCE_NOTIFY));
		}
		return resourceService.processNotify(magicNotify);
	}

	@Override
	public String copyGroup(String src, String target) {
		return null;
	}

	@Override
	public String getModuleName() {
		return "magic";
	}

	private boolean processWebSocketSendMessage(String clientId, String content) {
		WebSocketSessionManager.sendByClientId(clientId, content);
		return true;
	}

	private boolean processWebSocketMessageReceived(String clientId, String content) {
		MagicWebSocketDispatcher.processMessageReceived(clientId, content);
		return true;
	}
}
