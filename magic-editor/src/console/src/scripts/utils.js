import request from "@/api/request";

const replaceURL = (url) => url.replace(/:?\/+/g, e => e.indexOf(':') > -1 ? e : '/');
const isVisible = (elem) => elem && !!(elem.offsetWidth || elem.offsetHeight || elem.getClientRects().length);
const formatJson = (val, defaultVal) => {
    if (val) {
        if (typeof val == 'string') {
            try {
                val = JSON.parse(val);
            } catch (ignored) {
                return val;
            }
        }
        if (val) {
            return JSON.stringify(val, null, 4);
        }
    }
    return defaultVal || ''
};
const paddingZero = (val) => val < 10 ? '0' + val : val.toString();
const formatDate = (val) => {
    if (typeof val === 'number') {
        if (val.toString().length === 13) {
            val = new Date(val)
        } else {
            val = new Date(val * 1000)
        }
    }
    if (val instanceof Date) {
        var month = val.getMonth() + 1;
        var day = val.getDate();
        var hour = val.getHours();
        var minute = val.getMinutes();
        var seconds = val.getSeconds();
        return val.getFullYear() + '-' + paddingZero(month) + '-' + paddingZero(day) + ' ' + paddingZero(hour) + ':' + paddingZero(minute) + ':' + paddingZero(seconds);
    }
    return '';
};
const download = (blob,filename)=>{
    let element = document.createElement('a')
    let href = window.URL.createObjectURL(blob);
    element.href = href;
    element.download = filename;
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
    window.URL.revokeObjectURL(href)
}
const requestGroup = (path, group) => {
    return request.send(path, JSON.stringify({
        id: group.id,
        name: group.name,
        path: group.path,
        type: group.type,
        paths: group.paths,
        options: group.options,
        parentId: group.parentId
    }), {
        method: 'post',
        headers: {
            'Content-Type': 'application/json'
        },
        transformRequest: []
    })
}
export {replaceURL, isVisible, formatJson, formatDate, paddingZero, download, requestGroup}