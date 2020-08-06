function Span(source, start, end) {
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

Span.prototype.getText = function () {
    return this.cachedText;
}
Span.prototype.getSource = function () {
    return this.source;
}
Span.prototype.getStart = function () {
    return this.start;
}
Span.prototype.getEnd = function () {
    return this.end;
}
Span.prototype.toString = function () {
    return "Span [text=" + this.getText() + ", start=" + this.start + ", end=" + this.end + "]";
    ;
}

function throwError() {
    var message = '';
    var span;
    for (var i = 0, len = arguments.length; i < len; i++) {
        var value = arguments[i];
        if (value instanceof Span) {
            span = value
        } else {
            message += value + ' ';
        }

    }
    throw {message: message, span: span};
}

function CharacterStream(source, start, end) {
    this.index = start === undefined ? 0 : start;
    this.end = end === undefined ? source.length : end;
    this.source = source;
    this.spanStart = 0;
}

CharacterStream.prototype.hasMore = function () {
    return this.index < this.end;
}
CharacterStream.prototype.consume = function () {
    return this.source.charAt(this.index++);
}
CharacterStream.prototype.match = function (needle, consume) {
    if (typeof needle !== 'string') {
        needle = needle.literal;
    }
    var needleLength = needle.length;
    if (needleLength + this.index > this.end) {
        return false;
    }
    for (var i = 0, j = this.index; i < needleLength; i++, j++) {
        if (this.index >= this.end) return false;
        if (needle.charAt(i) != this.source.charAt(j)) return false;
    }
    if (consume) this.index += needleLength;
    return true;
}
CharacterStream.prototype.matchDigit = function (consume) {
    if (this.index >= this.end) return false;
    var c = this.source.charAt(this.index);
    if (!isNaN(c)) {
        if (consume) this.index++;
        return true;
    }
    return false;
}
CharacterStream.prototype.matchIdentifierStart = function (consume) {
    if (this.index >= this.end) return false;
    var c = this.source.charAt(this.index);
    if (c.match(/\w/) || c == '$' || c == '_' || c == '@') {
        if (consume) this.index++;
        return true;
    }
    return false;
}
CharacterStream.prototype.matchIdentifierPart = function (consume) {
    if (this.index >= this.end) return false;
    var c = this.source.charAt(this.index);
    if (c.match(/\w/) || c == '$' || c == '_') {
        if (consume) this.index++;
        return true;
    }
    return false;
}
CharacterStream.prototype.skipWhiteSpace = function (consume) {
    while (true) {
        if (this.index >= this.end) return;
        var c = this.source.charAt(this.index);
        if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
            this.index++;
            continue;
        } else {
            break;
        }
    }
}
CharacterStream.prototype.getSpan = function (start, end) {
    return new Span(this.source, start, end);
}
CharacterStream.prototype.skipLine = function () {
    while (true) {
        if (this.index >= this.end) {
            return;
        }
        if (this.source.charAt(this.index++) == '\n') {
            break;
        }
    }
}
CharacterStream.prototype.skipUntil = function (chars) {
    while (true) {
        if (this.index >= this.end) {
            return;
        }
        var matched = true;
        for (var i = 0, len = chars.length; i < len && this.index + i < this.end; i++) {
            if (chars.charAt(i) != this.source.charAt(this.index + i)) {
                matched = false;
                break;
            }
        }
        this.index += matched ? chars.length : 1;
        if (matched) {
            break;
        }
    }
}
CharacterStream.prototype.startSpan = function () {
    this.spanStart = this.index;
}
CharacterStream.prototype.endSpan = function () {
    return new Span(this.source, this.spanStart, this.index);
}
CharacterStream.prototype.getPosition = function () {
    return this.index;
}

function Token(tokenType, span) {
    this.type = tokenType;
    this.span = span;
}

Token.prototype.getTokenType = function () {
    return this.type;
}
Token.prototype.getSpan = function () {
    return this.span;
}
Token.prototype.getText = function () {
    return this.span.getText();
}
var TokenType = {
    Period: {literal: '.', error: '.'},
    Lambda: {literal: '=>', error: '->'},
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
    And: {literal: '&&', error: '&&'},
    Or: {literal: '||', error: '||'},
    Xor: {literal: '^', error: '^'},
    Not: {literal: '!', error: '!'},
    Questionmark: {literal: '?', error: '?'},
    DoubleQuote: {literal: '"', error: '"'},
    SingleQuote: {literal: '\'', error: '\''},
    BooleanLiteral: {error: 'true 或 false'},
    DoubleLiteral: {error: '一个 double 类型数值'},
    DecimalLiteral: {error: '一个 BigDecimal 类型数值'},
    FloatLiteral: {error: '一个 float 类型数值'},
    LongLiteral: {error: '一个 long 类型数值'},
    IntegerLiteral: {error: '一个 int 类型数值'},
    ShortLiteral: {error: '一个 short 类型数值'},
    ByteLiteral: {error: '一个 byte 类型数据'},
    CharacterLiteral: {error: '一个 char 类型数据'},
    StringLiteral: {error: '一个 字符串'},
    NullLiteral: {error: 'null'},
    Identifier: {error: '标识符'}
};
var tokenTypeValues = Object.getOwnPropertyNames(TokenType).map(e => TokenType[e]);
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
    })
    return this.values;
}

function TokenStream(tokens) {
    this.tokens = tokens;
    this.index = 0;
    this.end = tokens.length;
}

TokenStream.prototype.hasMore = function () {
    return this.index < this.end;
}
TokenStream.prototype.hasNext = function () {
    return this.index + 1 < this.end;
}
TokenStream.prototype.makeIndex = function () {
    return this.index;
}
TokenStream.prototype.resetIndex = function (index) {
    this.index = index;
}
TokenStream.prototype.getToken = function (consume) {
    var token = this.tokens[this.index];
    if (consume) {
        this.index++;
    }
    return token;
}
TokenStream.prototype.consume = function () {
    if (!this.hasMore()) {
        throwError('Reached the end of the source.')
    }
    return this.tokens[this.index++];
}
TokenStream.prototype.next = function () {
    if (!this.hasMore()) {
        throwError('Reached the end of the source.')
    }
    return this.tokens[++this.index];
}
TokenStream.prototype.prev = function () {
    if (this.index == 0) {
        throwError('Reached the end of the source.')
    }
    return this.tokens[--this.index];
}
TokenStream.prototype.getPrev = function () {
    if (this.index == 0) {
        throwError('Reached the end of the source.')
    }
    return this.tokens[this.index - 1];
}
TokenStream.prototype.match = function (tokenOrText, consume) {
    if (this.index >= this.end) {
        return false;
    }
    var match = false;
    if (Array.isArray(tokenOrText)) {
        for (var i = 0, len = tokenOrText.length; i < len; i++) {
            if (this.match(tokenOrText[i], consume)) {
                return true;
            }
        }
    } else if (typeof tokenOrText == 'string') {
        if (this.tokens[this.index].getText() == tokenOrText) {
            match = true
        }
    } else {
        if (this.tokens[this.index].type == tokenOrText) {
            match = true
        }
    }
    if (match && consume) {
        this.index++;
    }
    return match;
}
TokenStream.prototype.expect = function (text) {
    if (this.match(text, true)) {
        return this.tokens[this.index - 1];
    } else {
        var token = this.index < this.tokens.length ? this.tokens[this.index] : null;
        var span = token != null ? token.getSpan() : null;
        if (span == null) {
            throwError("Expected '" + text + "', but reached the end of the source.")
        } else {
            if (text instanceof Token) {
                text = text.type.error;
            }
            throw new Error();
            throwError("Expected '" + text + "', but got '" + token.getText() + "'", span)
        }
    }
}
TokenStream.prototype.getSource = function () {
    if (this.tokens.length == 0) {
        return null;
    }
    return this.tokens[0].getSpan().getSource();
}
var Parser = {
    scriptClass: {},
    extensions : {},
    importClass : [],
    tokenize: function (source) {
        var stream = new CharacterStream(source, 0, source.length);
        var tokens = [];
        var leftCount = 0;
        var rightCount = 0;
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
                var type = TokenType.IntegerLiteral;
                stream.startSpan();
                while (stream.matchDigit(true)) {
                    ;
                }
                if (stream.match(TokenType.Period.literal, true)) {
                    type = TokenType.FloatLiteral;
                    while (stream.matchDigit(true)) {
                        ;
                    }
                }
                if (stream.match("b", true) || stream.match("B", true)) {
                    if (type == TokenType.FloatLiteral) {
                        throwError("Byte literal can not have a decimal point.", stream.endSpan());
                    }
                    type = TokenType.ByteLiteral;
                } else if (stream.match("s", true) || stream.match("S", true)) {
                    if (type == TokenType.FloatLiteral) {
                        throwError("Short literal can not have a decimal point.", stream.endSpan());
                    }
                    type = TokenType.ShortLiteral;
                } else if (stream.match("l", true) || stream.match("L", true)) {
                    if (type == TokenType.FloatLiteral) {
                        throwError("Long literal can not have a decimal point.", stream.endSpan());
                    }
                    type = TokenType.LongLiteral;
                } else if (stream.match("f", true) || stream.match("F", true)) {
                    type = TokenType.FloatLiteral;
                } else if (stream.match("d", true) || stream.match("D", true)) {
                    type = TokenType.DoubleLiteral;
                } else if (stream.match("m", true) || stream.match("M", true)) {
                    type = TokenType.DecimalLiteral;
                }
                tokens.push(new Token(type, stream.endSpan()));
                continue;
            }
            // String literal
            if (stream.match(TokenType.SingleQuote.literal, true)) {
                stream.startSpan();
                var matchedEndQuote = false;
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
                    var ch = stream.consume();
                    if(ch == '\r' || ch == '\n'){
                       throwError("''定义的字符串不能换行", stream.endSpan());
                    }
                }
                if (!matchedEndQuote) {
                    throwError("字符串没有结束符\'", stream.endSpan());
                }
                var stringSpan = stream.endSpan();
                stringSpan = stream.getSpan(stringSpan.getStart() - 1, stringSpan.getEnd());
                tokens.push(new Token(TokenType.StringLiteral, stringSpan));
                continue;
            }

            // String literal
            if (stream.match('"""', true)) {
                stream.startSpan();
                var matchedEndQuote = false;
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
                    throwError('多行字符串没有结束符"""', stream.endSpan());
                }
                var stringSpan = stream.endSpan();
                stringSpan = stream.getSpan(stringSpan.getStart() - 1, stringSpan.getEnd() - 2);
                tokens.push(new Token(TokenType.StringLiteral, stringSpan));
                continue;
            }

            // String literal
            if (stream.match(TokenType.DoubleQuote.literal, true)) {
                stream.startSpan();
                var matchedEndQuote = false;
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
                    var ch = stream.consume();
                    if(ch == '\r' || ch == '\n'){
                        throwError('"定义的字符串不能换行', stream.endSpan());
                    }
                }
                if (!matchedEndQuote) {
                    throwError("字符串没有结束符\"", stream.endSpan());
                }
                var stringSpan = stream.endSpan();
                stringSpan = stream.getSpan(stringSpan.getStart() - 1, stringSpan.getEnd());
                tokens.push(new Token(TokenType.StringLiteral, stringSpan));
                continue;
            }
            // Identifier, keyword, boolean literal, or null literal
            if (stream.matchIdentifierStart(true)) {
                stream.startSpan();
                while (stream.matchIdentifierPart(true)) {
                    ;
                }
                var identifierSpan = stream.endSpan();
                identifierSpan = stream.getSpan(identifierSpan.getStart() - 1, identifierSpan.getEnd());
                if ("true" == (identifierSpan.getText()) || "false" == (identifierSpan.getText())) {
                    tokens.push(new Token(TokenType.BooleanLiteral, identifierSpan));
                } else if ("null" == (identifierSpan.getText())) {
                    tokens.push(new Token(TokenType.NullLiteral, identifierSpan));
                } else {
                    tokens.push(new Token(TokenType.Identifier, identifierSpan));
                }
                continue;
            }
            var continueOuter = false;
            // Simple tokens
            var values = TokenType.getSortedValues();
            for (var i = 0, len = values.length; i < len; i++) {
                var t = values[i];
                if (t.literal != null) {
                    if (stream.match(t.literal, true)) {
                        if (t == TokenType.LeftCurly) {
                            leftCount++;
                        }
                        tokens.push(new Token(t, stream.getSpan(stream.getPosition() - t.literal.length, stream.getPosition())));
                        continueOuter = true;
                        break;
                    }
                }
            }
            if (continueOuter) {
                continue;
            }
            if (leftCount != rightCount && stream.match("}", true)) {
                rightCount++;
                tokens.push(new Token(TokenType.RightCurly, stream.getSpan(stream.getPosition() - 1, stream.getPosition())));
                continueOuter = true;
            }
            if (continueOuter) {
                continue;
            }
            if (stream.hasMore()) {
                throwError("Unknown token", stream.getSpan(stream.getPosition(), stream.getPosition() + 1));
            }
        }
        return tokens;
    },
    getSimpleClass: function (target) {
        var index = target.lastIndexOf('.')
        if (index > -1) {
            return target.substring(index + 1);
        }
        return target;
    },
    getWrapperClass: function (target) {
        if (target == 'int' || target == 'java.lang.Integer') {
            return 'java.lang.Integer';
        }
        if (target == 'string' || target == 'java.lang.String') {
            return 'java.lang.String';
        }
        if (target == 'double' || target == 'java.lang.Double') {
            return 'java.lang.Double';
        }
        if (target == 'float' || target == 'java.lang.Float') {
            return 'java.lang.Float';
        }
        if (target == 'byte' || target == 'java.lang.Byte') {
            return 'java.lang.Byte';
        }
        if (target == 'short' || target == 'java.lang.Short') {
            return 'java.lang.Short';
        }
        if (target == 'long' || target == 'java.lang.Long') {
            return 'java.lang.Long';
        }
        if (target.indexOf('[]') > -1) {
            return '[Ljava.lang.Object;';
        }
        return target || 'java.lang.Object';
    },
    parse: function (stream) {
        try{
            var vars = {
                db: 'org.ssssssss.magicapi.functions.DatabaseQuery'
            };
            var expression;
            while (stream.hasMore()) {
                var token = stream.consume();
                if (token.type == TokenType.Identifier && token.getText() == 'var') {
                    var varName = stream.consume().getText();
                    if (stream.match(TokenType.Assignment, true)) {
                        var value = this.parseStatement(stream);
                        vars[varName] = value.getJavaType(vars);
                        if (!stream.hasMore()) {
                            expression = value;
                        }
                    }
                } else if (token.type == TokenType.Identifier && token.getText() == 'import') {
                    var varName;
                    var value;
                    if (stream.match(TokenType.Identifier, false)) {
                        varName = stream.consume().getText();
                        value = varName;
                    } else if (stream.match(TokenType.StringLiteral, false)) {
                        value = stream.consume().getText();
                        value = value.substring(1, value.length - 1);
                    }
                    if (stream.match('as', true)) {
                        varName = stream.consume().getText();
                    }
                    if (Parser.scriptClass[value] === undefined) {
                        MagicEditor.ajax({
                            url: 'class',
                            data: {
                                className: value
                            },
                            success: function (list) {
                                Parser.scriptClass[value] = null;
                                for (var i = 0, len = list.length; i < len; i++) {
                                    var item = list[i];
                                    Parser.scriptClass[item.className] = item;
                                }
                            }
                        })
                    }
                    if (varName) {
                        vars[varName] = value;
                    }
                } else if (token.type == TokenType.Assignment) {
                    var varName = stream.getPrev().getText()
                    var value = this.parseStatement(stream);
                    vars[varName] = value.getJavaType(vars);
                    if (!stream.hasMore()) {
                        expression = value;
                    }
                } else if (token.type == TokenType.Identifier) {
                    var index = stream.makeIndex();
                    stream.prev();
                    try {
                        expression = this.parseAccessOrCall(stream, token.type);
                    } catch (e) {
                        expression = null;
                        stream.resetIndex(index);
                    }
                }
            }
            var type = expression && expression.getJavaType(vars);
            return type;
        }catch(e){
            return '';
        }
    },
    parseStatement: function (tokens, expectRightCurly) {
        var result = null;
        if (tokens.match("import", false)) {
            result = this.parseImport(tokens);
        } else if (tokens.match("var", false)) {
            result = this.parseVarDefine(tokens);
        } else if (tokens.match("if", false)) {
            result = this.parseIfStatement(tokens);
        } else if (tokens.match("return", false)) {
            result = this.parseReturn(tokens);
        } else if (tokens.match("for", false)) {
            result = this.parseForStatement(tokens);
        } else if (tokens.match("continue", false)) {
            result = new AST.Continue(tokens.consume().getSpan());
        } else if (tokens.match("break", false)) {
            result = new AST.Break(tokens.consume().getSpan());
        } else {
            result = this.parseExpression(tokens, expectRightCurly);
        }
        // consume semi-colons as statement delimiters
        while (tokens.match(";", true)) {
            ;
        }
        return result;
    },
    parseReturn : function(stream){
        stream.expect("return");
        if (stream.match(";", false)) return new AST.Return(null);
        return new AST.Return(this.parseExpression(stream));
    },
    parseImport: function (stream) {
        var opening = stream.expect("import").getSpan();
        if (stream.hasMore()) {
            var expected = stream.consume();
            var packageName = null;
            var isStringLiteral = expected.getType() == TokenType.StringLiteral;
            if (isStringLiteral) {
                packageName = new AST.TypeLiteral('string', expected.getSpan()).getValue();
            } else if (expected.getType() == TokenType.Identifier) {
                packageName = expected.getSpan().getText();
            } else {
                throwError("Expected identifier or string, but got stream is " + expected.getType().getError(), stream.getPrev().getSpan());
            }
            var varName = packageName;
            if (isStringLiteral || stream.match("as", false)) {
                stream.expect("as");
                expected = stream.expect(TokenType.Identifier);
                varName = expected.getSpan().getText();
            }
            return new AST.Import(packageName, varName, !isStringLiteral);
        }
        throwError("Expected identifier or string, but got stream is EOF", stream.getPrev().getSpan());
    },
    parseVarDefine: function (stream) {
        var opening = stream.expect("var").getSpan();
        var expected = null;
        if (stream.hasMore()) {
            expected = TokenType.Identifier;
            var token = stream.expect(expected);
            var variableName = token.getSpan().getText();
            expected = TokenType.Assignment;
            if (stream.hasMore()) {
                stream.expect(expected);
                return new AST.VariableDefine(new Span(opening, stream.getPrev().getSpan()), variableName, this.parseExpression(stream));
            }
        }
        throwError("Expected " + expected.getError() + ", but got stream is EOF", stream.getPrev().getSpan());
    },
    expectCloseing: function (stream) {
        if (!stream.hasMore()) {
            throwError("Did not find closing }.", stream.prev().getSpan());
        }
        return stream.expect("}").getSpan();
    },
    parseIfStatement: function (stream) {
        var openingIf = stream.expect("if").getSpan();
        var condition = this.parseExpression(stream);
        stream.expect("{");
        var trueBlock = [];
        while (stream.hasMore() && !stream.match("}", false)) {
            var node = this.parseStatement(stream, true);
            if (node != null) {
                trueBlock.push(node);
            }
        }
        this.expectCloseing(stream);
        var elseIfs = [];
        var falseBlock = [];
        while (stream.hasMore() && stream.match("else", true)) {
            if (stream.hasMore() && stream.match("if", false)) {
                var elseIfOpening = stream.expect("if").getSpan();
                var elseIfCondition = this.parseExpression(stream);
                stream.expect("{");
                var elseIfBlock = [];
                while (stream.hasMore() && !stream.match("}", false)) {
                    var node = this.parseStatement(stream, true);
                    if (node != null) {
                        elseIfBlock.push(node);
                    }
                }
                this.expectCloseing(stream);
                var elseIfSpan = new Span(elseIfOpening, elseIfBlock.length > 0 ? elseIfBlock[elseIfBlock.length - 1].getSpan() : elseIfOpening);
                elseIfs.push(new AST.IfStatement(elseIfSpan, elseIfCondition, elseIfBlock, [], []));
            } else {
                stream.expect("{");
                while (stream.hasMore() && !stream.match("}", false)) {
                    falseBlock.push(this.parseStatement(stream, true));
                }
                this.expectCloseing(stream);
                break;
            }
        }
        var closingEnd = stream.getPrev().getSpan();
        return new AST.IfStatement(new Span(openingIf, closingEnd), condition, trueBlock, elseIfs, falseBlock);
    },
    parseNewExpression: function (stream) {
        var identifier = stream.expect(TokenType.Identifier);
        var args = this.parseArguments(stream);
        var closing = stream.expect(")").getSpan();
        return new AST.NewStatement(identifier.getText(), args);
    },
    parseExpression: function (stream, expectRightCurly) {
        return this.parseTernaryOperator(stream, expectRightCurly);
    },
    parseTernaryOperator: function (stream, expectRightCurly) {
        var condition = this.parseBinaryOperator(stream, 0, expectRightCurly);
        if (stream.match(TokenType.Questionmark, true)) {
            var trueExpression = this.parseTernaryOperator(stream, expectRightCurly);
            stream.expect(TokenType.Colon);
            var falseExpression = this.parseTernaryOperator(stream, expectRightCurly);
            return new AST.TernaryOperation(condition, trueExpression, falseExpression);
        } else {
            return condition;
        }
    },
    binaryOperatorPrecedence: [
        [TokenType.Assignment],
        [TokenType.Or, TokenType.And, TokenType.Xor],
        [TokenType.Equal, TokenType.NotEqual],
        [TokenType.Less, TokenType.LessEqual, TokenType.Greater, TokenType.GreaterEqual],
        [TokenType.Plus, TokenType.Minus],
        [TokenType.ForwardSlash, TokenType.Asterisk, TokenType.Percentage]
    ],
    unaryOperators: [TokenType.Not, TokenType.Plus, TokenType.Minus],
    parseBinaryOperator: function (stream, level, expectRightCurly) {
        var nextLevel = level + 1;
        var left = nextLevel == this.binaryOperatorPrecedence.length ? this.parseUnaryOperator(stream, expectRightCurly) : this.parseBinaryOperator(stream, nextLevel, expectRightCurly);
        var operators = this.binaryOperatorPrecedence[level];
        while (stream.hasMore() && stream.match(operators, false)) {
            var operator = stream.consume();
            var right = nextLevel == this.binaryOperatorPrecedence.length ? this.parseUnaryOperator(stream, expectRightCurly) : this.parseBinaryOperator(stream, nextLevel, expectRightCurly);
            left = new AST.BinaryOperation(left, operator, right);
        }

        return left;
    },
    parseUnaryOperator: function (stream, expectRightCurly) {
        if (stream.match(this.unaryOperators,false)) {
            return new AST.UnaryOperation(stream.consume(), this.parseUnaryOperator(stream, expectRightCurly));
        } else {
            if (stream.match(TokenType.LeftParantheses, false)) {    //(
                var openSpan = stream.expect(TokenType.LeftParantheses).getSpan();
                var index = stream.makeIndex();
                var parameters = [];
                while (stream.match(TokenType.Identifier, false)) {
                    var identifier = stream.expect(TokenType.Identifier);
                    parameters.push(identifier.getSpan().getText());
                    if (stream.match(TokenType.Comma, true)) { //,
                        continue;
                    }
                    if (stream.match(TokenType.RightParantheses, true)) {  //)
                        if (stream.match(TokenType.Lambda, true)) {   // =>
                            return this.parseLambdaBody(stream, openSpan, parameters);
                        }
                        break;
                    }
                }
                if (stream.match(TokenType.RightParantheses, true) && stream.match(TokenType.Lambda, true)) {
                    return this.parseLambdaBody(stream, openSpan, parameters);
                }
                stream.resetIndex(index);
                var expression = this.parseExpression(stream);
                stream.expect(TokenType.RightParantheses);
                return expression;
            } else {
                return this.parseAccessOrCallOrLiteral(stream, expectRightCurly);
            }
        }
    },
    parseListLiteral: function (stream) {
        var openBracket = stream.expect(TokenType.LeftBracket).getSpan();

        var values = [];
        while (stream.hasMore() && !stream.match(TokenType.RightBracket, false)) {
            values.push(this.parseExpression(stream));
            if (!stream.match(TokenType.RightBracket, false)) {
                stream.expect(TokenType.Comma);
            }
        }
        var closeBracket = stream.expect(TokenType.RightBracket).getSpan();
        return new AST.TypeLiteral('list', new Span(openBracket, closeBracket), values);
    },
    parseMapLiteral: function (stream) {
        var openCurly = stream.expect(TokenType.LeftCurly).getSpan();
        var keys = [];
        var values = [];
        while (stream.hasMore() && !stream.match("}", false)) {
            var key;
            if (stream.match(TokenType.StringLiteral, false)) {
                key = stream.expect(TokenType.StringLiteral);
            } else {
                key = stream.expect(TokenType.Identifier);
            }
            keys.push(key);
            if (stream.match([TokenType.Comma, TokenType.RightCurly],false)) {
                stream.match(TokenType.Comma, true);
                if (key.getTokenType() == TokenType.Identifier) {
                    values.push(new AST.VariableAccess(key.getSpan()));
                } else {
                    values.push(new AST.StringLiteral(key.getSpan()));
                }
            } else {
                stream.expect(":");
                values.add(parseExpression(stream));
                if (!stream.match("}", false)) {
                    stream.expect(TokenType.Comma);
                }
            }
        }
        var closeCurly = stream.expect("}").getSpan();
        return new AST.TypeLiteral('map', new Span(openCurly, closeCurly), keys, values);
    },
    parseAccessOrCallOrLiteral: function (stream, expectRightCurly) {
        if (expectRightCurly && stream.match("}", false)) {
            return null;
        } else if (stream.match(TokenType.Identifier, false)) {
            return this.parseAccessOrCall(stream, TokenType.Identifier);
        } else if (stream.match(TokenType.LeftCurly, false)) {
            return this.parseMapLiteral(stream);
        } else if (stream.match(TokenType.LeftBracket, false)) {
            return this.parseListLiteral(stream);
        } else if (stream.match(TokenType.StringLiteral, false)) {
            if (stream.hasNext()) {
                if (stream.next().type == TokenType.Period) {
                    stream.prev();
                    return this.parseAccessOrCall(stream, TokenType.StringLiteral);
                }
                stream.prev();
            }

            return new AST.TypeLiteral('string', stream.expect(TokenType.StringLiteral).getSpan());
        } else if (stream.match(TokenType.BooleanLiteral, false)) {
            return new AST.TypeLiteral('boolean', stream.expect(TokenType.BooleanLiteral).getSpan());
        } else if (stream.match(TokenType.DoubleLiteral, false)) {
            return new AST.TypeLiteral('double', stream.expect(TokenType.DoubleLiteral).getSpan());
        } else if (stream.match(TokenType.FloatLiteral, false)) {
            return new AST.TypeLiteral('float', stream.expect(TokenType.FloatLiteral).getSpan());
        } else if (stream.match(TokenType.ByteLiteral, false)) {
            return new AST.TypeLiteral('byte', stream.expect(TokenType.ByteLiteral).getSpan());
        } else if (stream.match(TokenType.ShortLiteral, false)) {
            return new AST.TypeLiteral('short', stream.expect(TokenType.ShortLiteral).getSpan());
        } else if (stream.match(TokenType.IntegerLiteral, false)) {
            return new AST.TypeLiteral('int', stream.expect(TokenType.IntegerLiteral).getSpan());
        } else if (stream.match(TokenType.LongLiteral, false)) {
            return new AST.TypeLiteral('long', stream.expect(TokenType.LongLiteral).getSpan());
        } else if (stream.match(TokenType.DecimalLiteral, false)) {
            return new AST.TypeLiteral('BigDecimal', stream.expect(TokenType.LongLiteral).getSpan());
        } else if (stream.match(TokenType.NullLiteral, false)) {
            return new AST.TypeLiteral('null', stream.expect(TokenType.NullLiteral).getSpan());
        } else {
            throwError("Expected a variable, field, map, array, function or method call, or literal.", stream.consume().getText());
            return null; // not reached
        }
    },
    parseArguments: function (stream) {
        stream.expect(TokenType.LeftParantheses);
        var args = [];
        while (stream.hasMore() && !stream.match(TokenType.RightParantheses, false)) {
            args.push(this.parseExpression(stream));
            if (!stream.match(TokenType.RightParantheses, false)) stream.expect(TokenType.Comma);
        }
        return args;
    },
    parseAccessOrCall: function (stream, tokenType) {
        var identifier = stream.expect(tokenType).getSpan();
        if(tokenType == TokenType.Identifier && "new" == identifier.getText()){
            return this.parseNewExpression(stream);
        }
        if (tokenType == TokenType.Identifier && stream.match(TokenType.Lambda, true)) {
            return this.parseLambdaBody(stream, identifier, [identifier.getText()]);
        }
        var result = tokenType == TokenType.StringLiteral ? new AST.TypeLiteral('string', identifier) : new AST.VariableAccess(identifier);
        while (stream.hasMore() && stream.match([TokenType.LeftParantheses, TokenType.LeftBracket, TokenType.Period], false)) {
            // function or method call
            if (stream.match(TokenType.LeftParantheses, false)) {
                var args = this.parseArguments(stream);
                var closingSpan = stream.expect(TokenType.RightParantheses).getSpan();
                if (result instanceof AST.VariableAccess || result instanceof AST.MapOrArrayAccess)
                    result = new AST.FunctionCall(result, args);
                else if (result instanceof AST.MemberAccess) {
                    result = new AST.MethodCall(result, args);
                } else {
                    throwError("Expected a variable, field or method.", stream);
                }
            }

            // map or array access
            else if (stream.match(TokenType.LeftBracket, true)) {
                var keyOrIndex = this.parseExpression(stream);
                var closingSpan = stream.expect(TokenType.RightBracket).getSpan();
                result = new AST.MapOrArrayAccess(result, keyOrIndex);
            }

            // field or method access
            else if (stream.match(TokenType.Period, true)) {
                identifier = stream.expect(TokenType.Identifier).getSpan();
                result = new AST.MemberAccess(result, identifier);
            }
        }
        return result;
    },
    parseLambdaBody: function (stream, openSpan, parameters) {
        var index = stream.makeIndex();
        var childNodes = [];
        try {
            var expression = this.parseExpression(stream);
            childNodes.push(new AST.Return(new Span("return", 0, 6), expression));
            return new AST.LambdaFunction(expression);
        } catch (e) {
            stream.resetIndex(index);
            if (stream.match(TokenType.LeftCurly, true)) {
                while (stream.hasMore() && !stream.match("}",false)) {
                    childNodes.push(this.parseStatement(stream, true));
                }
                var closeSpan = this.expectCloseing(stream);
                return new AST.LambdaFunction(childNodes);
            } else {
                var node = this.parseStatement(stream);
                childNodes.push(new AST.Return(new Span("return", 0, 6), node));
                return new AST.LambdaFunction(node);
            }
        }
    }
}
var AST = {
    TypeLiteral: function (type) {
        this.type = type;
        this.getJavaType = function () {
            if (type == 'string') {
                return 'java.lang.String';
            }
            if (type == 'boolean') {
                return 'java.lang.Boolean';
            }
            if (type == 'int') {
                return 'java.lang.Integer';
            }
            if (type == 'short') {
                return 'java.lang.Short';
            }
            if (type == 'byte') {
                return 'java.lang.Byte';
            }
            if (type == 'double') {
                return 'java.lang.Double';
            }
            if (type == 'float') {
                return 'java.lang.Float';
            }
            if (type == 'long') {
                return 'java.lang.Long';
            }
            if (type == 'BigDecimal') {
                return 'java.math.BigDecimal';
            }
            if (type == 'null') {
                return null;
            }
            if (type == 'list') {
                return 'java.util.List';
            }
            if (type == 'map') {
                return 'java.util.Map';
            }
            return 'java.lang.Object';
        }
    },
    VariableDefine: function () {

    },
    TernaryOperation: function (condition, trueExpression, falseExpression) {

    },
    UnaryOperation: function () {

    },
    Return : function(result){
        this.getJavaType = function(env){
            return result == null ? '' : result.getJavaType(env);
        }
    },
    NewStatement: function (identifier) {
        this.getJavaType = function (env) {
            return env[identifier] || 'java.lang.Object';
        }
    },
    BinaryOperation: function (left, operator, right) {
        this.getJavaType = function () {
            if (operator.type == TokenType.Plus) {
                if (left.type == 'string' || right.type == 'string') {
                    return 'java.lang.String';
                }
            }
            if (left.type == 'BigDecimal' || right.type == 'BigDecimal') {
                return 'java.math.BigDecimal';
            }
            if (left.type == 'double' || right.type == 'double') {
                return 'java.lang.Double';
            }
            if (left.type == 'float' || right.type == 'float') {
                return 'java.lang.Float';
            }
            if (left.type == 'long' || right.type == 'long') {
                return 'java.lang.Long';
            }
            if (left.type == 'int' || right.type == 'int') {
                return 'java.lang.Integer';
            }
            if (left.type == 'short' || right.type == 'short') {
                return 'java.lang.Short';
            }
            if (left.type == 'byte' || right.type == 'byte') {
                return 'java.lang.Byte';
            }
            return 'java.lang.Object';
        }
    },
    LambdaFunction: function (returnValue) {
        this.getJavaType = function (env) {
            if(returnValue == null){
                return 'java.lang.Object';
            }else if(Array.isArray(returnValue)){
                for(var i=0,len = returnValue.length;i<len;i++){
                    var node = returnValue[i];
                    if(node instanceof AST.Return){
                        return node.getJavaType(env);
                    }
                }
                return 'java.lang.Object';
            }else{
                returnValue.getJavaType(env);
            }
        }
    },
    matchTypes: function (parameters, args) {
        if (parameters.length == args.length) {
            return true;
        }
    },
    MethodCall: function (node, args) {
        this.node = node;
        this.args = args;
        this.getJavaType = function (env) {
            var target = node.node.getJavaType(env);
            var targetMethod = node.member;
            target = Parser.scriptClass[target];
            var methods = target && target.methods;
            if (methods) {
                for (var i = 0, len = methods.length; i < len; i++) {
                    var m = methods[i];
                    if (m.name == targetMethod && AST.matchTypes(m.parameters, args)) {
                        return Parser.getWrapperClass(m.returnType);
                    }
                }
            }
            target = Parser.extensions[target];
            var methods = target && target.methods;
            if (methods) {
                for (var i = 0, len = methods.length; i < len; i++) {
                    var m = methods[i];
                    if (m.name == targetMethod && AST.matchTypes(m.parameters, args)) {
                        return Parser.getWrapperClass(m.returnType);
                    }
                }
            }
            return 'java.lang.Object';
        }
    },
    FunctionCall: function (result) {
        this.getJavaType = function(env){
            return result.getJavaType(env);
        }
    },
    MemberAccess: function (node, member) {
        this.node = node;
        this.member = member.getText();
        this.getJavaType = function (env, args) {
            var target = node.getJavaType(env);
            target = Parser.scriptClass[target];
            if(target.superClass == 'java.util.HashMap'){
                var methods = target && target.methods;
                if (methods) {
                    for (var i = 0, len = methods.length; i < len; i++) {
                        var method = methods[i];
                        if (method.name == 'get' && method.parameters.length == 1) {
                            return Parser.getWrapperClass(method.returnType);
                        }
                    }
                }
            }
            return 'java.lang.Object';
        }
    },
    MapOrArrayAccess: function () {

    },
    IfStatement: function () {

    },
    VariableAccess: function (span) {
        this.name = span.getText();
        this.getJavaType = function (env) {
            return (env && env[span.getText()]) || 'java.lang.String'
        }
    }
}
require(['vs/editor/editor.main'], function() {
    monaco.languages.register({ id :'magicscript'});
    monaco.languages.setLanguageConfiguration('magicscript',{
        wordPattern: /(-?\d*\.\d\w*)|([^\`\~\!\#\%\^\&\*\(\)\-\=\+\[\{\]\}\\\|\;\:\'\"\,\.\<\>\/\?\s]+)/g,
        brackets: [
            ['{', '}'],
            ['[', ']'],
            ['(', ')'],
        ],
        comments: {
            lineComment: '//',
            blockComment: ['/*', '*/'],
        },
        operators: ['<=', '>=', '==', '!=', '+', '-','*', '/', '%', '&','|', '!', '&&', '||', '?', ':', ],
        autoClosingPairs: [
            { open: '{', close: '}' },
            { open: '[', close: ']' },
            { open: '(', close: ')' },
            {open: '"""', close: '"""', notIn: ['string.multi']},
            { open: '"', close: '"', notIn: ['string'] },
            { open: '\'', close: '\'', notIn: ['string'] },
        ],
    })
    var defaultSuggestions = [{
        label: 'if',
        kind: monaco.languages.CompletionItemKind.Snippet,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        insertText: [
            'if (${1:condition}) {',
            '\t$0',
            '}'
        ].join('\n')
    }, {
        label: 'ret',
        detail : 'return',
        kind: monaco.languages.CompletionItemKind.Snippet,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        insertText: 'return $1;'
    }, {
        label: 'ife',
        detail : 'if else',
        kind: monaco.languages.CompletionItemKind.Snippet,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        insertText: [
            'if (${1:condition}) {',
            '\t$2',
            '} else {',
            '\t$3',
            '}'
        ].join('\n')
    }, {
        label: 'for',
        kind: monaco.languages.CompletionItemKind.Snippet,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        insertText: [
            'for (${1:item} in ${2:range(${3:0},${4:100})}}) {',
            '\t$0',
            '}'
        ].join('\n')
    }, {
        label: 'br',
        detail : 'break',
        kind: monaco.languages.CompletionItemKind.Snippet,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        insertText: 'break;'
    }, {
        label: 'co',
        detail : 'continue',
        kind: monaco.languages.CompletionItemKind.Snippet,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        insertText: 'continue;'
    }, {
        label: 'try',
        detail : 'try catch',
        kind: monaco.languages.CompletionItemKind.Snippet,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        insertText: [
            'try {',
            '\t$1',
            '} catch(e) {',
            '\t$2',
            '}'
        ].join('\n')
    }, {
        label: 'tryf',
        detail : 'try catch finally',
        kind: monaco.languages.CompletionItemKind.Snippet,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        insertText: [
            'try {',
            '\t$1',
            '} catch(e) {',
            '\t$2',
            '} finally {',
            '\t$3',
            '}'
        ].join('\n')
    }, {
        label: 'cat',
        detail : 'catch',
        kind: monaco.languages.CompletionItemKind.Snippet,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        insertText: [
            'catch(e) {',
            '\t$1',
            '}'
        ].join('\n')
    }, {
        label: 'fin',
        detail : 'finally',
        kind: monaco.languages.CompletionItemKind.Snippet,
        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
        insertText: [
            'finally {',
            '\t$1',
            '}'
        ].join('\n')
    }];
    var findAttributes = function(className){
        var target = Parser.scriptClass[className];
        var attributes = [];
        if(target){
            attributes = target.attributes;
            if(target.superClass){
                attributes = attributes.concat(findAttributes(target.superClass));
            }
            if(target.interfaces && target.interfaces.length > 0){
                for(var i=0,len = target.interfaces.length;i < len;i++){
                    attributes = attributes.concat(findAttributes(target.interfaces[i]));
                }
            }
        }
        return attributes;
    }
    var findMethods = function(className){
        var target = Parser.scriptClass[className];
        var methods = [];
        var _findMethod = function(target,begin){
            for (var i = 0, len = target.methods.length; i < len; i++) {
                var method = target.methods[i];
                method.insertText = method.name;
                if (method.parameters.length > begin) {
                    var params = [];
                    var params1 = [];
                    var params2 = [];
                    for (var j = begin; j < method.parameters.length; j++) {
                        params.push('${' + (j + 1 - begin) + ':' + method.parameters[j].name + '}');
                        params1.push(method.parameters[j].name);
                        params2.push(Parser.getSimpleClass(method.parameters[j].type) + " " + method.parameters[j].name);
                    }
                    if (!method.comment) {
                        method.comment = Parser.getSimpleClass(method.returnType) + ':' + method.name + '(' + params1.join(',') + ')';
                    }
                    method.fullName = method.name + '(' + params2.join(', ') + ')';
                    method.insertText += '(' + params.join(',') + ')';
                } else {
                    method.insertText += '()';
                    method.fullName = method.name + '()';
                    if (!method.comment) {
                        method.comment = Parser.getSimpleClass(method.returnType) + ':' + method.name + '()';
                    }
                }
                methods.push(method);
            }
        }
        if(target){
            _findMethod(target,0);
            if(target.superClass){
                methods = methods.concat(findMethods(target.superClass));
            }
            if(target.interfaces && target.interfaces.length > 0){
                for(var i=0,len = target.interfaces.length;i < len;i++){
                    methods = methods.concat(findMethods(target.interfaces[i]));
                }
            }
        }
        target = Parser.extensions[className];
        if(target){
            _findMethod(target,1);
        }
        return methods;
    }
    monaco.languages.registerCompletionItemProvider('magicscript',{
        provideCompletionItems: function (model, position) {
            var value = model.getValueInRange({
                startLineNumber: 1,
                startColumn: 1,
                endLineNumber: position.lineNumber,
                endColumn: position.column
            });
            var line =  model.getValueInRange({
                startLineNumber: position.lineNumber,
                startColumn: 1,
                endLineNumber: position.lineNumber,
                endColumn: position.column
            });
            var suggestions = [];
            var imporIndex = 0;
            if(line.length > 1 && (imporIndex = $.trim(line).indexOf('import')) == 0){
                var keyword = $.trim($.trim(line).substring(imporIndex + 6)).replace(/['|"]/g,'').toLowerCase();
                var len = 0;
                if(keyword && (len = Parser.importClass.length) > 0){
                    var start = line.indexOf('"') + 1;
                    if(start == 0){
                        start = line.indexOf("'") + 1;
                    }
                    var set = new Set();
                    for(var i =0;i < len;i++){
                        var clazz = Parser.importClass[i];
                        var index = clazz.toLowerCase().indexOf(keyword);
                        if(index > -1){
                            if((index = clazz.indexOf('.',index + keyword.length)) > -1){
                                var content = clazz.substring(0,index);
                                content = content.substring(content.lastIndexOf('.') + 1) + '.';
                                if(set.has(content)){
                                    continue;
                                }
                                set.add(content);
                                suggestions.push({
                                    sortText: '1',
                                    label: content,
                                    kind: monaco.languages.CompletionItemKind.Folder,
                                    filterText : clazz,
                                    detail: content,
                                    insertText: content,
                                    insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                                    command : {
                                        id : 'editor.action.triggerSuggest'
                                    }
                                    //range : new monaco.Range(position.lineNumber, start + 1, position.lineNumber, position.column)
                                })
                            }else{
                                suggestions.push({
                                    sortText: '2',
                                    label: clazz.substring(clazz.lastIndexOf('.') + 1),
                                    kind: monaco.languages.CompletionItemKind.Module,
                                    filterText : clazz,
                                    detail: clazz,
                                    insertText: clazz,
                                    //insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
                                    range : new monaco.Range(position.lineNumber, start + 1, position.lineNumber, position.column)
                                })
                            }
                        }
                    }
                }
            }else if (value.length > 1) {
                var endDot = value.charAt(value.length - 1) == '.';
                var input = ''
                if(endDot){
                    input = value.substring(0, value.length - 1);
                }else if(value.indexOf('.') > 1){
                    input = value.substring(0,value.lastIndexOf('.'));
                }
                try{
                    var className = Parser.parse(new TokenStream(Parser.tokenize(input)));
                    if (className) {
                        var attributes = findAttributes(className);
                        if (attributes) {
                            for (var j = 0; j < attributes.length; j++) {
                                var attribute = attributes[j];
                                suggestions.push({
                                    label: attribute.name,
                                    kind: monaco.languages.CompletionItemKind.Field,
                                    detail: attribute.type + ":" + attribute.name,
                                    insertText: attribute.name,
                                    sortText: ' ~~' + attribute.name
                                })
                            }
                            var methods = findMethods(className);
                            var mmap = {};
                            for (var j = 0; j < methods.length; j++) {
                                var method = methods[j];
                                if(mmap[method.fullName]){
                                    continue;
                                }
                                mmap[method.fullName] = true;
                                suggestions.push({
                                    sortText: method.sortText || method.fullName,
                                    label: method.fullName,
                                    kind: monaco.languages.CompletionItemKind.Method,
                                    detail: method.comment,
                                    insertText: method.insertText,
                                    insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
                                })
                            }
                        }
                    }
                }catch (e) {
                }
            } else {
                suggestions = defaultSuggestions;
            }
            return {
                suggestions : suggestions
            }
        },
        triggerCharacters: ['.']
    })
    monaco.languages.setMonarchTokensProvider('magicscript',{
        escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,
        keywords : ['new','var','if','else','for','in','return','import','break','continue','as','null','true','false','try','catch','finally'],
        digits: /\d+(_+\d+)*/,
        tokenizer : {
            root : [
                [/[a-zA-Z_$][\w$]*/,{
                    cases :{
                        "@keywords" : { token : "keywords" },
                        "@default" : "identifier"
                    }
                }],
                [/[{}()\[\]]/, '@brackets'],
                [/(@digits)[lLbBsSdDfFmM]?/, 'number'],
                [/\/\*\*(?!\/)/, 'comment.doc', '@mulcomment'],
                [/\/\*/, 'comment', '@comment'],
                [/\/\/.*$/, 'comment'],
                [/[;,.]/, 'delimiter'],
                [/"([^"\\]|\\.)*$/, 'string.invalid'],
                [/'([^'\\]|\\.)*$/, 'string.invalid'],
                [/""/, 'string.multi', '@string_multi'],
                [/"/, 'string', '@string_double'],
                [/'/, 'string', '@string_single'],
            ],
            comment: [
                [/[^\/*]+/, 'comment'],
                // [/\/\*/, 'comment', '@push' ],    // nested comment not allowed :-(
                // [/\/\*/,    'comment.invalid' ],    // this breaks block comments in the shape of /* //*/
                [/\*\//, 'comment', '@pop'],
                [/[\/*]/, 'comment']
            ],
            mulcomment: [
                [/[^\/*]+/, 'comment.doc'],
                // [/\/\*/, 'comment.doc', '@push' ],    // nested comment not allowed :-(
                [/\/\*/, 'comment.doc.invalid'],
                [/\*\//, 'comment.doc', '@pop'],
                [/[\/*]/, 'comment.doc']
            ],
            string_multi: [
                [/"""/, {token: 'string', next: '@pop'}],
                [/"/, {token: 'string', next: '@string_multi_embedded', nextEmbedded: 'sql'}]
            ],
            string_multi_embedded: [
                [/"""/, {token: '@rematch', next: '@pop', nextEmbedded: '@pop'}],
                [/[^"""]+/, '']
            ],
            string_double: [
                [/[^\\"]+/, 'string'],
                [/@escapes/, 'string.escape'],
                [/\\./, 'string.escape.invalid'],
                [/"/, 'string', '@pop']
            ],
            string_single: [
                [/[^\\']+/, 'string'],
                [/@escapes/, 'string.escape'],
                [/\\./, 'string.escape.invalid'],
                [/'/, 'string', '@pop']
            ],
        }
    })
});