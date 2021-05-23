import tokenizer from '@/scripts/parsing/tokenizer.js'
import {TokenStream, TokenType} from '../parsing/index.js'
import {
    FunctionCall,
    LinqSelect,
    MapOrArrayAccess,
    MemberAccess,
    Node,
    VarDefine,
    VariableAccess
} from '../parsing/ast.js'
import {keywords, Parser} from '@/scripts/parsing/parser.js'
import {Range} from 'monaco-editor'
import JavaClass from "./java-class"
import RequestParameter from './request-parameter.js';

const findBestMatch = (node, row, col) => {
    let expressions = node.expressions();
    for (let index in expressions) {
        let expr = expressions[index];
        if (expr instanceof FunctionCall && expr.target instanceof VariableAccess && expr.getSpan().inPosition(row, col)) {
            return expr;
        }
        let v = findBestMatch(expr, row, col)
        if (v instanceof Node) {
            return v;
        }
    }
    if (node.getSpan().inPosition(row, col)) {
        return node;
    }
    return null;
}
const generateMethodDocument = (method, contents) => {
    contents.push({value: `${method.fullName}`})
    contents.push({value: `${method.comment}`})
    method.parameters.forEach((param, pIndex) => {
        if (pIndex > 0 || !method.extension) {
            contents.push({value: `${param.name}：${(param.comment || param.type)}`})
        }
    })
    contents.push({value: `返回值类型：${method.returnType}`})
}
const HoverProvider = {
    provideHover: async (model, position) => {
        let value = model.getValue()
        let tokens = tokenizer(value);
        let row = position.lineNumber;
        let col = position.column;
        for (let index in tokens) {
            let token = tokens[index];
            if (token.getTokenType() === TokenType.Identifier && token.getSpan().inPosition(row, col) && keywords.indexOf(token.getText()) > -1) {
                let line = token.getSpan().getLine();
                return {
                    range: new Range(line.lineNumber, line.startCol, line.endLineNumber, line.endCol + 1),
                    contents: [{
                        value: `关键字 **${token.getText()}**`
                    }]
                };
            }
        }
        let tokenStream = new TokenStream(tokens);
        let parser = new Parser(tokenStream)
        let nodes = parser.parse(true);
        tokenStream.resetIndex(0)
        parser.linqLevel = 0;
        for (let index in nodes) {
            let node = nodes[index];
            if (node.getSpan().inPosition(row, col)) {
                let best = findBestMatch(node, row, col);
                let env = await parser.preprocessComplection(false,RequestParameter.environmentFunction() || {});
                let contents = [];
                let line = best.getSpan().getLine();
                if (best instanceof VarDefine) {
                    let value = env[best.getVarName()];
                    contents.push({value: `变量：${best.getVarName()}`})
                    contents.push({value: `类型：${value}`})
                } else if (best instanceof VariableAccess) {
                    let value = env[best.getVariable()];
                    contents.push({value: `访问变量：${best.getVariable()}`})
                    contents.push({value: `类型：${value || 'unknow'}`})
                } else if (best instanceof MemberAccess) {
                    let javaType = await best.getTarget().getJavaType(env);
                    let clazz = await JavaClass.loadClass(javaType);
                    let methods = JavaClass.findMethods(clazz);
                    for (let m in methods) {
                        let method = methods[m];
                        if (method.name === best.member.getText()) {
                            generateMethodDocument(method, contents);
                        }
                    }
                } else if (best instanceof FunctionCall) {
                    let target = best.target;
                    let functions = JavaClass.findFunction().filter(method => method.name === target.variable);
                    if (functions.length > 0) {
                        generateMethodDocument(functions[0], contents);
                    } else {
                        let value = env[target.variable];
                        if (value && value.indexOf('@') === 0) {
                            var functionName = value.substring(1);
                            let func = JavaClass.getOnlineFunction(functionName);
                            if (func) {
                                let parameters = Array.isArray(func.parameter) ? func.parameter : JSON.parse(func.parameter || '[]');
                                parameters.forEach(it => it.comment = it.description);
                                generateMethodDocument({
                                    fullName: target.variable + " " + func.name,
                                    comment: func.description || '',
                                    parameters,
                                    returnType: func.returnType
                                }, contents);
                            }

                        } else {
                            contents.push({value: `访问变量：${target.variable}`})
                            contents.push({value: `类型：${value || 'unknow'}`})
                        }
                    }
                } else if (best instanceof MapOrArrayAccess) {
                    contents.push({value: `访问Map或数组`})
                } else if (best instanceof LinqSelect) {
                    contents.push({value: `linq查询`})
                } else {
                    return;
                }
                return {
                    range: new Range(line.lineNumber, line.startCol, line.endLineNumber, line.endCol + 1),
                    contents
                };
            }
        }
    }
}
export default HoverProvider