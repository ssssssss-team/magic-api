import Vue from 'vue';
import Contextmenu from "./Contextmenu";
import Submenu from "./Submenu";
import {COMPONENT_NAME} from "./constant";

const ContextmenuConstructor = Vue.extend(Contextmenu);
Vue.component(COMPONENT_NAME, Submenu);

function install(Vue) {
    let lastInstance = null;
    const ContextmenuProxy = function (options) {
        let instance = new ContextmenuConstructor();
        instance.menus = options.menus;
        instance.position.x = options.x || 0;
        instance.position.y = options.y || 0;
        if (options.event) {
            instance.position.x = options.event.clientX;
            instance.position.y = options.event.clientY;
        }
        instance.customClass = options.customClass;
        options.minWidth && (instance.style.minWidth = options.minWidth);
        options.zIndex && (instance.style.zIndex = options.zIndex);
        instance.destroy = options.destroy;
        ContextmenuProxy.destroy();
        lastInstance = instance;
        instance.$mount();
    };
    ContextmenuProxy.destroy = function (options) {
        if (lastInstance) {
            lastInstance.$destroy();
            lastInstance = null;
        }
    };
    Vue.prototype.$magicContextmenu = ContextmenuProxy;
}

export default {
    install
};
