export default {
    editor: {
        base: 'vs',
        rules: [
            {background: '#ffffff'},
            {token: 'keywords', foreground: '000080', fontStyle: 'bold'},
            {token: 'number', foreground: '0000FF'},
            {token: 'keyword', foreground: '000080', fontStyle: 'bold'},
            {token: 'string.sql', foreground: '008000'},
            {token: 'predefined', foreground: '000000', fontStyle: 'italic'},
            {token: 'operator.sql', foreground: '000080', fontStyle: 'bold'},
            {token: 'key', foreground: '660E7A'},
            {token: 'string.key.json', foreground: '660E7A'},
            {token: 'string.value.json', foreground: '008000'},
            {token: 'keyword.json', foreground: '0000FF'},
            {token: 'string', foreground: '008000', fontStyle: 'bold'},
            {token: 'string.invalid', foreground: '008000', background: 'FFCCCC'},
            {token: 'string.escape.invalid', foreground: '008000', background: 'FFCCCC'},
            {token: 'string.escape', foreground: '000080', fontStyle: 'bold'},
            {token: 'comment', foreground: '808080'},
            {token: 'comment.doc', foreground: '808080'},
            {token: 'string.escape', foreground: '000080'}
        ],
        colors: {
            'editor.foreground': '#000000',
            'editor.background': '#ffffff',
            'editorLineNumber.foreground': '#999999',	//行号的颜色
            'editorGutter.background': '#f0f0f0',	//行号背景色
            'editor.lineHighlightBackground': '#FFFAE3',	//光标所在行的颜色
            'dropdown.background': '#F2F2F2',	//右键菜单
            'dropdown.foreground': '#000000',	//右键菜单文字颜色
            'list.activeSelectionBackground': '#1A7DC4',	//右键菜单悬浮背景色
            'list.activeSelectionForeground': '#ffffff',	//右键菜单悬浮文字颜色
        }
    }
};