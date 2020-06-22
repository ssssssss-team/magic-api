package org.ssssssss.script.interpreter;

import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.MagicScriptError.ScriptException;
import org.ssssssss.script.parsing.ast.Break;
import org.ssssssss.script.parsing.ast.Continue;
import org.ssssssss.script.parsing.ast.Node;
import org.ssssssss.script.parsing.ast.Return;

import java.util.List;

/**
 * <p>
 * Interprets a Template given a MagicScriptContext to lookup variable values in and writes the evaluation results to an output
 * stream. Uses the global {@link AbstractReflection} instance as returned by {@link AbstractReflection#getInstance()} to access members and call
 * methods.
 * </p>
 *
 * <p>
 * The interpeter traverses the AST as stored in {@link MagicScript#getNodes()}. the interpeter has a method for each AST node type
 * be written to the output stream.
 * </p>
 **/
public class AstInterpreter {
    public static Object interpret(MagicScript magicScript, MagicScriptContext context) {
        try {
            Object value = interpretNodeList(magicScript.getNodes(), magicScript, context);
            if (value == Return.RETURN_SENTINEL) {
                return ((Return.ReturnValue) value).getValue();
            }
            return null;
        } catch (Throwable t) {
            if (t instanceof ScriptException) {
                throw (ScriptException) t;
            } else {
                MagicScriptError.error("执行表达式出错 " + t.getMessage(), magicScript.getNodes().get(0).getSpan(), t);
                return null; // never reached
            }
        } finally {
            Return.RETURN_SENTINEL.setValue(null);
        }
    }

    public static Object interpretNodeList(List<Node> nodes, MagicScript magicScript, MagicScriptContext context) {
        if (nodes != null) {
            for (int i = 0, n = nodes.size(); i < n; i++) {
                Node node = nodes.get(i);
                Object value = node.evaluate(magicScript, context);
                if (value == Break.BREAK_SENTINEL || value == Continue.CONTINUE_SENTINEL || value == Return.RETURN_SENTINEL) {
                    return value;
                }
            }
        }
        return null;
    }
}
