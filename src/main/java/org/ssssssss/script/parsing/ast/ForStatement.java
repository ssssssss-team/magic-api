package org.ssssssss.script.parsing.ast;

import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Span;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ForStatement extends Node {
    private final Span indexOrKeyName;
    private final Span valueName;
    private final Expression mapOrArray;
    private final List<Node> body;

    public ForStatement(Span span, Span indexOrKeyName, Span valueName, Expression mapOrArray, List<Node> body) {
        super(span);
        this.indexOrKeyName = indexOrKeyName;
        this.valueName = valueName;
        this.mapOrArray = mapOrArray;
        this.body = body;
    }

    /**
     * Returns null if no index or key name was given
     **/
    public Span getIndexOrKeyName() {
        return indexOrKeyName;
    }

    public Span getValueName() {
        return valueName;
    }

    public Expression getMapOrArray() {
        return mapOrArray;
    }

    public List<Node> getBody() {
        return body;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object evaluate(MagicScriptContext context) {
        Object mapOrArray = getMapOrArray().evaluate(context);
        if (mapOrArray == null) MagicScriptError.error("Expected a map or array, got null.", getMapOrArray().getSpan());
        String valueName = getValueName().getText();

        if (mapOrArray instanceof Map) {
            Map map = (Map) mapOrArray;
            if (getIndexOrKeyName() != null) {
                context.push();
                String keyName = getIndexOrKeyName().getText();
                for (Object entry : map.entrySet()) {
                    Map.Entry e = (Map.Entry) entry;
                    context.setOnCurrentScope(keyName, e.getKey());
                    context.setOnCurrentScope(valueName, e.getValue());
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
                    if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
                        break;
                    }
                    if (breakOrContinueOrReturn == Return.RETURN_SENTINEL) {
                        context.pop();
                        return breakOrContinueOrReturn;
                    }
                }
                context.pop();
            } else {
                context.push();
                for (Object value : map.values()) {
                    context.setOnCurrentScope(valueName, value);
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
                    if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
                        break;
                    }
                    if (breakOrContinueOrReturn == Return.RETURN_SENTINEL) {
                        context.pop();
                        return breakOrContinueOrReturn;
                    }
                }
                context.pop();
            }
        } else if (mapOrArray instanceof Iterable) {
            if (getIndexOrKeyName() != null) {
                context.push();
                String keyName = getIndexOrKeyName().getText();
                Iterator iter = ((Iterable) mapOrArray).iterator();
                int i = 0;
                while (iter.hasNext()) {
                    context.setOnCurrentScope(keyName, i++);
                    context.setOnCurrentScope(valueName, iter.next());
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
                    if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
                        break;
                    }
                    if (breakOrContinueOrReturn == Return.RETURN_SENTINEL) {
                        context.pop();
                        return breakOrContinueOrReturn;
                    }
                }
                context.pop();
            } else {
                Iterator iter = ((Iterable) mapOrArray).iterator();
                context.push();
                while (iter.hasNext()) {
                    context.setOnCurrentScope(valueName, iter.next());
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
                    if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
                        break;
                    }
                    if (breakOrContinueOrReturn == Return.RETURN_SENTINEL) {
                        context.pop();
                        return breakOrContinueOrReturn;
                    }
                }
                context.pop();
            }
        } else if (mapOrArray instanceof Iterator) {
            if (getIndexOrKeyName() != null) {
                MagicScriptError.error("Can not do indexed/keyed for loop on an iterator.", getMapOrArray().getSpan());
            } else {
                Iterator iter = (Iterator) mapOrArray;
                context.push();
                while (iter.hasNext()) {
                    context.setOnCurrentScope(valueName, iter.next());
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
                    if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
                        break;
                    }
                    if (breakOrContinueOrReturn == Return.RETURN_SENTINEL) {
                        context.pop();
                        return breakOrContinueOrReturn;
                    }
                }
                context.pop();
            }
        } else if (mapOrArray != null && mapOrArray.getClass().isArray()) {
            int len = Array.getLength(mapOrArray);
            if (getIndexOrKeyName() != null) {
                context.push();
                String keyName = getIndexOrKeyName().getText();
                for (int i = 0; i < len; i++) {
                    context.setOnCurrentScope(keyName, i);
                    context.setOnCurrentScope(valueName, Array.get(mapOrArray, i));
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
                    if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
                        break;
                    }
                    if (breakOrContinueOrReturn == Return.RETURN_SENTINEL) {
                        context.pop();
                        return breakOrContinueOrReturn;
                    }
                }
                context.pop();
            } else {
                context.push();
                for (int i = 0; i < len; i++) {
                    context.setOnCurrentScope(valueName, Array.get(mapOrArray, i));
                    Object breakOrContinueOrReturn = AstInterpreter.interpretNodeList(getBody(), context);
                    if (breakOrContinueOrReturn == Break.BREAK_SENTINEL) {
                        break;
                    }
                    if (breakOrContinueOrReturn == Return.RETURN_SENTINEL) {
                        context.pop();
                        return breakOrContinueOrReturn;
                    }
                }
                context.pop();
            }
        } else {
            MagicScriptError.error("Expected a map, an array or an iterable, got " + mapOrArray, getMapOrArray().getSpan());
        }
        return null;
    }
}