package org.ssssssss.script.parsing;


import org.ssssssss.script.MagicScript;
import org.ssssssss.script.MagicScriptContext;
import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.interpreter.AstInterpreter;
import org.ssssssss.script.parsing.ast.*;
import org.ssssssss.script.parsing.ast.binary.BinaryOperation;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


/**
 * Parses a {@link Source} into a {@link MagicScript}. The implementation is a simple recursive descent parser with a lookahead of
 * 1.
 **/
public class Parser {

    private static final TokenType[][] binaryOperatorPrecedence = new TokenType[][]{new TokenType[]{TokenType.Assignment},
            new TokenType[]{TokenType.Or, TokenType.And, TokenType.Xor}, new TokenType[]{TokenType.Equal, TokenType.NotEqual},
            new TokenType[]{TokenType.Less, TokenType.LessEqual, TokenType.Greater, TokenType.GreaterEqual}, new TokenType[]{TokenType.Plus, TokenType.Minus},
            new TokenType[]{TokenType.ForwardSlash, TokenType.Asterisk, TokenType.Percentage}};
    private static final TokenType[] unaryOperators = new TokenType[]{TokenType.Not, TokenType.Plus, TokenType.Minus};

    /**
     * Parses a {@link Source} into a {@link MagicScript}.
     **/
    public static List<Node> parse(String source) {
        List<Node> nodes = new ArrayList<Node>();
        TokenStream stream = new TokenStream(new Tokenizer().tokenize(source));
        while (stream.hasMore()) {
            Node node = parseStatement(stream);
            if (node != null) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    private static Node parseStatement(TokenStream tokens) {
        return parseStatement(tokens, false);
    }

    private static Node parseStatement(TokenStream tokens, boolean expectRightCurly) {
        Node result = null;
        if (tokens.match("import", false)) {
            result = parseImport(tokens);
        } else if (tokens.match("var", false)) {
            result = parseVarDefine(tokens);
        } else if (tokens.match("if", false)) {
            result = parseIfStatement(tokens);
        } else if (tokens.match("return", false)) {
            result = parseReturn(tokens);
        } else if (tokens.match("for", false)) {
            result = parseForStatement(tokens);
        } else if (tokens.match("continue", false)) {
            result = new Continue(tokens.consume().getSpan());
        } else if (tokens.match("break", false)) {
            result = new Break(tokens.consume().getSpan());
        } else {
            result = parseExpression(tokens, expectRightCurly);
        }
        // consume semi-colons as statement delimiters
        while (tokens.match(";", true)) {
            ;
        }
        return result;
    }

    private static Import parseImport(TokenStream stream) {
        Span opening = stream.expect("import").getSpan();
        if (stream.hasMore()) {
            Token expected = stream.consume();
            String packageName = null;
            boolean isStringLiteral = expected.getType() == TokenType.StringLiteral;
            if (isStringLiteral) {
                packageName = new StringLiteral(expected.getSpan()).getValue();
            } else if (expected.getType() == TokenType.Identifier) {
                packageName = expected.getSpan().getText();
            } else {
                MagicScriptError.error("Expected identifier or string, but got stream is " + expected.getType().getError(), stream.getPrev().getSpan());
            }
            String varName = packageName;
            if (isStringLiteral || stream.match("as",false)) {
                stream.expect("as");
                expected = stream.expect(TokenType.Identifier);
                varName = expected.getSpan().getText();
            }
            return new Import(new Span(opening, expected.getSpan()), packageName, varName, !isStringLiteral);
        }
        MagicScriptError.error("Expected identifier or string, but got stream is EOF", stream.getPrev().getSpan());
        return null;
    }

    private static VariableDefine parseVarDefine(TokenStream stream) {
        Span opening = stream.expect("var").getSpan();
        TokenType expected = null;
        if (stream.hasMore()) {
            expected = TokenType.Identifier;
            Token token = stream.expect(expected);
            String variableName = token.getSpan().getText();
            expected = TokenType.Assignment;
            if (stream.hasMore()) {
                stream.expect(expected);
                Expression right = parseExpression(stream);
                return new VariableDefine(new Span(opening, stream.getPrev().getSpan()), variableName, right);
            }
        }
        MagicScriptError.error("Expected " + expected.getError() + ", but got stream is EOF", stream.getPrev().getSpan());
        return null;
    }

    private static FunctionStatement parseFunctionStatement(TokenStream stream) {
        Span openingFunction = stream.expect("function").getSpan();
        stream.expect("(");
        List<String> parameters = new ArrayList<>();
        while (stream.hasMore() && stream.match(TokenType.Identifier, false)) {
            Token token = stream.consume();
            parameters.add(token.getSpan().getText());
            if (!stream.hasMore()) {
                MagicScriptError.error("function Did not closing ", stream.prev().getSpan());
            }
            if (!stream.match(TokenType.Comma, true)) {
                if (!stream.match(TokenType.RightParantheses, false)) {
                    MagicScriptError.error("Did not find closing ).", stream.prev().getSpan());
                } else {
                    break;
                }
            }
        }
        stream.expect(")");
        stream.expect("{");
        List<Node> childNodes = new ArrayList<>();
        while (stream.hasMore() && !stream.match(false, "}")) {
            childNodes.add(parseStatement(stream, true));
        }
        Span closingEnd = expectCloseing(stream);
        return new FunctionStatement(new Span(openingFunction, closingEnd), childNodes, parameters);
    }

    private static ForStatement parseForStatement(TokenStream stream) {
        Span openingFor = stream.expect("for").getSpan();
        stream.expect("(");
        Span index = null;
        Span value = stream.expect(TokenType.Identifier).getSpan();

        if (stream.match(TokenType.Comma, true)) {
            index = value;
            value = stream.expect(TokenType.Identifier).getSpan();
        }

        stream.expect("in");

        Expression mapOrArray = parseExpression(stream);
        stream.expect(")");
        stream.expect("{");
        List<Node> body = new ArrayList<Node>();
        while (stream.hasMore() && !stream.match(false, "}")) {
            body.add(parseStatement(stream, true));
        }
        Span closingEnd = expectCloseing(stream);
        return new ForStatement(new Span(openingFor, closingEnd), index, value, mapOrArray, body);
    }

    private static Span expectCloseing(TokenStream stream) {
        if (!stream.hasMore()) {
            MagicScriptError.error("Did not find closing }.", stream.prev().getSpan());
        }
        return stream.expect("}").getSpan();
    }

    private static Node parseIfStatement(TokenStream stream) {
        Span openingIf = stream.expect("if").getSpan();
        Expression condition = parseExpression(stream);
        stream.expect("{");
        List<Node> trueBlock = new ArrayList<Node>();
        while (stream.hasMore() && !stream.match("}", false)) {
            Node node = parseStatement(stream, true);
            if (node != null) {
                trueBlock.add(node);
            }
        }
        expectCloseing(stream);
        List<IfStatement> elseIfs = new ArrayList<IfStatement>();
        List<Node> falseBlock = new ArrayList<Node>();
        while (stream.hasMore() && stream.match("else", true)) {
            if (stream.hasMore() && stream.match("if", false)) {
                Span elseIfOpening = stream.expect("if").getSpan();
                Expression elseIfCondition = parseExpression(stream);
                stream.expect("{");
                List<Node> elseIfBlock = new ArrayList<Node>();
                while (stream.hasMore() && !stream.match("}", false)) {
                    Node node = parseStatement(stream, true);
                    if (node != null) {
                        elseIfBlock.add(node);
                    }
                }
                expectCloseing(stream);
                Span elseIfSpan = new Span(elseIfOpening, elseIfBlock.size() > 0 ? elseIfBlock.get(elseIfBlock.size() - 1).getSpan() : elseIfOpening);
                elseIfs.add(new IfStatement(elseIfSpan, elseIfCondition, elseIfBlock, new ArrayList<IfStatement>(), new ArrayList<Node>()));
            } else {
                stream.expect("{");
                while (stream.hasMore() && !stream.match("}", false)) {
                    falseBlock.add(parseStatement(stream, true));
                }
                expectCloseing(stream);
                break;
            }
        }
        Span closingEnd = stream.getPrev().getSpan();

        return new IfStatement(new Span(openingIf, closingEnd), condition, trueBlock, elseIfs, falseBlock);
    }

    private static Node parseReturn(TokenStream tokens) {
        Span returnSpan = tokens.expect("return").getSpan();
        if (tokens.match(";", false)) return new Return(returnSpan, null);
        Expression returnValue = parseExpression(tokens);
        return new Return(new Span(returnSpan, returnValue.getSpan()), returnValue);
    }

    public static Expression parseExpression(TokenStream stream) {
        return parseTernaryOperator(stream);
    }

    public static Expression parseExpression(TokenStream stream, boolean expectRightCurly) {
        return parseTernaryOperator(stream, expectRightCurly);
    }

    private static Expression parseTernaryOperator(TokenStream stream, boolean expectRightCurly) {
        Expression condition = parseBinaryOperator(stream, 0, expectRightCurly);
        if (stream.match(TokenType.Questionmark, true)) {
            Expression trueExpression = parseTernaryOperator(stream, expectRightCurly);
            stream.expect(TokenType.Colon);
            Expression falseExpression = parseTernaryOperator(stream, expectRightCurly);
            return new TernaryOperation(condition, trueExpression, falseExpression);
        } else {
            return condition;
        }
    }

    private static Expression parseTernaryOperator(TokenStream stream) {
        return parseTernaryOperator(stream, false);
    }

    private static Expression parseBinaryOperator(TokenStream stream, int level, boolean expectRightCurly) {
        int nextLevel = level + 1;
        Expression left = nextLevel == binaryOperatorPrecedence.length ? parseUnaryOperator(stream, expectRightCurly) : parseBinaryOperator(stream, nextLevel, expectRightCurly);

        TokenType[] operators = binaryOperatorPrecedence[level];
        while (stream.hasMore() && stream.match(false, operators)) {
            Token operator = stream.consume();
            Expression right = nextLevel == binaryOperatorPrecedence.length ? parseUnaryOperator(stream, expectRightCurly) : parseBinaryOperator(stream, nextLevel, expectRightCurly);
            left = BinaryOperation.create(left, operator, right);
        }

        return left;
    }


    private static Expression parseUnaryOperator(TokenStream stream, boolean expectRightCurly) {
        if (stream.match(false, unaryOperators)) {
            return new UnaryOperation(stream.consume(), parseUnaryOperator(stream, expectRightCurly));
        } else {
            if (stream.match(TokenType.LeftParantheses, false)) {    //(
                Span openSpan = stream.expect(TokenType.LeftParantheses).getSpan();
                int index = stream.makeIndex();
                List<String> parameters = new ArrayList<>();
                while(stream.match(TokenType.Identifier,false)){
                    Token identifier = stream.expect(TokenType.Identifier);
                    parameters.add(identifier.getSpan().getText());
                    if(stream.match(TokenType.Comma,true)){ //,
                        continue;
                    }
                    if(stream.match(TokenType.RightParantheses,true)){  //)
                        if(stream.match(TokenType.Lambda,true)){   // =>
                            return parseLambdaBody(stream, openSpan, parameters);
                        }
                        break;
                    }
                }
                if(stream.match(TokenType.RightParantheses,true) && stream.match(TokenType.Lambda, true)){
                    return parseLambdaBody(stream, openSpan, parameters);
                }
                stream.resetIndex(index);
                Expression expression = parseExpression(stream);
                stream.expect(TokenType.RightParantheses);
                return expression;
            } else {
                return parseAccessOrCallOrLiteral(stream,expectRightCurly);
            }
        }
    }

    private static Expression parseLambdaBody(TokenStream stream, Span openSpan, List<String> parameters) {
        int index = stream.makeIndex();
        List<Node> childNodes = new ArrayList<>();
        try {
            Expression expression = parseExpression(stream);
            childNodes.add(new Return(new Span("return", 0, 6), expression));
            return new LambdaFunction(new Span(openSpan, expression.getSpan()), parameters, childNodes);
        } catch (Exception e) {
            stream.resetIndex(index);
            if (stream.match(TokenType.LeftCurly, true)) {
                while (stream.hasMore() && !stream.match(false, "}")) {
                    childNodes.add(parseStatement(stream, true));
                }
                Span closeSpan = expectCloseing(stream);
                return new LambdaFunction(new Span(openSpan, closeSpan), parameters, childNodes);
            } else {
                Node node = parseStatement(stream);
                childNodes.add(new Return(new Span("return", 0, 6), node));
                return new LambdaFunction(new Span(openSpan, node.getSpan()), parameters, childNodes);
            }
        }
    }

    private static Expression parseAccessOrCallOrLiteral(TokenStream stream, boolean expectRightCurly) {
        if (expectRightCurly && stream.match("}", false)) {
            return null;
        } else if (stream.match("function", false)) {
            return parseFunctionStatement(stream);
        } else if (stream.match(TokenType.Identifier, false)) {
            return parseAccessOrCall(stream, TokenType.Identifier);
        } else if (stream.match(TokenType.LeftCurly, false)) {
            return parseMapLiteral(stream);
        } else if (stream.match(TokenType.LeftBracket, false)) {
            return parseListLiteral(stream);
        } else if (stream.match(TokenType.StringLiteral, false)) {
            if (stream.hasNext()) {
                if (stream.next().getType() == TokenType.Period) {
                    stream.prev();
                    return parseAccessOrCall(stream, TokenType.StringLiteral);
                }
                stream.prev();
            }

            return new StringLiteral(stream.expect(TokenType.StringLiteral).getSpan());
        } else if (stream.match(TokenType.BooleanLiteral, false)) {
            return new BooleanLiteral(stream.expect(TokenType.BooleanLiteral).getSpan());
        } else if (stream.match(TokenType.DoubleLiteral, false)) {
            return new DoubleLiteral(stream.expect(TokenType.DoubleLiteral).getSpan());
        } else if (stream.match(TokenType.FloatLiteral, false)) {
            return new FloatLiteral(stream.expect(TokenType.FloatLiteral).getSpan());
        } else if (stream.match(TokenType.ByteLiteral, false)) {
            return new ByteLiteral(stream.expect(TokenType.ByteLiteral).getSpan());
        } else if (stream.match(TokenType.ShortLiteral, false)) {
            return new ShortLiteral(stream.expect(TokenType.ShortLiteral).getSpan());
        } else if (stream.match(TokenType.IntegerLiteral, false)) {
            return new IntegerLiteral(stream.expect(TokenType.IntegerLiteral).getSpan());
        } else if (stream.match(TokenType.LongLiteral, false)) {
            return new LongLiteral(stream.expect(TokenType.LongLiteral).getSpan());
        } else if (stream.match(TokenType.CharacterLiteral, false)) {
            return new CharacterLiteral(stream.expect(TokenType.CharacterLiteral).getSpan());
        } else if (stream.match(TokenType.NullLiteral, false)) {
            return new NullLiteral(stream.expect(TokenType.NullLiteral).getSpan());
        } else {
            MagicScriptError.error("Expected a variable, field, map, array, function or method call, or literal.", stream);
            return null; // not reached
        }
    }


    private static Expression parseMapLiteral(TokenStream stream) {
        Span openCurly = stream.expect(TokenType.LeftCurly).getSpan();

        List<Token> keys = new ArrayList<>();
        List<Expression> values = new ArrayList<>();
        while (stream.hasMore() && !stream.match("}", false)) {
            if (stream.match(TokenType.StringLiteral, false)) {
                keys.add(stream.expect(TokenType.StringLiteral));
            } else {
                keys.add(stream.expect(TokenType.Identifier));
            }

            stream.expect(":");
            values.add(parseExpression(stream));
            if (!stream.match("}", false)) {
                stream.expect(TokenType.Comma);
            }
        }
        Span closeCurly = stream.expect("}").getSpan();
        return new MapLiteral(new Span(openCurly, closeCurly), keys, values);
    }

    private static Expression parseListLiteral(TokenStream stream) {
        Span openBracket = stream.expect(TokenType.LeftBracket).getSpan();

        List<Expression> values = new ArrayList<>();
        while (stream.hasMore() && !stream.match(TokenType.RightBracket, false)) {
            values.add(parseExpression(stream));
            if (!stream.match(TokenType.RightBracket, false)) {
                stream.expect(TokenType.Comma);
            }
        }

        Span closeBracket = stream.expect(TokenType.RightBracket).getSpan();
        return new ListLiteral(new Span(openBracket, closeBracket), values);
    }

    private static Expression parseAccessOrCall(TokenStream stream, TokenType tokenType) {
        //Span identifier = stream.expect(TokenType.Identifier);
        //Expression result = new VariableAccess(identifier);
        Span identifier = stream.expect(tokenType).getSpan();
        if (tokenType == TokenType.Identifier && stream.match(TokenType.Lambda, true)) {
            return parseLambdaBody(stream, identifier, Arrays.asList(identifier.getText()));
        }
        Expression result = tokenType == TokenType.StringLiteral ? new StringLiteral(identifier) : new VariableAccess(identifier);

        while (stream.hasMore() && stream.match(false, TokenType.LeftParantheses, TokenType.LeftBracket, TokenType.Period)) {

            // function or method call
            if (stream.match(TokenType.LeftParantheses, false)) {
                List<Expression> arguments = parseArguments(stream);
                Span closingSpan = stream.expect(TokenType.RightParantheses).getSpan();
                if (result instanceof VariableAccess || result instanceof MapOrArrayAccess)
                    result = new FunctionCall(new Span(result.getSpan(), closingSpan), result, arguments);
                else if (result instanceof MemberAccess) {
                    result = new MethodCall(new Span(result.getSpan(), closingSpan), (MemberAccess)result, arguments);
                } else {
                    MagicScriptError.error("Expected a variable, field or method.", stream);
                }
            }

            // map or array access
            else if (stream.match(TokenType.LeftBracket, true)) {
                Expression keyOrIndex = parseExpression(stream);
                Span closingSpan = stream.expect(TokenType.RightBracket).getSpan();
                result = new MapOrArrayAccess(new Span(result.getSpan(), closingSpan), result, keyOrIndex);
            }

            // field or method access
            else if (stream.match(TokenType.Period, true)) {
                identifier = stream.expect(TokenType.Identifier).getSpan();
                result = new MemberAccess(result, identifier);
            }
        }

        return result;
    }

    /**
     * Does not consume the closing parentheses.
     **/
    private static List<Expression> parseArguments(TokenStream stream) {
        stream.expect(TokenType.LeftParantheses);
        List<Expression> arguments = new ArrayList<Expression>();
        while (stream.hasMore() && !stream.match(TokenType.RightParantheses, false)) {
            arguments.add(parseExpression(stream));
            if (!stream.match(TokenType.RightParantheses, false)) stream.expect(TokenType.Comma);
        }
        return arguments;
    }

    public static void main(String[] args) {
        System.out.println(AstInterpreter.interpret(MagicScript.create("return message!=null&&message.length()>=3"), new MagicScriptContext(new HashMap<>())));
        ;
    }
}
