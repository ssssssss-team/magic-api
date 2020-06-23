package org.ssssssss.script;


import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.Parser;
import org.ssssssss.script.parsing.ast.Node;

import java.util.List;

public class MagicScript {
    private final List<Node> nodes;

    private MagicScript(List<Node> nodes) {
        this.nodes = nodes;
    }

    public static MagicScript create(String source) {
        return new MagicScript(Parser.parse(source));
    }

    public List<Node> getNodes() {
        return nodes;
    }

    /**
     * Renders the magicScript using the MagicScriptContext to resolve variable values referenced in the magicScript.
     **/
    Object execute(MagicScriptContext context) {
        return AstInterpreter.interpret(this, context);
    }
}
