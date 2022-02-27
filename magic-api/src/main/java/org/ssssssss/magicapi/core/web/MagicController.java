package org.ssssssss.magicapi.core.web;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.core.config.Constants;
import org.ssssssss.magicapi.core.config.JsonCodeConstants;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.annotation.Valid;
import org.ssssssss.magicapi.core.model.*;
import org.ssssssss.magicapi.core.exception.InvalidArgumentException;
import org.ssssssss.magicapi.core.exception.MagicLoginException;
import org.ssssssss.magicapi.core.interceptor.Authorization;
import org.ssssssss.magicapi.core.context.MagicUser;
import org.ssssssss.magicapi.core.service.MagicAPIService;
import org.ssssssss.magicapi.backup.service.MagicBackupService;
import org.ssssssss.magicapi.core.service.MagicResourceService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Controller 基类
 *
 * @author mxd
 */
public class MagicController implements JsonCodeConstants {

	final MagicAPIService magicAPIService;
	final MagicBackupService magicBackupService;
	protected MagicConfiguration configuration;

	public MagicController(MagicConfiguration configuration) {
		this.configuration = configuration;
		this.magicAPIService = configuration.getMagicAPIService();
		this.magicBackupService = configuration.getMagicBackupService();
	}

	public void doValid(HttpServletRequest request, Valid valid) {
		if (valid != null) {
			if (!valid.readonly() && configuration.getWorkspace().readonly()) {
				throw new InvalidArgumentException(IS_READ_ONLY);
			}
			if (valid.authorization() != Authorization.NONE && !allowVisit(request, valid.authorization())) {
				throw new InvalidArgumentException(PERMISSION_INVALID);
			}
		}
	}

	/**
	 * 判断是否有权限访问按钮
	 */
	boolean allowVisit(HttpServletRequest request, Authorization authorization) {
		if (authorization == null) {
			return true;
		}
		MagicUser magicUser = (MagicUser) request.getAttribute(Constants.ATTRIBUTE_MAGIC_USER);
		return configuration.getAuthorizationInterceptor().allowVisit(magicUser, request, authorization);
	}

	boolean allowVisit(HttpServletRequest request, Authorization authorization, MagicEntity entity) {
		if (authorization == null) {
			return true;
		}
		MagicUser magicUser = (MagicUser) request.getAttribute(Constants.ATTRIBUTE_MAGIC_USER);
		return configuration.getAuthorizationInterceptor().allowVisit(magicUser, request, authorization, entity);
	}

	boolean allowVisit(HttpServletRequest request, Authorization authorization, Group group) {
		if (authorization == null) {
			return true;
		}
		MagicUser magicUser = (MagicUser) request.getAttribute(Constants.ATTRIBUTE_MAGIC_USER);
		return configuration.getAuthorizationInterceptor().allowVisit(magicUser, request, authorization, group);
	}

	List<MagicEntity> entities(HttpServletRequest request, Authorization authorization) {
		MagicResourceService service = configuration.getMagicResourceService();
		return service.tree()
				.values()
				.stream()
				.flatMap(it -> it.flat().stream())
				.filter(it -> !Constants.ROOT_ID.equals(it.getId()))
				.filter(it -> allowVisit(request, authorization, it))
				.flatMap(it -> service.listFiles(it.getId()).stream())
				.filter(it -> allowVisit(request, authorization, it))
				.filter(it -> Objects.nonNull(it.getScript()))
				.collect(Collectors.toList());
	}

	@ExceptionHandler(MagicLoginException.class)
	@ResponseBody
	public JsonBean<Void> invalidLogin(MagicLoginException exception) {
		return new JsonBean<>(401, exception.getMessage());
	}
}
