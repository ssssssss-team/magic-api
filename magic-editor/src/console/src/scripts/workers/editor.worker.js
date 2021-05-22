import {initialize} from 'monaco-editor/esm/vs/editor/editor.worker'

self.onmessage = (e) => {
    initialize();
};