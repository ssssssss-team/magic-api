import JavaClass from './java-class.js'
import tokenizer from '../parsing/tokenizer.js'
import {TokenStream} from '../parsing/index.js'
import {Parser} from '../parsing/parser.js'
import * as monaco from 'monaco-editor'
import RequestParameter from "@/scripts/editor/request-parameter";

const completionImport = (suggestions, position, line, importIndex) => {
    let len = 0;
    let start = line.indexOf('"') + 1;
    if (start === 0) {
        start = line.indexOf("'") + 1;
    }
    if(start === 0){
        JavaClass.getDefineModules().forEach(module => suggestions.push({
            label: module,
            filterText: module,
            kind: monaco.languages.CompletionItemKind.Module,
            detail: module,
            insertText: module,
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
        }))
        return;
    }
    let text = line.trim().substring(importIndex + 6).trim().replace(/['|"]/g, '');
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
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
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
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
            })
        })
        return;
    }
    let keyword = text.toLowerCase();
    let importClass = JavaClass.getImportClass();
    if (start !== 0 && keyword && (len = importClass.length) > 0) {
        let set = new Set();
        for (let i = 0; i < len; i++) {
            let clazz = importClass[i];
            let index = clazz.toLowerCase().indexOf(keyword);
            if (index > -1) {
                if ((index = clazz.indexOf('.', index + keyword.length)) > -1) {
                    let content = clazz.substring(0, index);
                    content = content.substring(content.lastIndexOf('.') + 1) + '.';
                    if (set.has(content)) {
                        continue;
                    }
                    set.add(content);
                    suggestions.push({
                        sortText: '1',
                        label: content,
                        kind: monaco.languages.CompletionItemKind.Folder,
                        filterText: clazz,
                        detail: content,
                        insertText: content,
                        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                        command: {
                            id: 'editor.action.triggerSuggest'
                        }
                    })
                } else {
                    suggestions.push({
                        sortText: '2',
                        label: clazz.substring(clazz.lastIndexOf('.') + 1),
                        kind: monaco.languages.CompletionItemKind.Module,
                        filterText: clazz,
                        detail: clazz,
                        insertText: clazz,
                        range: new monaco.Range(position.lineNumber, start + 1, position.lineNumber, position.column)
                    })
                }
            }
        }
    }
}
const completionFunction = (suggestions, input, env) => {
    env = env || {}
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
    if(count > 2){
        Array.from(new Set(matches)).filter((it,index) => index + 2 < count && known.indexOf(it) === -1 && vars.indexOf(it) === -1).map(it => {
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
            for (let j = (method.extension ? 1 : 0); j < method.parameters.length; j++) {
                let param = method.parameters[j];
                document.push('`' + param.name + '` ' + (param.comment || param.type));
                document.push('\r\n')
            }
            method.comment && document.push('\r\n') && document.push(method.comment)
            suggestions.push({
                sortText: method.sortText || method.fullName,
                label: method.fullName,
                kind: monaco.languages.CompletionItemKind.Method,
                detail: `${simpleName}.${method.fullName}: ${method.returnType}`,
                documentation: {
                    value: document.join('\r\n')
                },
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
            completionFunction(suggestions, input)
            return;
        }
        let parser = new Parser(new TokenStream(tokens));
        const { best, env } = await parser.parseBest(input.length - 1, env);
        if(input.endsWith(".")){
            await completionMethod(await best.getJavaType(env), suggestions)
        } else if(best) {
            let astName = best.constructor.name;
            if (astName === 'MemberAccess' || astName === 'MethodCall') {
                await completionMethod(await best.target.getJavaType(env), suggestions)
            } else {
                completionFunction(suggestions, input, env)
            }
        } else {
            completionFunction(suggestions, input, env)
        }
        return suggestions;
    } catch (e) {
        // console.log("error")
    }
}

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
        let suggestions = [];
        let importIndex;
        if (line.length > 1 && (importIndex = line.trim().indexOf('import')) === 0) {
            completionImport(suggestions, position, line, importIndex)
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
            completionFunction(suggestions, value, {
                ...RequestParameter.environmentFunction(),
                ...JavaClass.getAutoImportClass(),
                ...JavaClass.getAutoImportModule()
            })
        }
        return {suggestions}
    },
    triggerCharacters: ['.', ":"]
};
export default CompletionItemProvider