package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicModuleLoader;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.exception.ModuleNotFoundException;
import org.ssssssss.script.parsing.Span;

public class Import extends Node {

	private String packageName;

	private String variableName;

	private boolean module;

	public Import(Span span, String packageName, String variableName, boolean module) {
		super(span);
		this.packageName = packageName;
		this.variableName = variableName;
		this.module = module;
	}

	@Override
	public Object evaluate(MagicScriptContext context) {
        if (this.module) {
            Object target = MagicModuleLoader.loadModule(packageName);
            if (target == null) {
                throw new ModuleNotFoundException(String.format("module [%s] not found.", this.packageName), getSpan().getLine());
            }
            context.set(variableName, target);
        }else{
            Object target = MagicModuleLoader.loadClass(packageName);
            if (target == null) {
                throw new ModuleNotFoundException(String.format("class [%s] not found.", this.packageName), getSpan().getLine());
            }
            context.set(variableName, target);
        }
		return null;
	}
}
