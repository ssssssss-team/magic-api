package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.parsing.Span;

public class Return extends Node {

    public static final ReturnValue RETURN_SENTINEL = new ReturnValue();
    private final Node returnValue;

    public Return(Span span, Node returnValue) {
        super(span);
        this.returnValue = returnValue;
    }

    @Override
    public Object evaluate(MagicScript magicScript, MagicScriptContext context) {
        RETURN_SENTINEL.setValue(returnValue != null ? returnValue.evaluate(magicScript, context) : null);
        return RETURN_SENTINEL;
    }

    /**
     * A sentital of which only one instance exists. Uses thread local storage to store an (optional) return value. See
     **/
    public static class ReturnValue {
        private final ThreadLocal<Object> value = new ThreadLocal<Object>();

        public Object getValue() {
            return value.get();
        }

        public void setValue(Object value) {
            this.value.set(value);
        }
    }
}