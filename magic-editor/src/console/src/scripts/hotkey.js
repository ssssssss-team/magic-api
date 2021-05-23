const Key = {
    Alt: 512,
    Ctrl: 1024,
    Shift: 2048
}
// A-Z
new Array(26).fill(0).forEach((_item, index) => Key[String.fromCharCode(65 + index)] = 65 + index);
// F1 - F12
new Array(12).fill(0).forEach((_item, index) => Key[`F${index + 1}`] = 112 + index);
const listeners = [];
const listener = (e) => {
    for (let i = 0, len = listeners.length; i < len; i++) {
        let listener = listeners[i];
        if ((listener.target.contains(e.target) || e.target === listener.target) && e.keyCode & listener.code === listener.code) {
            let controlKey = e.keyCode;
            controlKey |= (e.ctrlKey && Key.Ctrl || 0);
            controlKey |= (e.shiftKey && Key.Shift || 0);
            controlKey |= (e.altKey && Key.Alt || 0);
            controlKey |= (e.metaKey && Key.Ctrl || 0);
            if (controlKey == listener.code) {
                e.preventDefault();
                listener.callback();
                return;
            }
        }
    }
};
let inited = false;
Key.init = () => document.addEventListener('keydown', listener);

Key.bind = (target, code, callback) => {
    if(!inited){
        inited = true
        Key.init();
    }
    if (typeof callback === 'function') {
        listeners.push({target, code, callback});
    }
}
Key.unbind = () => {
    listeners.length = 0;
    document.removeEventListener('keydown', listener);
    inited = false;

}
export default Key;