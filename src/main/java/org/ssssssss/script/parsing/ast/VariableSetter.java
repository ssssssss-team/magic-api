package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;

public interface VariableSetter {
    public void setValue(MagicScript magicScript, MagicScriptContext context, Object value);
}