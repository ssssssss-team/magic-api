import tokenizer from '@/scripts/parsing/tokenizer.js'
import { TokenStream } from '../parsing/index.js'
import {
    ClassConverter,
    FunctionCall,
    LinqSelect,
    MapOrArrayAccess,
    MemberAccess, NewStatement,
    Node,
    VarDefine,
    VariableAccess
} from '../parsing/ast.js'
import { Parser } from '@/scripts/parsing/parser.js'
import { Range } from 'monaco-editor'
import JavaClass from "./java-class"

const findBestMatch = (node, row, col) => {
    let expressions = node.expressions().filter(it => it);
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
const generateMethodDocument = (prefix,method, contents) => {
    contents.push({value: `${prefix}${method.fullName}`})
    contents.push({value: `${method.comment}`})
    method.parameters.forEach((param, pIndex) => {
        if (pIndex > 0 || !method.extension) {
            contents.push({value: `${param.name}：${(param.comment || param.type)}`})
        }
    })
    contents.push({value: `返回类型：\`${method.returnType}\``})
}

const generateFunctionCall = (methodName, env, contents, isNew)=>{
    let functions = JavaClass.findFunction().filter(method => method.name === methodName);
    if (functions.length > 0) {
        generateMethodDocument('', functions[0], contents);
    } else {
        let value = env[methodName];
        if (value && value.indexOf('@') === 0) {
            let functionName = value.substring(1);
            let func = JavaClass.getOnlineFunction(functionName);
            if (func) {
                let parameters = Array.isArray(func.parameter) ? func.parameter : JSON.parse(func.parameter || '[]');
                parameters.forEach(it => it.comment = it.description);
                generateMethodDocument('', {
                    fullName: methodName + " " + func.name,
                    comment: func.description || '',
                    parameters,
                    returnType: func.returnType
                }, contents);
            }

        } else {
            contents.push({value: `${isNew ? '创建对象' : '访问变量'}：${methodName}`})
            contents.push({value: `类型：${value || 'unknow'}`})
        }
    }
}
const HoverProvider = {
    provideHover: async (model, position) => {
        let value = model.getValue()
        let tokens = tokenizer(value);
        let tokenStream = new TokenStream(tokens);
        let parser = new Parser(tokenStream)
        let nodes = parser.parse(true);
        let input = model.getValueInRange({
            startLineNumber: 1,
            startColumn: 1,
            endLineNumber: position.lineNumber,
            endColumn: position.column
        });
        let index = input.length;
        for (let i = 0, len = nodes.length; i < len; i++) {
            let best = parser.findBestMatch(nodes[i], index)
            if(best){
                let env = await parser.processEnv(nodes)
                let contents = [];
                let line = best.getSpan().getLine();
                if (best instanceof VarDefine) {
                    let value = env[best.getVarName()];
                    contents.push({value: `定义变量：${best.getVarName()}`})
                    contents.push({value: `变量类型：${value}`})
                } else if (best instanceof ClassConverter) {
                    if(best.convert === 'json'){
                        contents.push({value: '强制转换为`JSON`类型'})
                    }else if(best.convert === 'stringify'){
                        contents.push({value: '转换为`JSON`字符串'})
                    }else if(best.convert === 'sql'){
                        let args = best.args || []
                        contents.push({value: `等同于\`new SqlParameterValue(java.sql.Types.${args[0]?.span?.getText()?.toUpperCase()},${best.target.getSpan().getText()})\``})
                    }else{
                        contents.push({value: `转换为\`${best.convert}\``})
                    }
                } else if (best instanceof VariableAccess) {
                    let value = env[best.getVariable()];
                    if(value){
                        contents.push({value: `访问变量：${best.getVariable()}`})
                        contents.push({value: `变量类型：${value || 'unknow'}`})
                    }else{
                        generateFunctionCall(best.getVariable(), env, contents)
                    }
                } else if (best instanceof MemberAccess) {
                    let javaType = await best.getTarget().getJavaType(env);
                    let clazz = await JavaClass.loadClass(javaType);
                    let memberName = best.member.getText()
                    JavaClass.findMethods(clazz).filter(method => method.name === memberName)
                        .forEach(method => generateMethodDocument(`${JavaClass.getSimpleClass(javaType)}.`,method, contents))
                    JavaClass.findEnums(clazz).filter(it => it === memberName).forEach(it => {
                        contents.push({value: `访问枚举：\`${javaType}.${memberName}\``})
                    })
                    JavaClass.findAttributes(clazz).filter(attr => attr.name === memberName).forEach(it => {
                        contents.push({value: `访问属性：\`${javaType}.${memberName}\``})
                        it.comment && contents.push({value: `${it.comment}`})
                        contents.push({value: `属性类型：` + `\`${it.type}\``})
                    })
                    line = best.member.getLine();
                } else if (best instanceof FunctionCall) {
                    let target = best.target;
                    generateFunctionCall(target.variable, env, contents)
                } else if (best instanceof NewStatement) {
                    let target = best.identifier;
                    if(target instanceof VariableAccess){
                        generateFunctionCall(target, env, contents, true)
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