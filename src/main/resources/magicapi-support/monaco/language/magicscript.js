require(['vs/editor/editor.main'], function() {
    monaco.languages.register({ id :'magicscript'});
    monaco.editor.defineTheme('magicscript', {
        base: 'vs',
        inherit: true,
        rules: [
            { token: 'object.null', foreground: 'ff0001' },
            { token: 'keywords', foreground: '0000ff' },
            { token: 'comment', foreground: '008000' },
            { token: 'comment.mul', foreground: '008000' },
            { token: 'method.call.empty', foreground: 'ff0000', fontStyle: 'bold' },
        ]
    });
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
    monaco.languages.registerCompletionItemProvider('magicscript',{
        provideCompletionItems : function(){
            var suggestions  = [{
                label : 'if',
                kind: monaco.languages.CompletionItemKind.Snippet,
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                insertText : [
                    'if (${1:condition}) {',
                    '\t$0',
                    '}'
                ].join('\n')
            },{
                label : 'ifelse',
                kind: monaco.languages.CompletionItemKind.Snippet,
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                insertText : [
                    'if (${1:condition}) {',
                    '\t$2',
                    '} else {',
                    '\t$3',
                    '}'
                ].join('\n')
            },{
                label : 'for',
                kind: monaco.languages.CompletionItemKind.Snippet,
                insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
                insertText : [
                    'for (${1:item} in ${2:range(${3:0},${4:100})}}) {',
                    '\t$0',
                    '}'
                ].join('\n')
            }]
            return {
                suggestions : suggestions
            }
        }
    })
    monaco.languages.setMonarchTokensProvider('magicscript',{
        escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,
        keywords : ['new','var','if','else','for','return','import','break','continue','as'],
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
                [/(@digits)[lLbBsS]?/, 'number'],
                [/(@digits)[dDfF]?/, 'number.float'],
                [/\/\*\*(?!\/)/, 'comment.mul', '@mulcomment'],
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
                [/[^\/*]+/, 'comment.mul'],
                // [/\/\*/, 'comment.doc', '@push' ],    // nested comment not allowed :-(
                [/\/\*/, 'comment.mul.invalid'],
                [/\*\//, 'comment.mul', '@pop'],
                [/[\/*]/, 'comment.mul']
            ],
            string_multi: [
                [/"""/, {token: 'string.multi', next: '@pop'}],
                [/"/, {token: 'string.multi', next: '@string_multi_embedded', nextEmbedded: 'sql'}]
            ],
            string_multi_embedded: [
                [/"""/, {token: '@rematch', next: '@pop', nextEmbedded: '@pop'}],
                [/[^"""]+/, '']
            ],
            string_double: [
                [/[^\\"]+/, 'string'],
               // [/@escapes/, 'string.escape'],
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