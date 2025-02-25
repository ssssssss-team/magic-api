import MagicAlert from './magic-alert.vue'
import MagicConfirm from './magic-confirm.vue'
import MagicDialog from './magic-dialog.vue'
import Vue from 'vue'

const MagicAlertConstructor = Vue.extend(MagicAlert)
const MagicConfirmConstructor = Vue.extend(MagicConfirm)
const MagicDialogConstructor = Vue.extend(MagicDialog)

const MagicAlertProxy = function (options) {
    let instance = new MagicAlertConstructor()
    instance.value = true
    for (let key in options) {
        if (options[key] !== undefined && options[key] !== null) {
            instance[key] = options[key]
        }
    }
    instance.$mount()
    document.getElementsByClassName('ma-container')[0].append(instance.$el)
}
const MagicConfirmProxy = function (options) {
    let instance = new MagicConfirmConstructor()
    instance.value = true
    for (let key in options) {
        if (options[key] !== undefined && options[key] !== null) {
            instance[key] = options[key]
        }
    }
    instance.$mount()
    document.getElementsByClassName('ma-container')[0].append(instance.$el)
}
const MagicDialogProxy = function (options) {
    let instance = new MagicDialogConstructor()
    instance.value = true
    for (let key in options) {
        if (options[key] !== undefined && options[key] !== null) {
            instance[key] = options[key]
        }
    }
    instance.$mount()
    document.getElementsByClassName('ma-container')[0].append(instance.$el)
}

function install(Vue) {
    Vue.prototype.$magicAlert = MagicAlertProxy
    Vue.prototype.$magicConfirm = MagicConfirmProxy
    Vue.prototype.$magicDialog = MagicDialogProxy
}

const modal = {
    magicAlert: MagicAlertProxy,
    magicConfirm: MagicConfirmProxy,
    magicDialog: MagicDialogProxy
}

export default {
    install
}

export {modal}
