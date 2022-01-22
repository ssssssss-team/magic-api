package org.ssssssss.magicapi.core.config;

import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.web.MagicControllerRegister;

public interface MagicPluginConfiguration {

	Plugin plugin();


	/**
	 * 注册Controller
	 */
	default MagicControllerRegister controllerRegister(){
		return (mapping, configuration) -> { };
	}
}
