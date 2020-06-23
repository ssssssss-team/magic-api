package org.ssssssss.script.parsing;

import org.ssssssss.script.MagicScriptError;
import org.ssssssss.script.MagicScriptError.StringLiteralException;

import java.util.ArrayList;
import java.util.List;


public class Tokenizer {

    public List<Token> tokenize(String source) {
        CharacterStream stream = new CharacterStream(source, 0, source.length());
        List<Token> tokens = new ArrayList<Token>();
        int leftCount = 0;
        int rightCount = 0;
        outer:
        while (stream.hasMore()) {
            stream.skipWhiteSpace();
            if (stream.match("//", true)) {    //注释
                stream.skipLine();
                continue;
            }
            if (stream.match("/*", true)) {    //多行注释
                stream.skipUntil("*/");
                continue;
            }
            if (stream.matchDigit(false)) {
                TokenType type = TokenType.IntegerLiteral;
                stream.startSpan();
                while (stream.matchDigit(true)) {
                    ;
                }
                if (stream.match(TokenType.Period.getLiteral(), true)) {
                    type = TokenType.FloatLiteral;
                    while (stream.matchDigit(true)) {
                        ;
                    }
                }
                if (stream.match("b", true) || stream.match("B", true)) {
                    if (type == TokenType.FloatLiteral) {
                        MagicScriptError.error("Byte literal can not have a decimal point.", stream.endSpan());
                    }
                    type = TokenType.ByteLiteral;
                } else if (stream.match("s", true) || stream.match("S", true)) {
                    if (type == TokenType.FloatLiteral) {
                        MagicScriptError.error("Short literal can not have a decimal point.", stream.endSpan());
                    }
                    type = TokenType.ShortLiteral;
                } else if (stream.match("l", true) || stream.match("L", true)) {
                    if (type == TokenType.FloatLiteral) {
                        MagicScriptError.error("Long literal can not have a decimal point.", stream.endSpan());
                    }
                    type = TokenType.LongLiteral;
                } else if (stream.match("f", true) || stream.match("F", true)) {
                    type = TokenType.FloatLiteral;
                } else if (stream.match("d", true) || stream.match("D", true)) {
                    type = TokenType.DoubleLiteral;
                }
                Span numberSpan = stream.endSpan();
                tokens.add(new Token(type, numberSpan));
                continue;
            }

            // String literal
            if (stream.match(TokenType.SingleQuote.getLiteral(), true)) {
                stream.startSpan();
                boolean matchedEndQuote = false;
                while (stream.hasMore()) {
                    // Note: escape sequences like \n are parsed in StringLiteral
                    if (stream.match("\\", true)) {
                        stream.consume();
                    }
                    if (stream.match(TokenType.SingleQuote.getLiteral(), true)) {
                        matchedEndQuote = true;
                        break;
                    }
                    stream.consume();
                }
                if (!matchedEndQuote) {
                    MagicScriptError.error("字符串没有结束符\'", stream.endSpan(), new StringLiteralException());
                }
                Span stringSpan = stream.endSpan();
                stringSpan = new Span(stringSpan.getSource(), stringSpan.getStart() - 1, stringSpan.getEnd());
                tokens.add(new Token(TokenType.StringLiteral, stringSpan));
                continue;
            }

            // String literal
            if (stream.match(TokenType.DoubleQuote.getLiteral(), true)) {
                stream.startSpan();
                boolean matchedEndQuote = false;
                while (stream.hasMore()) {
                    // Note: escape sequences like \n are parsed in StringLiteral
                    if (stream.match("\\", true)) {
                        stream.consume();
                    }
                    if (stream.match(TokenType.DoubleQuote.getLiteral(), true)) {
                        matchedEndQuote = true;
                        break;
                    }
                    stream.consume();
                }
                if (!matchedEndQuote) {
                    MagicScriptError.error("字符串没有结束符\"", stream.endSpan(), new StringLiteralException());
                }
                Span stringSpan = stream.endSpan();
                stringSpan = new Span(stringSpan.getSource(), stringSpan.getStart() - 1, stringSpan.getEnd());
                tokens.add(new Token(TokenType.StringLiteral, stringSpan));
                continue;
            }

            // Identifier, keyword, boolean literal, or null literal
            if (stream.matchIdentifierStart(true)) {
                stream.startSpan();
                while (stream.matchIdentifierPart(true)) {
                    ;
                }
                Span identifierSpan = stream.endSpan();
                identifierSpan = new Span(identifierSpan.getSource(), identifierSpan.getStart() - 1, identifierSpan.getEnd());

                if ("true".equals(identifierSpan.getText()) || "false".equals(identifierSpan.getText())) {
                    tokens.add(new Token(TokenType.BooleanLiteral, identifierSpan));
                } else if ("null".equals(identifierSpan.getText())) {
                    tokens.add(new Token(TokenType.NullLiteral, identifierSpan));
                } else {
                    tokens.add(new Token(TokenType.Identifier, identifierSpan));
                }
                continue;
            }
            // Simple tokens
            for (TokenType t : TokenType.getSortedValues()) {
                if (t.getLiteral() != null) {
                    if (stream.match(t.getLiteral(), true)) {
                        if (t == TokenType.LeftCurly) {
                            leftCount++;
                        }
                        tokens.add(new Token(t, new Span(source, stream.getPosition() - t.getLiteral().length(), stream.getPosition())));
                        continue outer;
                    }
                }
            }
            if (leftCount != rightCount && stream.match("}", true)) {
                rightCount++;
                tokens.add(new Token(TokenType.RightCurly, new Span(source, stream.getPosition() - 1, stream.getPosition())));
                continue outer;
            }
            if (stream.hasMore()) {
                MagicScriptError.error("Unknown token", new Span(source, stream.getPosition(), stream.getPosition() + 1));
            }
        }
        return tokens;
    }
}
