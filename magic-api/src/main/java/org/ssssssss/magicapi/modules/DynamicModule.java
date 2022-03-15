package org.ssssssss.magicapi.modules;

import org.ssssssss.script.MagicScriptContext;

import java.beans.Transient;

public interface DynamicModule<T> {

	@Transient
	T getDynamicModule(MagicScriptContext context);
}
