export const HighLightOptions = {
    escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,
    builtinFunctions: [
        "count", "max", "min", "avg", "sum",
        "round", "ceil", "floor", "precent",
        "date_format", "ifnull", "now", "uuid"
    ],
    digits: /\d+(_+\d+)*/,
    regexpctl: /[(){}\[\]\$\^|\-*+?\.]/,
    regexpesc: /\\(?:[bBdDfnrstvwWn0\\\/]|@regexpctl|c[A-Z]|x[0-9a-fA-F]{2}|u[0-9a-fA-F]{4})/,
    tokenizer: {
        root: [
            [/\s+/, 'white'],
            [
                /```((?:\w|[\/\-#])+).*$/,
                { token: 'string', next: '@codeblockgh', nextEmbedded: '$1' }
            ],
            [/```$/, { token: 'string', next: '@codeblock' }],
            [/[a-zA-Z_$][\w$]*[\s]?/, {
                cases: {
                    '@builtinFunctions': 'predefined',
                    "~(new|var|if|else|for|in|return|import|break|continue|as|null|true|false|try|catch|finally|async|while|exit|asc|desc|ASC|DESC)[\\s]?": {token: "keywords"},
                    "~(select|from|left|join|on|and|or|order|by|where|group|having|SELECT|FROM|LEFT|JOIN|ON|AND|OR|ORDER|BY|WHERE|GROUP|HAVING)[\\s]{1}": {token: "keywords"},
                    "@default": "identifier"
                }
            }],
            [/::[a-zA-Z]+/, 'keywords'],
            [/[{}()[\]]/, '@brackets'],
            [/(@digits)[lLbBsSdDfFmM]?/, 'number'],
            [/\/\*/, 'comment', '@comment'],
            [/\/\//, 'comment', '@commentTodo'],
            [
                /\/(?=([^\\\/]|\\.)+\/([gimsuy]*)(\s*)(\.|;|,|\)|\]|\}|$))/,
                {token: 'regexp', bracket: '@open', next: '@regexp'}
            ],
            [/[;,.]/, 'delimiter'],
            [/"""/, {token: 'string', next: '@string_multi_embedded', nextEmbedded: 'sql'}],
            [/"([^"\\]|\\.)*$/, 'string.invalid'],
            [/'([^'\\]|\\.)*$/, 'string.invalid'],
            [/"/, 'string', '@string_double'],
            [/'/, 'string', '@string_single'],
        ],
        comment: [
            [/((TODO)|(todo)|(fixme)|(FIXME))[ \t]+[^\n(?!\*\/)]+/, 'comment.todo','@comment'],
            [/[ \t]+/, 'comment','@comment'],
            [/\*\//, 'comment', '@popall'],
            [/[^ \t]+(?!((TODO)|(todo)|(fixme)|(FIXME)))/, 'comment','@comment']
        ],
        commentTodo: [
            [/((TODO)|(todo)|(fixme)|(FIXME))[ \t]+[^\n]+/, 'comment.todo','@popall'],
            [/^/,'', '@popall'],
            [/[^ \t]+(?!((TODO)|(todo)|(fixme)|(FIXME)))/, 'comment', '@commentTodo']

        ],
        regexp: [
            [
                /(\{)(\d+(?:,\d*)?)(\})/,
                ['regexp.escape.control', 'regexp.escape.control', 'regexp.escape.control']
            ],
            [
                /(\[)(\^?)(?=(?:[^\]\\\/]|\\.)+)/,
                ['regexp.escape.control', {token: 'regexp.escape.control', next: '@regexrange'}]
            ],
            [/(\()(\?:|\?=|\?!)/, ['regexp.escape.control', 'regexp.escape.control']],
            [/[()]/, 'regexp.escape.control'],
            [/@regexpctl/, 'regexp.escape.control'],
            [/[^\\\/]/, 'regexp'],
            [/@regexpesc/, 'regexp.escape'],
            [/\\\./, 'regexp.invalid'],
            [
                /(\/)([gimsuy]*)/,
                [{token: 'regexp', bracket: '@close', next: '@pop'}, 'keyword.other']
            ]
        ],
        codeblock: [
            [/^```$/, { token: 'string', next: '@pop' }],
            [/.*$/, 'variable.source']
        ],
        codeblockgh: [
            [/```\s*$/, { token: 'variable.source', next: '@pop', nextEmbedded: '@pop' }],
            [/[^`]+/, 'variable.source']
        ],
        regexrange: [
            [/-/, 'regexp.escape.control'],
            [/\^/, 'regexp.invalid'],
            [/@regexpesc/, 'regexp.escape'],
            [/[^\]]/, 'regexp'],
            [
                /\]/,
                {
                    token: 'regexp.escape.control',
                    next: '@pop',
                    bracket: '@close'
                }
            ]
        ],
        string_multi_embedded: [
            [/[^"]+/, ''],
            ['"""', {token: 'string', next: '@pop', nextEmbedded: '@pop'}]

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
};