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
        brackets: [
            ['{', '}'],
            ['[', ']'],
            ['(', ')'],
        ],
        operators: ['<=', '>=', '==', '!=', '+', '-','*', '/', '%', '&','|', '!', '&&', '||', '?', ':', ],
        autoClosingPairs: [
            { open: '{', close: '}' },
            { open: '[', close: ']' },
            { open: '(', close: ')' },
            { open: '"', close: '"', notIn: ['string'] },
            { open: '\'', close: '\'', notIn: ['string'] },
        ],
    })

    monaco.languages.setMonarchTokensProvider('magicscript',{
        escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,
        keywords : ['var','if','else','for','return','import','break','continue','as'],
        tokenizer : {
            root : [
                [/[a-zA-Z_$][\w$]*/,{
                    cases :{
                        "@keywords" : { token : "keywords" },
                        "@default" : "identifier"
                    }
                }],
                [/\/\*\*(?!\/)/, 'comment.mul', '@mulcomment'],
                [/\/\*/, 'comment', '@comment'],
                [/\/\/.*$/, 'comment'],
                [/\./, {token : 'period'}],
                [/"([^"\\]|\\.)*$/, 'string.invalid'],
                [/'([^'\\]|\\.)*$/, 'string.invalid'],
                [/"/, 'string', '@string_double'],
                [/'/, 'string', '@string_single'],
                [/\}/, { token: 'sf-end', next: '@pop' }],
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