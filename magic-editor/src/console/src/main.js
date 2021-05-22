import Vue from 'vue'
import App from './App.vue'
import MagicContextMenu from './components/common/magic-contextmenu'
import Modal from './components/common/modal'

Vue.config.productionTip = false

Vue.use(MagicContextMenu)
Vue.use(Modal)

new Vue({
    render: h => h(App),
}).$mount('#app')
