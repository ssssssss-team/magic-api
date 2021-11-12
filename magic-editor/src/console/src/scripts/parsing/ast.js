import JavaClass from '../editor/java-class.js'
import {Span, TokenType} from './index.js'

class Node {
    constructor(span) {
        this.span = span;
    }

    getSpan() {
        return this.span;
    }

    async getJavaType(env) {
        await this.getExpressionsJavaType(env);
        return 'java.lang.Object';
    }

    async getExpressionsJavaType(env){
        for (const expr of this.expressions().filter(it => it)) {
            await expr.getJavaType(env);
        }
    }

    expressions() {
        return []
    }

    toString() {
        return this.span.getText();
    }
}

class Expression extends Node {
    constructor(span) {
        super(span);
    }
}

class Literal extends Expression {
    constructor(span, javaType, expressionList) {
        super(span);
        this.javaType = javaType;
        this.expressionList = expressionList || []
    }

    expressions() {
        return this.expressionList
    }

    async getJavaType() {
        return this.javaType;
    }

    getValue(){
        const text = this.getSpan().getText()
        return text.replace(/\\\\/g, '\\').replace(/\\n/g, '\n').replace(/\\r/g, '\r').replace(/\\t/g, '\t').replace(/\\"/g, '"').replace(/\\'/g, "\'")
    }

}

class MethodCall extends Node {
    constructor(span, target, args) {
        super(span)
        this.target = target
        this.args = args
    }

    expressions() {
        return [this.target, ...this.args]
    }

    getMethod(){
        return this.target;
    }

    getArguments(){
        return this.args;
    }

    async getJavaType(env) {
        let method = this.target.member.getText()
        let targetType = await this.target.getJavaType(env)
        let methods = JavaClass.findMethods(targetType);
        if (methods) {
            for (let i = 0, len = methods.length; i < len; i++) {
                let m = methods[i];
                if (m.name === method && JavaClass.matchTypes(m.parameters, this.args, m.extension)) {
                    return m.origin ? targetType : JavaClass.getWrapperClass(m.returnType);
                }
            }
        }
        return 'java.lang.Object';
    }

}

class FunctionCall extends Node {
    constructor(span, target, args) {
        super(span)
        this.target = target;
        this.args = args
    }

    expressions() {
        return [this.target, ...this.args]
    }

    getFunction(){
        return this.target;
    }

    getArguments(){
        return this.args;
    }

    async getJavaType(env) {
        return await this.target.getJavaType(env);
    }
}

class MemberAccess extends Node {
    constructor(span, target, optional, member, whole) {
        super(span)
        this.target = target;
        this.optional = optional;
        this.member = member
        this.whole = whole;
    }

    isWhole() {
        return this.whole === true
    }

    expressions() {
        return [this.target]
    }

    getTarget() {
        return this.target;
    }

    async getJavaType(env) {
        let javaType = await this.target.getJavaType(env);

        let clazz = await JavaClass.loadClass(javaType);
        let methods = clazz.methods;
        if (methods) {
            for (let i = 0, len = methods.length; i < len; i++) {
                let method = methods[i];
                if (clazz.superClass === 'java.util.HashMap' && method.name === 'get' && method.parameters.length === 1) {
                    return JavaClass.getWrapperClass(method.returnType);
                }
            }
        }
        return javaType || 'java.lang.Object';
    }
}

class VariableAccess extends Node {
    constructor(span, variable) {
        super(span)
        this.variable = variable
    }

    getVariable() {
        return this.variable;
    }

    async getJavaType(env) {
        // @import
        let value = (env && env[this.variable]);
        if(!value){
            let imports = env['@import']
            for(let i = imports.length - 1; i >= 0 && !value; i--){
                value = JavaClass.findClass(imports[i] + this.variable);
            }
        }
        return  value|| 'java.lang.Object';
    }
}

class MapOrArrayAccess extends Node {
    constructor(span, target, keyOrIndex) {
        super(span)
        this.target = target
        this.keyOrIndex = keyOrIndex
    }

    async getJavaType(env) {
        const javaType = await this.target.getJavaType(env)
        return javaType === 'db' ? 'db' : super.getJavaType(env);
    }
}

class IfStatement extends Node {
    constructor(span, condition, trueBlock, elseIfs, falseBlock) {
        super(span)
        this.condition = condition
        this.trueBlock = trueBlock || []
        this.elseIfs = elseIfs || []
        this.falseBlock = falseBlock || []
    }

    expressions() {
        return [this.condition, ...this.trueBlock, ...this.elseIfs, ...this.falseBlock]
    }
}

class WholeLiteral extends Literal {
    constructor(span) {
        super(span)
    }
}

class LambdaFunction extends Node {
    constructor(span, parameters, childNodes) {
        super(span)
        this.parameters = parameters
        this.childNodes = childNodes
    }

    expressions() {
        return [...this.childNodes]
    }

    async getJavaType(env) {
        if (Array.isArray(this.childNodes) && this.childNodes.length > 0) {
            for (let i = 0, len = this.childNodes.length; i < len; i++) {
                let node = this.childNodes[i];
                if (node instanceof Return) {
                    return await node.getJavaType(env);
                }
            }
            return await this.childNodes[this.childNodes.length - 1].getJavaType(env);
        }
        return await super.getJavaType(env)
    }
}

class Return extends Node {
    constructor(span, returnValue) {
        super(span)
        this.returnValue = returnValue
    }

    expressions() {
        return [this.returnValue]
    }

    async getJavaType(env) {
        return this.returnValue == null ? '' : await this.returnValue.getJavaType(env);
    }
}

class Continue extends Node {
    constructor(span) {
        super(span)
    }
}

class Break extends Node {
    constructor(span) {
        super(span)
    }
}

class Exit extends Node {
    constructor(span, values) {
        super(span)
        this.values = values
    }

    expressions() {
        return this.values
    }
}

class Throw extends Node {
    constructor(span, value) {
        super(span)
        this.value = value
    }

    expressions() {
        return [this.value]
    }
}

class Assert extends Node {
    constructor(span, condition, values) {
        super(span)
        this.condition = condition
        this.values = values
    }

    expressions() {
        return [this.condition, ...this.values]
    }
}

class NewStatement extends Node {
    constructor(span, identifier, parameters) {
        super(span)
        this.identifier = identifier
        this.parameters = parameters
    }

    expressions() {
        return [...this.parameters]
    }

    async getJavaType(env) {
        let value = env[this.identifier];
        if(!value){
            let imports = env['@import']
            for(let i = imports.length - 1; i >= 0 && !value; i--){
                value = JavaClass.findClass(imports[i] + this.identifier);
            }
        }
        return  value|| 'java.lang.Object';
    }
}

class AsyncCall extends Node {
    constructor(span, expression) {
        super(span)
        this.expression = expression
    }

    expressions() {
        return [this.expression]
    }

    async getJavaType(env) {
        return 'java.util.concurrent.Future';
    }
}

class UnaryOperation extends Node {
    constructor(operator, operand, atAfter) {
        super(new Span(operator.getSpan(), operand.getSpan()))
        this.operand = operand
        this.operator = operator
        this.atAfter = atAfter
    }

    async getJavaType(env) {
        return await this.operand.getJavaType(env);
    }

}

class TryStatement extends Node {
    constructor(span, exceptionVarNode, tryBlock, catchBlock, finallyBlock) {
        super(span)
        this.exceptionVarNode = exceptionVarNode;
        this.tryBlock = tryBlock;
        this.catchBlock = catchBlock;
        this.finallyBlock = finallyBlock;
    }

    expressions() {
        return [...this.tryBlock, ...this.catchBlock, ...this.finallyBlock]
    }
}

class ForStatement extends Node {
    constructor(span, indexOrKey, value, mapOrArray, body) {
        super(span)
        this.indexOrKey = indexOrKey;
        this.value = value;
        this.mapOrArray = mapOrArray;
        this.body = body;
    }

    expressions() {
        return [this.mapOrArray, ...this.body]
    }

}

class WhileStatement extends Node {
    constructor(span, condition, trueBlock) {
        super(span)
        this.condition = condition;
        this.trueBlock = trueBlock;
    }

    expressions() {
        return [this.condition, ...this.trueBlock]
    }

}

class Import extends Node {
    constructor(span, packageName, varName, module) {
        super(span)
        this.packageName = packageName;
        this.varName = varName;
        this.module = module;
    }

    async getJavaType(env){
        if(this.packageName.endsWith('.*')) {
            env['@import'].push(this.packageName.substring(0, this.packageName.length - 1))
        }else if(this.module){
            env[this.packageName] = this.packageName
        }else if(this.varName){
            env[this.varName] = this.packageName
        }else {
            let index = this.packageName.lastIndexOf('.');
            if (index > -1) {
                env[this.packageName.substring(index + 1)] = this.packageName
            }
        }
    }

}

class VarDefine extends Node {
    constructor(span, varName, expression) {
        super(span)
        this.varName = varName;
        this.expression = expression;
    }

    getVarName() {
        return this.varName;
    }

    expressions() {
        return this.expression == null ? [] : [this.expression]
    }

    async getJavaType(env) {
        let type = await this.expression.getJavaType(env);
        env[this.varName] = type
        return type
    }
}

class TernaryOperation extends Node {
    constructor(condition, trueExpression, falseExpression) {
        super(new Span(condition.getSpan(), falseExpression.getSpan()));
        this.condition = condition;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }

    expressions() {
        return [this.condition, this.trueExpression, this.falseExpression]
    }
}

class Spread extends Node {
    constructor(span, target) {
        super(span)
        this.target = target;
    }

    expressions() {
        return [this.target]
    }
}

class MapLiteral extends Literal {
    constructor(span, keys, values) {
        super(span, 'java.util.LinkedHashMap')
        this.keys = keys;
        this.values = values;
    }

    expressions() {
        return this.values
    }
}

class ListLiteral extends Literal {
    constructor(span, values) {
        super(span, 'java.util.ArrayList')
        this.values = values;
    }

    expressions() {
        return this.values
    }
}

class LanguageExpression extends Node{
    constructor(span) {
        super(span);
    }

    async getJavaType() {
        return 'java.util.function.Function';
    }
    expressions() {
        return []
    }
}
class BinaryOperation extends Node {
    constructor(left, operator, right, linqLevel) {
        super(new Span(left.getSpan(), right.getSpan()));
        this.left = left;
        this.right = right;
        this.operator = operator;
        this.linqLevel = linqLevel;
    }

    getOperator(){
        return this.operator;
    }
    setRightOperand(right){
        this.right = right;
    }
    getRightOperand() {

        return this.right
    }

    expressions() {
        return [this.left, this.right]
    }

    async getJavaType(env) {
        let lType = await this.left.getJavaType(env);
        let rType = await this.right.getJavaType(env);
        lType = lType.toLowerCase().substring(lType.lastIndexOf(".") + 1)
        rType = rType.toLowerCase().substring(rType.lastIndexOf(".") + 1)
        if (this.operator.type === TokenType.Plus || this.operator.type === TokenType.PlusEqual) {
            if (lType === 'string' || rType === 'string') {
                return 'java.lang.String';
            }
        }
        if (this.operator.type === TokenType.Equal || (this.operator.type === TokenType.Assignment && this.linqLevel > 0)) {
            return 'java.lang.Boolean';
        }
        if (lType === 'bigdecimal' || rType === 'bigdecimal') {
            return 'java.math.BigDecimal';
        }
        if (lType === 'double' || rType === 'double') {
            return 'java.lang.Double';
        }
        if (lType === 'float' || rType === 'float') {
            return 'java.lang.Float';
        }
        if (lType === 'long' || rType === 'long') {
            return 'java.lang.Long';
        }
        if (lType === 'integer' || rType === 'integer') {
            return 'java.lang.Integer';
        }
        if (lType === 'short' || rType === 'short') {
            return 'java.lang.Short';
        }
        if (lType === 'byte' || rType === 'byte') {
            return 'java.lang.Byte';
        }
        return 'java.lang.Object';
    }
}

class LinqField extends Expression {
    constructor(span, expression, alias) {
        super(span)
        this.expression = expression;
        this.alias = alias;
    }

    expressions() {
        return [this.expression];
    }
}

class LinqJoin extends Expression {
    constructor(span, leftJoin, target, condition) {
        super(span)
        this.leftJoin = leftJoin;
        this.target = target;
        this.condition = condition;
    }

    expressions() {
        return [this.target, this.condition];
    }
}

class LinqOrder extends Expression {
    constructor(span, expression, alias, order) {
        super(span)
        this.expression = expression;
        this.alias = alias;
        this.order = order;
    }

    expressions() {
        return [this.expression];
    }
}

class ClassConverter extends Expression {
    constructor(span, convert, target, args) {
        super(span);
        this.convert = convert;
        this.target = target;
        this.args = args;
    }

    expressions() {
        return [this.target, ...this.args];
    }

    async getJavaType() {
        if (this.convert == 'double') {
            return 'java.lang.Double';
        }
        if (this.convert == 'float') {
            return 'java.lang.Float';
        }
        if (this.convert == 'long') {
            return 'java.lang.Long';
        }
        if (this.convert == 'int') {
            return 'java.lang.Integer';
        }
        if (this.convert == 'short') {
            return 'java.lang.Short';
        }
        if (this.convert == 'byte') {
            return 'java.lang.Byte';
        }
        if (this.convert == 'date') {
            return 'java.util.Date';
        }
        return 'java.lang.Object';
    }
}

class LinqSelect extends Expression {
    constructor(span, fields, from, joins, where, groups, having, orders, limit, offset) {
        super(span)
        this.fields = fields;
        this.from = from;
        this.joins = joins;
        this.where = where;
        this.groups = groups;
        this.having = having;
        this.orders = orders;
        this.limit = limit;
        this.offset = offset;
    }

    expressions() {
        let temp = [];
        if (this.where) {
            temp.push(this.where)
        }
        if (this.having) {
            temp.push(this.having)
        }
        return [...this.fields, this.from, ...this.joins, ...this.groups, ...temp, ...this.orders, this.limit, this.offset];
    }

    async getJavaType() {
        return 'java.util.List';
    }
}


export {
    Node,
    Expression,
    Literal,
    Assert,
    MethodCall,
    FunctionCall,
    MemberAccess,
    VariableAccess,
    MapOrArrayAccess,
    IfStatement,
    LambdaFunction,
    Return,
    Continue,
    Break,
    NewStatement,
    AsyncCall,
    UnaryOperation,
    TryStatement,
    ForStatement,
    WhileStatement,
    Import,
    VarDefine,
    TernaryOperation,
    BinaryOperation,
    Spread,
    MapLiteral,
    ListLiteral,
    Exit,
    LinqField,
    LinqJoin,
    LinqOrder,
    LinqSelect,
    WholeLiteral,
    ClassConverter,
    LanguageExpression,
    Throw
}