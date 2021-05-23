import * as worker from 'monaco-editor/esm/vs/editor/editor.worker.js';
import {JSONWorker} from 'monaco-editor/esm/vs/language/json/jsonWorker.js';

self.onmessage = function () {
    // ignore the first message
    worker.initialize(function (ctx, createData) {
        return new JSONWorker(ctx, createData);
    });
};