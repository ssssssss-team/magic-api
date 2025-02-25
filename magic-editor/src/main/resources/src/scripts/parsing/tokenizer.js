import {CharacterStream, LiteralToken, ParseException, Token, TokenStream, TokenType} from './index.js'

const regexpToken = (stream, tokens) => {
    if (tokens.length > 0) {
        let token = tokens[tokens.length - 1];
        if (token instanceof LiteralToken) {
            return false;
        }
        switch (token.getTokenType()){
            case TokenType.Comma :			// ,
            case TokenType.Semicolon :		// ;
            case TokenType.Colon:			// :
            case TokenType.RightCurly:		// }
            case TokenType.LeftBracket:		// [
            case TokenType.LeftParantheses:	// (
            case TokenType.Assignment:		// =
            case TokenType.NotEqual:		// !=
            case TokenType.EqualEqualEqual:	// ===
            case TokenType.NotEqualEqual:	// !==
            case TokenType.Equal:			// ==
            case TokenType.And:				// &&
            case TokenType.Or:				// ||
            case TokenType.SqlAnd:			// and
            case TokenType.SqlOr:			// or
            case TokenType.SqlNotEqual:		// <>
            case TokenType.Questionmark:	// ?
            case TokenType.Lambda:			// => ->
                break;
            default: return false;
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
                if (deep === 0) {
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
            if (ch === '\r' || ch === '\n') {
                stream.reset(mark);
                return false;
            }
        }
        if (deep !== 0) {
            throw new ParseException("Missing ']'", stream.getSpan(maybeMissForwardSlash, maybeMissForwardSlashEnd - 1));
        }
        if (!matchedEndQuote) {
            stream.reset(mark);
            return false;
        }
        let regexpSpan = stream.endSpan();
        regexpSpan = stream.getSpan(regexpSpan.getStart() - 1, regexpSpan.getEnd());
        tokens.push(new LiteralToken(TokenType.RegexpLiteral, regexpSpan));
        return true;
    }
    return false;
}

const tokenizerString = (stream, tokenType, tokens) => {
    // String literal
    if (stream.match(tokenType, true)) {
        stream.startSpan();
        let matchedEndQuote = false;
        while (stream.hasMore()) {
            // Note: escape sequences like \n are parsed in StringLiteral
            if (stream.match("\\", true)) {
                stream.consume();
                continue;
            }
            if (stream.match(tokenType.literal, true)) {
                matchedEndQuote = true;
                break;
            }
            let ch = stream.consume();
            if (tokenType !== TokenType.TripleQuote && (ch === '\r' || ch === '\n')) {
                throw new ParseException(tokenType.error + tokenType.error + "定义的字符串不能换行", stream.endSpan());
            }
        }
        if (!matchedEndQuote) {
            throw new ParseException("字符串没有结束符" + tokenType.error, stream.endSpan());
        }
        let stringSpan = stream.endSpan();
        stringSpan = stream.getSpan(stringSpan.getStart(), stringSpan.getEnd() - tokenType.literal.length);
        tokens.push(new LiteralToken(TokenType.StringLiteral, stringSpan));
        return true;
    }
    return false;
};
const autoNumberType = (span, radix) => {
    let value = Number.parseInt(span.getText().substring(2).replace(/\_/g,''), radix)
    if (value > 0x7fffffff || value < -0x80000000) {
        return new LiteralToken(TokenType.LongLiteral, span, value);
    } else if (value > 127 || value < -128) {
        return new LiteralToken(TokenType.LongLiteral, span, value);
    }
    return new LiteralToken(TokenType.ByteLiteral, span, value);
}
const tokenizerNumber = (stream, tokens) => {
    if (stream.match('0', false)) {
        let index = stream.getPosition();
        stream.startSpan();
        stream.consume();
        if (stream.matchAny(['x', 'X'], true)) {
            while (stream.matchDigit(true) || stream.matchAny(["A", "B", "C", "D", "E", "F", "a", "b", "c", "d", "e", "f", "_"], true)) {
                ;
            }
            if (stream.matchAny(["L", "l"], true)) {
                let span = stream.endSpan();
                let text = span.getText();
                tokens.push(new LiteralToken(TokenType.LongLiteral, span, parseInt(text.substring(2, text.length - 1).replace(/\_/g,''), 16)));
                return true;
            }
            tokens.push(autoNumberType(stream.endSpan(), 16));
            return true;
        } else if (stream.matchAny(['b','B'], true)){
            while (stream.matchAny([ '0', '1', '_'], true)) {
                ;
            }
            if (stream.matchAny([ "L", "l"], true)) {
                let span = stream.endSpan();
                let text = span.getText();
                tokens.push(new LiteralToken(TokenType.LongLiteral, span, parseInt(text.substring(2, text.length - 1).replace(/\_/g,''), 2)));
                return true;
            }
            tokens.push(autoNumberType(stream.endSpan(), 2));
            return true;
        }
        stream.reset(index);
    }
    if (stream.matchDigit(false)) {
        let type = TokenType.IntegerLiteral;
        stream.startSpan();
        while (stream.matchDigit(true) || stream.match('_', true)) {
        }
        if (stream.match(TokenType.Period.literal, true)) {
            if (stream.hasMore()) {
                type = TokenType.DoubleLiteral;
                while (stream.matchDigit(true) || stream.match('_',true)) {
                }
            } else {
                stream.reset(stream.getPosition() - 1)
            }
        }
        if (stream.matchAny(['b', 'B'], true)) {
            if (type === TokenType.DoubleLiteral) {
                throw new ParseException('Byte literal can not have a decimal point.', stream.endSpan());
            }
            type = TokenType.ByteLiteral;
        } else if (stream.matchAny(['s', 'S'], true)) {
            if (type === TokenType.DoubleLiteral) {
                throw new ParseException('Short literal can not have a decimal point.', stream.endSpan());
            }
            type = TokenType.ShortLiteral;
        } else if (stream.matchAny(['l', 'L'], true)) {
            if (type === TokenType.DoubleLiteral) {
                throw new ParseException('Long literal can not have a decimal point.', stream.endSpan());
            }
            type = TokenType.LongLiteral;
        } else if (stream.matchAny(['f', 'F'], true)) {
            type = TokenType.FloatLiteral;
        } else if (stream.matchAny(['d', 'D'], true)) {
            type = TokenType.DoubleLiteral;
        } else if (stream.matchAny(['m', 'M'], true)) {
            type = TokenType.DecimalLiteral;
        }
        tokens.push(new LiteralToken(type, stream.endSpan()));
        return true
    }
    return false;
}

const tokenizerLanguage = (stream, tokens) => {
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
            return true;
        } else {
            throw new ParseException('```后需要标识语言类型', stream.endSpan());
        }
    }
    return false;
}
const tokenizerIdentifier = (stream, tokens) => {
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
        return true;
    }
    return false;
}

const tokenizerTemplateString = (stream, tokens)=>{
    if (stream.match("`", true)) {
        let begin = stream.getPosition();
        let start = begin;
        let matchedEndQuote = false;
        let subTokens = [];
        while (stream.hasMore()) {
            if (stream.match("\\", true)) {
                stream.consume();
                continue;
            }
            if (stream.match("`", true)) {
                matchedEndQuote = true;
                break;
            }
            if (stream.match("${", true)) {
                let end = stream.getPosition();
                if (start < end - 2) {
                    subTokens.push(new LiteralToken(TokenType.StringLiteral, stream.endSpan(start, end - 2)));
                }
                subTokens.push(...tokenizer(stream, [], "}"));
                start = stream.getPosition();
                continue;
            }
            stream.consume();
        }
        let stringSpan = stream.endSpan(begin, stream.getPosition());
        let end = stream.getPosition() - 1;
        if (end - start > 0) {
            subTokens.push(new LiteralToken(TokenType.StringLiteral, stream.endSpan(start, end)));
        }
        stringSpan = stream.getSpan(stringSpan.getStart() - 1, stringSpan.getEnd());
        tokens.push(new LiteralToken(TokenType.StringLiteral, stringSpan, new TokenStream(subTokens)));
        return true;
    }
    return false;
}

const tokenizer = (stream, tokens, except) => {
    let leftCount = 0;
    let rightCount = 0;
    while (stream.hasMore()) {
        stream.skipWhiteSpace();
        if (except && stream.match(except, true)) {
            return tokens;
        }
        if (stream.match("//", true)) {    //注释
            stream.skipLine();
            continue;
        }
        if (stream.match("/*", true)) {    //多行注释
            stream.skipUntil("*/");
            continue;
        }
        // int short double long float byte decimal
        if (tokenizerNumber(stream, tokens)) {
            continue;
        }
        // '' "" """ """
        if (tokenizerString(stream, TokenType.SingleQuote, tokens) || tokenizerString(stream, TokenType.TripleQuote, tokens) || tokenizerString(stream, TokenType.DoubleQuote, tokens)) {
            continue;
        }

        // regexp
        if (regexpToken(stream, tokens)) {
            continue;
        }
        // ``` ```
        if(tokenizerLanguage(stream, tokens)){
            continue;
        }
        // template string
        if (tokenizerTemplateString(stream, tokens)) {
            continue;
        }

        // Identifier, keyword, boolean literal, or null literal
        if(tokenizerIdentifier(stream, tokens)){
            continue;
        }
        // lambda
        if (stream.matchAny(['=>','->'], true)) {
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

export default (source) => {
    return tokenizer(new CharacterStream(source, 0, source.length), [])
}