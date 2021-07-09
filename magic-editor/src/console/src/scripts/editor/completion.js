import JavaClass from './java-class.js'
import tokenizer from '../parsing/tokenizer.js'
import {TokenStream, TokenType} from '../parsing/index.js'
import {Parser} from '../parsing/parser.js'
import * as monaco from 'monaco-editor'
import RequestParameter from './request-parameter.js'

const completionImport = (suggestions, position, line, importIndex) => {
    let len = 0;
    let start = line.indexOf('"') + 1;
    if (start === 0) {
        start = line.indexOf("'") + 1;
    }
    let keyword = line.trim().substring(importIndex + 6).trim().replace(/['|"]/g, '').toLowerCase();
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
const completionFunction = (suggestions, input) => {
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
    Object.keys(RequestParameter.environmentFunction()).forEach(it => {
        suggestions.push({
            sortText: '00000000' + it,
            label: it,
            filterText: it,
            kind: monaco.languages.CompletionItemKind.Variable,
            detail: it,
            insertText: it,
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
        })
    })
    let known = suggestions.map(it => it.detail);
    let matches = input.match(/[a-zA-Z_$]+/ig);
    let count = matches.length;
    if(count > 2){
        matches.filter((it,index) => index + 2 < count && known.indexOf(it) === -1).map(it => {
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

async function completionScript(suggestions, input) {
    try {
        let tokens = tokenizer(input);
        let tokenLen = tokens.length;
        if (tokenLen === 0) {
            return;
        }
        let tokenType = tokens[tokenLen - 1].getTokenType();
        if (tokenType === TokenType.Identifier) {
            if (tokenLen === 1) {
                completionFunction(suggestions, input);
                return;
            }
            tokenType = tokens[tokenLen - 2].getTokenType();
            tokens.pop();
        }
        if (tokenType === TokenType.Period) {
            tokens.pop();
        } else {
            completionFunction(suggestions, input);
            return;
        }
        let parser = new Parser(new TokenStream(tokens));
        let clazz = await parser.completion(RequestParameter.environmentFunction());
        if (clazz) {
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
                        document.push('- ' + param.name + '：' + (param.comment || param.type));
                        document.push('---')
                    }
                    document.push(`- 返回值：\`${method.returnType}\``)
                    suggestions.push({
                        sortText: method.sortText || method.fullName,
                        label: method.fullName,
                        kind: monaco.languages.CompletionItemKind.Method,
                        detail: method.comment,
                        documentation: {
                            value: document.join('\r\n')
                        },
                        insertText: method.insertText,
                        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
                    })
                }
            }
        }
    } catch (e) {
        // console.log(e);
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
        } else if (line.endsWith(":")) {
            if (line.endsWith("::")) {
                suggestions = ['int', 'long', 'date', 'string', 'short', 'byte', 'float', 'double', 'json','stringify'].map(it => {
                    return {
                        label: it,
                        detail: `转换为${it === 'stringify' ? 'json字符串': it}`,
                        insertText: it,
                        kind: monaco.languages.CompletionItemKind.TypeParameter,
                        insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet
                    }
                })
            }
        } else if (value.length > 1) {
            await completionScript(suggestions, value)
        }
        return {suggestions}
    },
    triggerCharacters: ['.', ":"]
};
export default CompletionItemProvider