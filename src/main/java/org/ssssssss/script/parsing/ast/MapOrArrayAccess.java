package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.parsing.Span;

import java.util.List;
import java.util.Map;

public class MapOrArrayAccess extends Expression implements VariableSetter {
    private final Expression mapOrArray;
    private final Expression keyOrIndex;

    public MapOrArrayAccess(Span span, Expression mapOrArray, Expression keyOrIndex) {
        super(span);
        this.mapOrArray = mapOrArray;
        this.keyOrIndex = keyOrIndex;
    }

    /**
     * Returns an expression that must evaluate to a map or array.
     **/
    public Expression getMapOrArray() {
        return mapOrArray;
    }

    /**
     * Returns an expression that is used as the key or index to fetch a map or array element.
     **/
    public Expression getKeyOrIndex() {
        return keyOrIndex;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object evaluate(MagicScript magicScript, MagicScriptContext context) {
        Object mapOrArray = getMapOrArray().evaluate(magicScript, context);
        if (mapOrArray == null) {
            return null;
        }
        Object keyOrIndex = getKeyOrIndex().evaluate(magicScript, context);
        if (keyOrIndex == null) {
            return null;
        }

        if (mapOrArray instanceof Map) {
            return ((Map) mapOrArray).get(keyOrIndex);
        } else if (mapOrArray instanceof List) {
            if (!(keyOrIndex instanceof Number)) {
                MagicScriptError.error("List index must be an integer, but was " + keyOrIndex.getClass().getSimpleName(), getKeyOrIndex().getSpan());
            }
            int index = ((Number) keyOrIndex).intValue();
            return ((List) mapOrArray).get(index);
        } else {
            if (!(keyOrIndex instanceof Number)) {
                MagicScriptError.error("Array index must be an integer, but was " + keyOrIndex.getClass().getSimpleName(), getKeyOrIndex().getSpan());
            }
            int index = ((Number) keyOrIndex).intValue();
            if (mapOrArray instanceof int[]) {
                return ((int[]) mapOrArray)[index];
            } else if (mapOrArray instanceof float[]) {
                return ((float[]) mapOrArray)[index];
            } else if (mapOrArray instanceof double[]) {
                return ((double[]) mapOrArray)[index];
            } else if (mapOrArray instanceof boolean[]) {
                return ((boolean[]) mapOrArray)[index];
            } else if (mapOrArray instanceof char[]) {
                return ((char[]) mapOrArray)[index];
            } else if (mapOrArray instanceof short[]) {
                return ((short[]) mapOrArray)[index];
            } else if (mapOrArray instanceof long[]) {
                return ((long[]) mapOrArray)[index];
            } else if (mapOrArray instanceof byte[]) {
                return ((byte[]) mapOrArray)[index];
            } else if (mapOrArray instanceof String) {
                return Character.toString(((String) mapOrArray).charAt(index));
            } else {
                return ((Object[]) mapOrArray)[index];
            }
        }
    }

    @Override
    public void setValue(MagicScript magicScript, MagicScriptContext context, Object value) {
        Object mapOrArray = getMapOrArray().evaluate(magicScript, context);
        if (mapOrArray != null) {
            Object keyOrIndex = getKeyOrIndex().evaluate(magicScript, context);
            if (keyOrIndex != null) {
                if (mapOrArray instanceof Map) {
                    ((Map) mapOrArray).put(keyOrIndex, value);
                } else if (mapOrArray instanceof List) {
                    if (!(keyOrIndex instanceof Number)) {
                        MagicScriptError.error("List index must be an integer, but was " + keyOrIndex.getClass().getSimpleName(), getKeyOrIndex().getSpan());
                    }
                    int index = ((Number) keyOrIndex).intValue();
                    ((List) mapOrArray).set(index, value);
                }
            }
        }
    }
}