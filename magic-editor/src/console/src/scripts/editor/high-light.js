export const HighLightOptions = {
    escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,
    builtinFunctions: [],
    digits: /[0-9_]+/,
    binarydigits: /[0-1_]+/,
    hexdigits: /[[0-9a-fA-F_]+/,
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
                    "~(new|var|if|else|for|in|return|import|break|continue|as|null|true|false|try|catch|finally|async|while|exit|asc|desc|ASC|DESC|assert|let|const)[\\s]?": {token: "keywords"},
                    "~(select|from|left|join|on|and|or|order|by|where|group|having|SELECT|FROM|LEFT|JOIN|ON|AND|OR|ORDER|BY|WHERE|GROUP|HAVING)[\\s]{1}": {token: "keywords"},
                    "@default": "identifier"
                }
            }],
            [/::[a-zA-Z]+/, 'keywords'],
            [/[{}()[\]]/, '@brackets'],
            [/(@digits)\.(@digits)/, 'number.float'],
            [/0[xX](@hexdigits)n?/, 'number.hex'],
            [/0[bB](@binarydigits)n?/, 'number.binary'],
            [/(@digits)[lLbBsSdDfFmM]?/, 'number'],
            [/\/\*\**/, 'comment', '@comment'],
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
            [/`/, 'string', '@string_backtick']
        ],
        comment: [
            [/\*\//, 'comment', '@popall'],
            [/\S((TODO)|(todo)|(fixme)|(FIXME))\s+/, 'comment'],
            [/((TODO)|(todo)|(fixme)|(FIXME))\s+[^(*/)]+/, 'comment.todo'],
            [/\S/, 'comment'],
        ],
        commentTodo: [
            [/^/,'', '@popall'],
            [/\S((TODO)|(todo)|(fixme)|(FIXME))\s+/, 'comment'],
            [/((TODO)|(todo)|(fixme)|(FIXME))[ \t]+[^\n]+/, 'comment.todo','@popall'],
            [/\S/, 'comment'],
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
        string_backtick: [
            [/\$\{/, { token: 'delimiter.bracket', next: '@bracketCounting' }],
            [/[^\\`$]+/, 'string'],
            [/@escapes/, 'string.escape'],
            [/\\./, 'string.escape.invalid'],
            [/`/, 'string', '@pop']
        ],
        bracketCounting: [
            [/\{/, 'delimiter.bracket', '@bracketCounting'],
            [/\}/, 'delimiter.bracket', '@pop'],
            { include: 'root' }
        ]
    }
};