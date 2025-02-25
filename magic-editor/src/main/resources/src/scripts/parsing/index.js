class ParseException extends Error {
    constructor(message, span) {
        super(message)
        this.name = 'ParseException'
        this.span = span
    }
}

class Line {
    constructor(source, start, end, lineNumber, endLineNumber, startCol, endCol) {
        this.source = source;
        this.start = start;
        this.end = end;
        this.lineNumber = lineNumber;
        this.endLineNumber = endLineNumber;
        this.startCol = startCol;
        this.endCol = endCol;
    }
}

class Span {
    constructor(source, start, end) {
        if (source instanceof Span && start instanceof Span) {
            this.source = source.source;
            this.start = source.start;
            this.end = start.end;
            this.cachedText = this.source.substring(this.start, this.end);
        } else {
            this.source = source;
            this.start = start || 0;
            this.end = end || source.length;
            this.cachedText = source.substring(this.start, this.end);
        }
    }

    getText() {
        return this.cachedText;
    }

    getSource() {
        return this.source;
    }

    getStart() {
        return this.start;
    }

    getEnd() {
        return this.end;
    }

    toString() {
        return "Span [text=" + this.getText() + ", start=" + this.start + ", end=" + this.end + "]";
    }

    inPosition(position) {
        return this.start <= position && this.end >= position;
    }

    getLine() {
        if (this.line != null) {
            return this.line;
        }
        let lineStart = this.start;
        while (lineStart < this.end) {
            if (lineStart < 0) {
                break;
            }
            let c = this.source.charAt(lineStart);
            if (c === '\n') {
                lineStart = lineStart + 1;
                break;
            }
            lineStart--;
        }
        if (lineStart < 0) {
            lineStart = 0;
        }

        let lineEnd = this.end;
        while (true) {
            if (lineEnd > this.source.length - 1) {
                break;
            }
            let c = this.source.charAt(lineEnd);
            if (c === '\n') {
                break;
            }
            lineEnd++;
        }

        let lineNumber = 0;
        let idx = lineStart;
        while (idx > 0 && idx < this.end) {
            let c = this.source.charAt(idx);
            if (c === '\n') {
                lineNumber++;
            }
            idx--;
        }
        lineNumber++;
        idx = lineStart + 1;
        let endLineNumber = lineNumber;
        while (idx < lineEnd) {
            let c = this.source.charAt(idx);
            if (c === '\n') {
                endLineNumber++;
            }
            idx++;
        }
        let startCol = this.start - lineStart + 1;
        let endCol = startCol + this.end - this.start - 1;
        this.line = new Line(this.source, lineStart, lineEnd, lineNumber, endLineNumber, startCol, endCol);
        return this.line;
    }
}

const TokenType = {
    Spread: {literal: '...', error: '...'},
    Period: {literal: '.', error: '.'},
    QuestionPeriod: {literal: '?.', error: '?.'},
    Comma: {literal: ',', error: ','},
    Semicolon: {literal: ';', error: ';'},
    Colon: {literal: ':', error: ':'},
    Plus: {literal: '+', error: '+'},
    Minus: {literal: '-', error: '-'},
    Asterisk: {literal: '*', error: '*'},
    ForwardSlash: {literal: '/', error: '/'},
    PostSlash: {literal: '\\', error: '\\'},
    Percentage: {literal: '%', error: '%'},
    LeftParantheses: {literal: '(', error: '('},
    RightParantheses: {literal: ')', error: ')'},
    LeftBracket: {literal: '[', error: '['},
    RightBracket: {literal: ']', error: ']'},
    LeftCurly: {literal: '{', error: '{'},
    RightCurly: {error: '}'},// 特殊待遇！
    Less: {literal: '<', error: '<'},
    Greater: {literal: '>', error: '>'},
    LessEqual: {literal: '<=', error: '<='},
    GreaterEqual: {literal: '>=', error: '>='},
    Equal: {literal: '==', error: '=='},
    NotEqual: {literal: '!=', error: '!='},
    Assignment: {literal: '=', error: '='},
    PlusPlus: {literal: '++', error: '++'},
    MinusMinus: {literal: '--', error: '--'},
    PlusEqual: {literal: '+=', error: '+='},
    MinusEqual: {literal: '-=', error: '-='},
    AsteriskEqual: {literal: '*=', error: '*='},
    ForwardSlashEqual: {literal: '/=', error: '/='},
    PercentEqual: {literal: '%=', error: '%='},
    ColonColon: {literal: '::', error: '::'},
    EqualEqualEqual: {literal: '===', error: '==='},
    NotEqualEqual: {literal: '!==', error: '!=='},
    And: {literal: '&&', error: '&&'},
    Or: {literal: '||', error: '||'},
    Xor: {literal: '^', error: '^'},
    Not: {literal: '!', error: '!'},
    BitAnd: {literal:'&', error: '&'},
    BitOr: {literal:'|', error: '|'},
    BitNot: {literal:'~', error: '~'},
    LShift: {literal:'<<', error: '<<'},
    RShift: {literal:'>>', error: '>>'},
    RShift2: {literal:'>>>', error: '>>>'},
    XorEqual: {literal:'^=', error: '^=', modifiable: true},
    BitAndEqual: {literal:'&=', error: '&=', modifiable: true},
    BitOrEqual: {literal:'|=', error: '|=', modifiable: true},
    LShiftEqual: {literal:'<<=', error: '<<=', modifiable: true},
    RShiftEqual: {literal:'>>=', error: '>>=', modifiable: true},
    RShift2Equal: {literal:'>>>=', error: '>>>=', modifiable: true},


    SqlAnd: {literal: 'and', error: 'and'},
    SqlOr: {literal: 'or', error: 'or'},
    SqlNotEqual: {literal: '<>', error: '<>', inLinq: true},
    Questionmark: {literal: '?', error: '?'},
    DoubleQuote: {literal: '"', error: '"'},
    TripleQuote: {literal: '"""', error: '"""'},
    SingleQuote: {literal: '\'', error: '\''},
    Lambda: {error: '=> 或 ->'},
    BooleanLiteral: {error: 'true 或 false'},
    DoubleLiteral: {error: '一个 double 类型数值'},
    DecimalLiteral: {error: '一个 BigDecimal 类型数值'},
    FloatLiteral: {error: '一个 float 类型数值'},
    LongLiteral: {error: '一个 long 类型数值'},
    IntegerLiteral: {error: '一个 int 类型数值'},
    ShortLiteral: {error: '一个 short 类型数值'},
    ByteLiteral: {error: '一个 byte 类型数据'},
    CharacterLiteral: {error: '一个 char 类型数据'},
    RegexpLiteral: {error: '一个 正则表达式'},
    StringLiteral: {error: '一个 字符串'},
    NullLiteral: {error: 'null'},
    Language: {error: 'language'},
    Identifier: {error: '标识符'},
    Unknown: {error: 'unknown'}
};
let tokenTypeValues = Object.getOwnPropertyNames(TokenType).map(e => TokenType[e]);
TokenType.getSortedValues = function () {
    if (this.values) {
        return this.values;
    }
    this.values = tokenTypeValues.sort(function (o1, o2) {
        if (!o1.literal && !o2.literal) {
            return 0;
        }
        if (!o1.literal && !!o2.literal) {
            return 1;
        }
        if (!!o1.literal && !o2.literal) {
            return -1;
        }
        return o2.literal.length - o1.literal.length;
    });
    return this.values;
};

class Token {
    constructor(tokenType, span, valueOrTokenStream) {
        this.type = tokenType;
        this.span = span;
        if(valueOrTokenStream instanceof TokenStream){
            this.tokenStream = valueOrTokenStream;
        }else if(valueOrTokenStream){
            this.value = valueOrTokenStream;
        }
    }

    getTokenType() {
        return this.type;
    }

    getTokenStream() {
        return this.tokenStream;
    }

    getSpan() {
        return this.span;
    }

    getText() {
        return this.span.getText();
    }
}

class LiteralToken extends Token {
    constructor(tokenType, span, valueOrTokenStream) {
        super(tokenType, span, valueOrTokenStream)
    }

    getJavaType() {
        if (this.type === TokenType.StringLiteral) {
            return 'java.lang.String'
        }
        if (this.type === TokenType.DoubleLiteral) {
            return 'java.lang.Double'
        }
        if (this.type === TokenType.ByteLiteral) {
            return 'java.lang.Byte'
        }
        if (this.type === TokenType.FloatLiteral) {
            return 'java.lang.Float'
        }
        if (this.type === TokenType.DecimalLiteral) {
            return 'java.math.BigDecimal'
        }
        if (this.type === TokenType.IntegerLiteral) {
            return 'java.lang.Integer'
        }
        if (this.type === TokenType.LongLiteral) {
            return 'java.lang.Long'
        }
        if (this.type === TokenType.BooleanLiteral) {
            return 'java.lang.Boolean'
        }
        if (this.type === TokenType.RegexpLiteral) {
            return 'java.util.regex.Pattern'
        }
        return 'java.lang.Object'

    }
}

class CharacterStream {
    constructor(source, start, end) {
        this.index = start === undefined ? 0 : start;
        this.end = end === undefined ? source.length : end;
        this.source = source;
        this.spanStart = 0;
    }

    hasMore() {
        return this.index < this.end;
    }

    consume() {
        return this.source.charAt(this.index++);
    }

    match(needle, consume) {
        if (typeof needle !== 'string') {
            needle = needle.literal;
        }
        let needleLength = needle.length;
        if (needleLength + this.index > this.end) {
            return false;
        }
        for (let i = 0, j = this.index; i < needleLength; i++, j++) {
            if (this.index >= this.end)
                return false;
            if (needle.charAt(i) !== this.source.charAt(j))
                return false;
        }
        if (consume)
            this.index += needleLength;
        return true;
    }
    matchAny(strs, consume) {
        for(let i=0,len = strs.length; i < len;i++){
            if(this.match(strs[i], consume)){
                return true;
            }
        }
        return false;
    }

    matchDigit(consume) {
        return this.matchAny('0123456789', consume)
    }

    matchIdentifierStart(consume) {
        if (this.index >= this.end)
            return false;
        let c = this.source.charAt(this.index);
        if (c.match(/[a-zA-Z0-9_\u4e00-\u9fa5]/) || c === '$' || c === '_' || c === '@') {
            if (consume)
                this.index++;
            return true;
        }
        return false;
    }

    matchIdentifierPart(consume) {
        if (this.index >= this.end)
            return false;
        let c = this.source.charAt(this.index);
        if (c.match(/[a-zA-Z0-9_\u4e00-\u9fa5]/) || c === '@') {
            if (consume)
                this.index++;
            return true;
        }
        return false;
    }

    skipWhiteSpace() {
        while (this.index < this.end) {
            let c = this.source.charAt(this.index);
            if (c === ' ' || c === '\n' || c === '\r' || c === '\t') {
                this.index++;
            } else {
                break;
            }
        }
    }

    getSpan(start, end) {
        return new Span(this.source, start, end);
    }

    skipLine() {
        while (this.index < this.end) {
            if (this.source.charAt(this.index++) === '\n') {
                break;
            }
        }
    }

    skipUntil(chars) {
        while (this.index < this.end) {
            let matched = true;
            for (let i = 0, len = chars.length; i < len && this.index + i < this.end; i++) {
                if (chars.charAt(i) !== this.source.charAt(this.index + i)) {
                    matched = false;
                    break;
                }
            }
            this.index += matched ? chars.length : 1;
            if (matched) {
                return true;
            }
        }
        return false;
    }

    startSpan() {
        this.spanStart = this.index;
    }

    endSpan(offsetOrStart, end) {
        if(end !== undefined) {
            return new Span(this.source, offsetOrStart, end)
        }
        return new Span(this.source, this.spanStart, this.index + (offsetOrStart || 0));
    }

    getPosition() {
        return this.index;
    }

    reset(position) {
        this.index = position;
    }
}

class TokenStream {
    constructor(tokens) {
        this.tokens = tokens;
        this.index = 0;
        this.end = tokens.length;
    }

    getEnd() {
        return this.end > 0 && this.tokens[this.end -1]
    }

    hasMore() {
        return this.index < this.end;
    }

    hasNext() {
        return this.index + 1 < this.end;
    }

    makeIndex() {
        return this.index;
    }

    resetIndex(index) {
        this.index = index;
    }

    getToken(consume) {
        let token = this.tokens[this.index];
        if (consume) {
            this.index++;
        }
        return token;
    }

    consume() {
        if (!this.hasMore()) {
            throw new Error('Reached the end of the source.');
        }
        return this.tokens[this.index++];
    }

    next() {
        if (!this.hasMore()) {
            throw new Error('Reached the end of the source.');
        }
        return this.tokens[++this.index];
    }

    prev() {
        if (this.index === 0) {
            throw new Error('Reached the end of the source.');
        }
        return this.tokens[--this.index];
    }

    getPrev() {
        if (this.index === 0) {
            throw new Error('Reached the end of the source.');
        }
        return this.tokens[this.index - 1];
    }

    match(tokenOrText, consume, ignoreCase) {
        if (this.index >= this.end) {
            return false;
        }
        let match = false;
        if (Array.isArray(tokenOrText)) {
            for (let i = 0, len = tokenOrText.length; i < len; i++) {
                if (this.match(tokenOrText[i], consume, ignoreCase)) {
                    return true;
                }
            }
        } else if (typeof tokenOrText == 'string') {
            if (this.tokens[this.index].getText() === tokenOrText || (ignoreCase === true && this.tokens[this.index].getText().toLowerCase() === tokenOrText.toLowerCase())) {
                match = true;
            }
        } else {
            if (this.tokens[this.index].type === tokenOrText) {
                match = true;
            }
        }
        if (match && consume) {
            this.index++;
        }
        return match;
    }

    textToString(tokenOrText) {
        if (typeof tokenOrText == 'string') {
            return tokenOrText;
        } else if (tokenOrText instanceof Token) {
            return tokenOrText.getText();
        } else if (Array.isArray(tokenOrText)) {
            let arr = [];
            tokenOrText.forEach(it => arr.push(this.textToString(it)));
            return arr.join(",")
        } else {
            return tokenOrText.error;
        }
    }

    expect(text, ignoreCase) {
        if (this.match(text, true, ignoreCase)) {
            return this.tokens[this.index - 1];
        } else {
            if (!this.hasMore()) {
                let span = this.tokens[this.index - 1].getSpan();
                return new Token(TokenType.Unknown, span);
            } else {
                let token = this.tokens[this.index];
                if (text instanceof Token) {
                    text = text.type.error;
                }
                throw new ParseException("Expected '" + this.textToString(text) + "', but got '" + token.getText() + "'", token.getSpan());
            }
        }
    }

    hasPrev() {
        return this.index > 0
    }

    getSource() {
        if (this.tokens.length === 0) {
            return null;
        }
        return this.tokens[0].getSpan().getSource();
    }
}

export {
    Span,
    Token,
    TokenType,
    CharacterStream,
    TokenStream,
    LiteralToken,
    ParseException
};