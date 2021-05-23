import './assets/index.css'

import MagicEditor from './components/magic-editor'
import MagicContextMenu from './components/common/magic-contextmenu'
import Modal from './components/common/modal'
import _Vue from 'vue'

/* 打包组件使用 */
import 'monaco-editor/esm/vs/editor/editor.worker.js'
import 'monaco-editor/esm/vs/language/json/json.worker.js'
import 'monaco-editor/esm/vs/language/json/workerManager'
import 'monaco-editor/esm/vs/language/json/jsonMode'
import 'monaco-editor/esm/vs/basic-languages/sql/sql.contribution.js'
import 'monaco-editor/esm/vs/basic-languages/sql/sql.js'

export function install(Vue) {
    if (install.installed) return
    install.installed = true
    Vue.component('MagicEditor', MagicEditor)
    Vue.use(MagicContextMenu)
    Vue.use(Modal)
}

const plugin = {
    install
}

let GlobalVue = null
if (typeof window !== 'undefined' && window.Vue) {
    GlobalVue = window.Vue
} else if (typeof global !== 'undefined' && global.Vue) {
    GlobalVue = global.Vue
} else {
    GlobalVue = _Vue;
}
if (GlobalVue) {
    GlobalVue.use(plugin)
}

export default MagicEditor