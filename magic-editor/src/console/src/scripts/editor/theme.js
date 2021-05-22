import * as monaco from 'monaco-editor';

export const Themes = {};
export const defineTheme = (name, options) => {
    options = options || {};
    let editor = options.editor || {};
    editor.base = editor.base || 'vs';
    editor.inherit = editor.inherit === undefined ? true : editor.inherit;
    editor.rules = editor.rules || [];
    editor.colors = editor.colors || [];
    monaco.editor.defineTheme(name, editor);
    Themes[name] = options.styles || {};
}