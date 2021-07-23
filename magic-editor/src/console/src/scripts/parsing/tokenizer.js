import {CharacterStream, LiteralToken, ParseException, Token, TokenType} from './index.js'

let regexpToken = (stream, tokens) => {
    if (tokens.length > 0) {
        let token = tokens[tokens.length - 1];
        if (token instanceof LiteralToken || token.getTokenType() == TokenType.Identifier) {
            return false;
        }
    }
    if (stream.match("/", false)) {
        let mark = stream.getPosition();
        stream.consume();
        stream.startSpan();
        let matchedEndQuote = false;
        let deep = 0;
        let maybeMissForwardSlash = 0;
        let maybeMissForwardSlashEnd = 0;
        while (stream.hasMore()) {
            // Note: escape sequences like \n are parsed in StringLiteral
            if (stream.match("\\", true)) {
                stream.consume();
                continue;
            }
            if (stream.match("[", false)) {
                deep++;
                maybeMissForwardSlash = stream.getPosition();
            } else if (deep > 0 && stream.match("]", false)) {
                deep--;
            } else if (stream.match(TokenType.ForwardSlash.literal, true)) {
                if (deep == 0) {
                    if (stream.match("g", true)) {
                    }
                    if (stream.match("i", true)) {
                    }
                    if (stream.match("m", true)) {
                    }
                    if (stream.match("s", true)) {
                    }
                    if (stream.match("u", true)) {
                    }
                    if (stream.match("y", true)) {
                    }
                    matchedEndQuote = true;
                    break;
                } else {
                    maybeMissForwardSlashEnd = stream.getPosition();
                }
            }
            let ch = stream.consume();
            if (ch == '\r' || ch == '\n') {
                stream.reset(mark);
                return false;
            }
        }
        if (deep != 0) {
            throw new ParseException("Missing ']'", stream.getSpan(maybeMissForwardSlash, maybeMissForwardSlashEnd - 1));
        }
        if (!matchedEndQuote) {
            stream.reset(mark);
            return false;
        }
        let regexpSpan = stream.endSpan();
        regexpSpan = stream.getSpan(regexpSpan.getStart() - 1, regexpSpan.getEnd());
        tokens.push(new Token(TokenType.RegexpLiteral, regexpSpan));
        return true;
    }
    return false;
}

let stringToken = (stream, tokens) => {
    // String literal
    if (stream.match(TokenType.SingleQuote.literal, true)) {
        stream.startSpan();
        let matchedEndQuote = false;
        while (stream.hasMore()) {
            // Note: escape sequences like \n are parsed in StringLiteral
            if (stream.match("\\", true)) {
                stream.consume();
                continue;
            }
            if (stream.match(TokenType.SingleQuote.literal, true)) {
                matchedEndQuote = true;
                break;
            }
            let ch = stream.consume();
            if (ch == '\r' || ch == '\n') {
                throw new ParseException("''定义的字符串不能换行", stream.endSpan());
            }
        }
        if (!matchedEndQuote) {
            throw new ParseException("字符串没有结束符\'", stream.endSpan());
        }
        let stringSpan = stream.endSpan();
        stringSpan = stream.getSpan(stringSpan.getStart() - 1, stringSpan.getEnd());
        tokens.push(new LiteralToken(TokenType.StringLiteral, stringSpan));
        return true;
    }

    // String literal
    if (stream.match('"""', true)) {
        stream.startSpan();
        let matchedEndQuote = false;
        while (stream.hasMore()) {
            // Note: escape sequences like \n are parsed in StringLiteral
            if (stream.match("\\", true)) {
                stream.consume();
                continue;
            }
            if (stream.match('"""', true)) {
                matchedEndQuote = true;
                break;
            }
            stream.consume();
        }
        if (!matchedEndQuote) {
            throw new ParseException('多行字符串没有结束符"""', stream.endSpan());
        }
        let stringSpan = stream.endSpan();
        stringSpan = stream.getSpan(stringSpan.getStart() - 1, stringSpan.getEnd() - 2);
        tokens.push(new LiteralToken(TokenType.StringLiteral, stringSpan));
        return true;
    }

    // String literal
    if (stream.match(TokenType.DoubleQuote.literal, true)) {
        stream.startSpan();
        let matchedEndQuote = false;
        while (stream.hasMore()) {
            // Note: escape sequences like \n are parsed in StringLiteral
            if (stream.match("\\", true)) {
                stream.consume();
                continue;
            }
            if (stream.match(TokenType.DoubleQuote.literal, true)) {
                matchedEndQuote = true;
                break;
            }
            let ch = stream.consume();
            if (ch === '\r' || ch === '\n') {
                throw new ParseException("\"\"定义的字符串不能换行", stream.endSpan());
            }
        }
        if (!matchedEndQuote) {
            throw new ParseException("字符串没有结束符\"", stream.endSpan());
        }
        let stringSpan = stream.endSpan();
        stringSpan = stream.getSpan(stringSpan.getStart(), stringSpan.getEnd() - 1);
        tokens.push(new LiteralToken(TokenType.StringLiteral, stringSpan));
        return true;
    }
    return false;
};
export default (source) => {
    let stream = new CharacterStream(source, 0, source.length);
    let tokens = [];
    let leftCount = 0;
    let rightCount = 0;
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
            let type = TokenType.IntegerLiteral;
            stream.startSpan();
            while (stream.matchDigit(true)) {
            }
            if (stream.match(TokenType.Period.literal, true)) {
                type = TokenType.DoubleLiteral;
                while (stream.matchDigit(true)) {
                }
            }
            if (stream.match("b", true) || stream.match("B", true)) {
                if (type === TokenType.DoubleLiteral) {
                    throw new ParseException('Byte literal can not have a decimal point.', stream.endSpan());
                }
                type = TokenType.ByteLiteral;
            } else if (stream.match("s", true) || stream.match("S", true)) {
                if (type === TokenType.DoubleLiteral) {
                    throw new ParseException('Short literal can not have a decimal point.', stream.endSpan());
                }
                type = TokenType.ShortLiteral;
            } else if (stream.match("l", true) || stream.match("L", true)) {
                if (type === TokenType.DoubleLiteral) {
                    throw new ParseException('Long literal can not have a decimal point.', stream.endSpan());
                }
                type = TokenType.LongLiteral;
            } else if (stream.match("f", true) || stream.match("F", true)) {
                type = TokenType.FloatLiteral;
            } else if (stream.match("d", true) || stream.match("D", true)) {
                type = TokenType.DoubleLiteral;
            } else if (stream.match("m", true) || stream.match("M", true)) {
                type = TokenType.DecimalLiteral;
            }
            tokens.push(new LiteralToken(type, stream.endSpan()));
            continue;
        }
        // string
        if (stringToken(stream, tokens)) {
            continue;
        }

        // regexp
        if (regexpToken(stream, tokens)) {
            continue;
        }

        // TODO exception
        if (stream.match("```", true)) {
            stream.startSpan();
            if (stream.matchIdentifierStart(true)) {
                while (stream.matchIdentifierPart(true)) {
                }
                let language = stream.endSpan();
                tokens.push(new Token(TokenType.Language, language));
                stream.startSpan();
                if (!stream.skipUntil("```")) {
                    throw new ParseException('```需要以```结尾', stream.endSpan());
                }
                tokens.push(new Token(TokenType.Language, stream.endSpan(-3)));
            } else {
                throw new ParseException('```后需要标识语言类型', stream.endSpan());
            }
        }
        // Identifier, keyword, boolean literal, or null literal
        if (stream.matchIdentifierStart(true)) {
            stream.startSpan();
            while (stream.matchIdentifierPart(true)) {
            }
            let identifierSpan = stream.endSpan();
            identifierSpan = stream.getSpan(identifierSpan.getStart() - 1, identifierSpan.getEnd());
            if ("true" === identifierSpan.getText() || "false" === identifierSpan.getText()) {
                tokens.push(new LiteralToken(TokenType.BooleanLiteral, identifierSpan));
            } else if ("null" === identifierSpan.getText()) {
                tokens.push(new LiteralToken(TokenType.NullLiteral, identifierSpan));
            } else if (TokenType.SqlAnd.literal === identifierSpan.getText()) {
                tokens.push(new Token(TokenType.SqlAnd, identifierSpan));
            } else if (TokenType.SqlOr.literal === identifierSpan.getText()) {
                tokens.push(new Token(TokenType.SqlOr, identifierSpan));
            } else {
                tokens.push(new Token(TokenType.Identifier, identifierSpan));
            }
            continue;
        }
        if (stream.match("=>", true) || stream.match("->", true)) {
            tokens.push(new Token(TokenType.Lambda, stream.getSpan(stream.getPosition() - 2, stream.getPosition())));
            continue;
        }
        let outer = false;
        // Simple tokens
        let sortedTokens = TokenType.getSortedValues();
        for (let i = 0, len = sortedTokens.length; i < len; i++) {
            let t = sortedTokens[i];
            if (t.literal != null) {
                if (stream.match(t.literal, true)) {
                    if (t === TokenType.LeftCurly) {
                        leftCount++;
                    }
                    tokens.push(new Token(t, stream.getSpan(stream.getPosition() - t.literal.length, stream.getPosition())));
                    outer = true;
                    break;
                }
            }
        }
        if (outer) {
            continue;
        }
        if (leftCount !== rightCount && stream.match("}", true)) {
            rightCount++;
            tokens.push(new Token(TokenType.RightCurly, stream.getSpan(stream.getPosition() - 1, stream.getPosition())));
            continue;
        }
        if (stream.hasMore()) {
            throw new ParseException("Unknown token", stream.getSpan(stream.getPosition(), stream.getPosition() + 1));
        }
    }
    return tokens;
}