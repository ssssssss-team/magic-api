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
// 判断arr是否为一个数组，返回一个bool值
const isArray = (arr) => {
  return Object.prototype.toString.call(arr) === '[object Array]';
}

// 深度克隆
const deepClone = (obj) => {
  // 对常见的“非”值，直接返回原来值
  if([null, undefined, NaN, false].includes(obj)) return obj;
  if(typeof obj !== "object" && typeof obj !== 'function') {
    //原始类型直接返回
    return obj;
  }
  var o = isArray(obj) ? [] : {};
  for(let i in obj) {
    if(obj.hasOwnProperty(i)){
      o[i] = typeof obj[i] === "object" ? deepClone(obj[i]) : obj[i];
    }
  }
  return o;
}

// 展示锚点对象
const goToAnchor = (dom) => {
  if (typeof dom === 'string') {
    dom = document.querySelector(dom)
  }
  if (dom) {
    dom.scrollIntoView(true)
  }
}
export {replaceURL, isVisible, formatJson, formatDate, paddingZero, download, requestGroup, deepClone, goToAnchor}
