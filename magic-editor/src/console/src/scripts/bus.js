import Vue from 'vue'
import contants from './contants.js'

const bus = new Vue()
try {
    window._mtac = {};
    let element = document.createElement("script");
    element.src = "//pingjs.qq.com/h5/stats.js?v2.0.4";
    element.setAttribute("name", "MTAH5");
    element.setAttribute("sid", "500724136");
    element.setAttribute("cid", "500724141");
    let s = document.getElementsByTagName("script")[0];
    s.parentNode.insertBefore(element, s);
    element.onload = element.onreadystatechange = function () {
        if (!this.readyState || this.readyState == 'loaded' || this.readyState == 'complete') {
            bus.$emit('report', contants.MAGIC_API_VERSION);
        }
    }
} catch (ignored) {

}
bus.$on('report', (eventId) => {
    try {
        window.MtaH5.clickStat(eventId);
    } catch (ignored) {

    }
})
export default bus 