package org.ssssssss.magicapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.exception.InvalidArgumentException;
import org.ssssssss.magicapi.model.JsonBean;

public interface MagicExceptionHandler {

	Logger logger = LoggerFactory.getLogger(MagicExceptionHandler.class);

	@ExceptionHandler(Exception.class)
	@ResponseBody
	default Object exceptionHandler(Exception e) {
		logger.error("magic-api调用接口出错", e);
		return new JsonBean<>(-1, e.getMessage());
	}

	@ExceptionHandler(InvalidArgumentException.class)
	@ResponseBody
	default Object exceptionHandler(InvalidArgumentException e) {
		return new JsonBean<>(e.getCode(), e.getMessage());
	}
}
