import JavaClass from './java-class.js'
import tokenizer from '../parsing/tokenizer.js'
import {TokenStream} from '../parsing/index.js'
import {Parser} from '../parsing/parser.js'
import * as monaco from 'monaco-editor'
import RequestParameter from "@/scripts/editor/request-parameter";
import {MemberAccess, MethodCall, NewStatement, VariableAccess} from "@/scripts/parsing/ast";

const completionImportJavaPackage = (suggestions, keyword, start, position) => {
    let len = -1
    let importClass = JavaClass.getImportClass();
    if (start !== 0 && keyword && (len = importClass.length) > 0) {
        keyword = keyword.toLowerCase()
        JavaClass.getDefineModules().filter(module => module.toLowerCase().indexOf(keyword) > -1).forEach(module => suggestions.push({
            label: module,
            filterText: module,
            kind: monaco.languages.CompletionItemKind.Module,
            detail: module,
            insertText: module,
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
        }))
        let set = new Set();
        for (let i = 0; i < len && suggestions.length < 100; i++) {
            let clazz = importClass[i];
            let index = clazz.toLowerCase().indexOf(keyword);
            if (index > -1) {
                let className = clazz.substring(clazz.lastIndexOf('.') + 1);
                if (index === 0) {
                    let content = clazz.substring(keyword.length);
                    let detail = content
                    if(content.startsWith(".")){
                        detail = keyword + '.'
                        content = keyword.substring(keyword.lastIndexOf(".") + 1) + '.'
                    } else {
                        if(content.indexOf('.') === -1){
                            suggestions.push({
                                sortText: `2${className}`,
                                label: className,
                                kind: monaco.languages.CompletionItemKind.Class,
                                filterText: clazz,
                                detail: clazz,
                                insertText: className,
                                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                            })
                            continue;
                        }
                        let text = content.substring(0, content.indexOf('.') + 1);
                        detail = keyword + text
                        content = keyword.substring(keyword.lastIndexOf(".") + 1) + text;
                    }
                    if (set.has(content)) {
                        continue;
                    }
                    set.add(content);
                    suggestions.push({
                        sortText: `1${content}`,
                        label: content,
                        kind: monaco.languages.CompletionItemKind.Folder,
                        filterText: clazz,
                        detail: detail.replace(/\.$/, ''),
                        insertText: content,
                        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                        command: {
                            id: 'editor.action.triggerSuggest'
                        }
                    })
                } else if (className.toLowerCase().indexOf(keyword) > -1) {
                    suggestions.push({
                        sortText: `2${className}`,
                        label: className,
                        kind: monaco.languages.CompletionItemKind.Class,
                        filterText: className,
                        detail: clazz,
                        insertText: clazz,
                        range: new monaco.Range(position.lineNumber, start + 1, position.lineNumber, position.column)
                    })
                }
            }
        }
    } else {
        JavaClass.getDefineModules().forEach(module => suggestions.push({
            label: module,
            filterText: module,
            kind: monaco.languages.CompletionItemKind.Module,
            detail: module,
            insertText: module,
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
        }))
    }
}

const completionImport = (suggestions, position, line, importIndex) => {
    let start = line.indexOf('"') + 1;
    if (start === 0) {
        start = line.indexOf("'") + 1;
    }
    if(start === 0){
        line = line.trim().replace('import', '').trim()
        completionImportJavaPackage(suggestions, line, importIndex + 1 , position)
        return;
    }
    let text = line.substring(importIndex).trim().replace(/['|"]/g, '');
    if(text.startsWith('@')){
        if(text.indexOf(' ')> -1){
            return;
        }
        let finder = JavaClass.getApiFinder();
        (finder && finder() || []).forEach(it => {
            let label = '@' + it.method + ':' + it.path
            suggestions.push({
                sortText: label,
                label: label,
                kind: monaco.languages.CompletionItemKind.Reference,
                filterText: label,
                detail: it.name,
                insertText: label,
                range: new monaco.Range(position.lineNumber, start + 1, position.lineNumber, position.column)
            })
        })
        finder = JavaClass.getFunctionFinder();
        (finder && finder() || []).forEach(it => {
            let label = '@' + it.path
            suggestions.push({
                sortText: label,
                label: label,
                kind: monaco.languages.CompletionItemKind.Reference,
                filterText: label,
                detail: it.name,
                insertText: label,
                range: new monaco.Range(position.lineNumber, start + 1, position.lineNumber, position.column)
            })
        })
        return;
    }
    completionImportJavaPackage(suggestions, text, start, position)
}
const completionFunction = async (suggestions, input, env, best, isNew) => {
    env = env || {}
    if (best && best instanceof VariableAccess) {
        if(await best.getJavaType(env) === 'java.lang.Object'){
            let importClass = JavaClass.getImportClass();
            const keyword = best.variable
            importClass.forEach(clazz => {
                let className = clazz.substring(clazz.lastIndexOf('.') + 1);
                if(className.indexOf(keyword) > -1){
                    suggestions.push({
                        sortText: `${className}`,
                        label: className,
                        kind: monaco.languages.CompletionItemKind.Class,
                        filterText: className,
                        detail: clazz,
                        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                        command: {
                            id: 'editor.action.scrollUp1Line'
                        },
                        insertText: className + (isNew ? '()' : ''),
                        additionalTextEdits: [{
                            forceMoveMarkers: true,
                            text: `import ${clazz}\r\n`,
                            range: new monaco.Range(1, 0, 1, 0)
                        }]
                    })
                }
            })
        }
    }
    JavaClass.findFunction().forEach(it => {
        suggestions.push({
            sortText: it.sortText || it.fullName,
            label: it.fullName,
            filterText: it.name,
            kind: monaco.languages.CompletionItemKind.Method,
            detail: it.comment,
            insertText: it.insertText,
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
        })
    })
    let known = suggestions.map(it => it.detail);
    let matches = input.match(/[a-zA-Z_$]+/ig) || [];
    let count = matches.length;
    let vars = Object.keys(env);
    vars.forEach(key => {
        suggestions.push({
            label: key,
            filterText: key,
            kind: monaco.languages.CompletionItemKind.Variable,
            detail: env[key],
            insertText: key,
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
        })
    })
    if (count > 2) {
        Array.from(new Set(matches)).filter((it, index) => index + 2 < count && known.indexOf(it) === -1 && vars.indexOf(it) === -1).map(it => {
            suggestions.push({
                label: it,
                filterText: it,
                kind: monaco.languages.CompletionItemKind.Text,
                detail: it,
                insertText: it,
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            })
        })
    }
};

const completionMethod = async (className, suggestions) => {
    let clazz = await JavaClass.loadClass(className)
    let index = className.lastIndexOf('.')
    let simpleName = index > 0 ? className.substring(index  + 1) : className
    let enums = JavaClass.findEnums(clazz);
    if (enums) {
        for (let j = 0; j < enums.length; j++) {
            let value = enums[j];
            suggestions.push({
                label: value,
                kind: monaco.languages.CompletionItemKind.Enum,
                detail: value + ":" + value,
                insertText: value,
                sortText: ' ~~~' + value
            })
        }
    }
    let attributes = JavaClass.findAttributes(clazz);
    if (attributes) {
        for (let j = 0; j < attributes.length; j++) {
            let attribute = attributes[j];
            suggestions.push({
                label: attribute.name,
                kind: monaco.languages.CompletionItemKind.Field,
                detail: attribute.comment || (attribute.type + ":" + attribute.name),
                insertText: attribute.name,
                sortText: ' ~~' + attribute.name
            })
        }
    }
    let methods = JavaClass.findMethods(clazz);
    if (methods) {
        let mmap = {};
        for (let j = 0; j < methods.length; j++) {
            let method = methods[j];
            if (mmap[method.signature]) {
                continue;
            }
            mmap[method.signature] = true;
            let document = [];
            method.comment && document.push(method.comment)
            for (let j = (method.extension ? 1 : 0); j < method.parameters.length; j++) {
                let param = method.parameters[j];
                document.push(`\`${param.name}\`：${(param.comment || param.type)}`)
            }
            document.push(`返回类型：\`${method.returnType}\``)
            suggestions.push({
                sortText: method.sortText || method.fullName,
                label: method.fullName,
                kind: monaco.languages.CompletionItemKind.Method,
                detail: `${simpleName}.${method.fullName}: ${method.returnType}`,
                documentation: { value: document.join('\r\n\r\n\r\n') },
                insertText: method.insertText,
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            })
        }
    }
}

async function completionScript(suggestions, input) {
    try {
        let tokens = tokenizer(input);
        let tokenLen = tokens.length;
        if (tokenLen === 0) {
            await completionFunction(suggestions, input)
            return;
        }
        let parser = new Parser(new TokenStream(tokens));
        const { best, env } = await parser.parseBest(input.length - 1, env);
        if(input.endsWith(".")){
            await completionMethod(await best.getJavaType(env), suggestions)
        } else if(best) {
            if (best instanceof MemberAccess || best instanceof MethodCall ) {
                await completionMethod(await best.target.getJavaType(env), suggestions)
            } else if(best instanceof NewStatement && best.identifier instanceof VariableAccess){
                await completionFunction(suggestions, input, env, best.identifier, true)
            } else {
                await completionFunction(suggestions, input, env, best)
            }
        } else {
           await completionFunction(suggestions, input, env)
        }
        return suggestions;
    } catch (e) {
        // console.error(e)
    }
}

const quickSuggestions = [
    ['bre', 'break;', '跳出循环'],
    ['con', 'continue;', '继续循环'],
    ['imp', 'import $1', '导入'],
    ['if', 'if (${1:condition}){\r\n\t$2\r\n}', '判断'],
    ['ife', 'if (${1:condition}) {\r\n\t$2\r\n} else { \r\n\t$3\r\n}', '判断'],
    ['for', 'for (item in ${1:collection}) {\r\n\t$2\r\n}', '循环集合'],
    ['exit', 'exit ${1:code}, ${2:message};', '退出'],
    ['logi', 'log.info($1);', 'info日志'],
    ['logd', 'log.debug($1);', 'debug日志'],
    ['loge', 'log.error($1);', 'error日志'],
    ['ass', 'assert ${1:condition} : ${2:code}, ${3:message}', '校验参数']
]

const CompletionItemProvider = {
    provideCompletionItems: async function (model, position) {
        let value = model.getValueInRange({
            startLineNumber: 1,
            startColumn: 1,
            endLineNumber: position.lineNumber,
            endColumn: position.column
        });
        let line = model.getValueInRange({
            startLineNumber: position.lineNumber,
            startColumn: 1,
            endLineNumber: position.lineNumber,
            endColumn: position.column
        });
        let word = model.getWordUntilPosition(position);
        let range = {
            startLineNumber: position.lineNumber,
            endLineNumber: position.lineNumber,
            startColumn: word.startColumn,
            endColumn: word.endColumn
        }
        let incomplete = false;
        let suggestions = quickSuggestions.map(item => {
            return {
                label: item[0],
                kind: monaco.languages.CompletionItemKind.Struct,
                detail: item[2] || item[1],
                insertText: item[1],
                filterText: item[0],
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                range
            }
        });
        if (line.length > 1 && (line.trim().indexOf('import')) === 0) {
            completionImport(suggestions, position, line, line.indexOf('import') + 6)
            incomplete = true;
        } else if (line.endsWith("::")) {
            suggestions = ['int', 'long', 'date', 'string', 'short', 'byte', 'float', 'double', 'json','stringify', 'sql'].map(it => {
                return {
                    label: it,
                    detail: `转换为${it === 'stringify' ? 'json字符串': it === 'sql' ? 'sql参数类型': it}`,
                    insertText: it,
                    kind: monaco.languages.CompletionItemKind.TypeParameter,
                    insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
                }
            })
        } else if (value.length > 1) {
            await completionScript(suggestions, value)
        } else {
            await completionFunction(suggestions, value, {
                ...RequestParameter.environmentFunction(),
                ...JavaClass.getAutoImportClass(),
                ...JavaClass.getAutoImportModule()
            })
        }
        return { suggestions, incomplete }
    },
    triggerCharacters: ['.', ':']
};
export default CompletionItemProvider