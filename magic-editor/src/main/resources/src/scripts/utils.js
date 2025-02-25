import request from "@/api/request";
const Beautifier = require('./beautifier/javascript/beautifier').Beautifier
const replaceURL = (url) => url.replace(/:?\/+/g, e => e.indexOf(':') > -1 ? e : '/');
const isVisible = (elem) => elem && !!(elem.offsetWidth || elem.offsetHeight || elem.getClientRects().length);
const formatJson = (val, defaultVal) => {
    if (val) {
        if (typeof val == 'string') {
            return new Beautifier(val).beautify()
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

/*
 * @Description 深度克隆
 * ignoreFields 忽略克隆对象字段，只针对对象有效
 */
const deepClone = (obj, ignoreFields = []) => {
    // 对常见的“非”值，直接返回原来值
    if([null, undefined, NaN, false].includes(obj)) return obj;
    if(typeof obj !== "object" && typeof obj !== 'function') {
        //原始类型直接返回
        return obj;
    }
    var o = isArray(obj) ? [] : {};
    for(let i in obj) {
        if(obj.hasOwnProperty(i)){
            o[i] = typeof obj[i] === "object" ? deepClone(obj[i], ignoreFields) : obj[i];
        }
    }
    // 清除忽略字段
    ignoreFields.forEach(i => {
        delete o[i]
    })
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

/**
 * 获取url中的参数
 * @param {String} variable
 * @returns
 */
const getQueryVariable = (variable) => {
  var query = window.location.search.substring(1)
  var vars = query.split('&')
  for (var i = 0; i < vars.length; i++) {
    var pair = vars[i].split('=')
    if (pair[0] == variable) {
      return pair[1]
    }
  }
  return false
}
const getTextNodeList = (dom) => {
    const nodeList = [...dom.childNodes]
    const textNodes = []
    while (nodeList.length) {
        const node = nodeList.shift()
        if (node.nodeType === node.TEXT_NODE) {
            textNodes.push(node)
        } else {
            nodeList.unshift(...node.childNodes)
        }
    }
    return textNodes
}

const getTextInfoList = (textNodes) => {
    let length = 0
    return textNodes.map(node => {
        let startIdx = length, endIdx = length + node.wholeText.length
        length = endIdx
        return {
            text: node.wholeText,
            startIdx,
            endIdx
        }
    })
}
const getMatchList = (content, keyword) => {
    const characters = [...'[]()?.+*^${}:'].reduce((r, c) => (r[c] = true, r), {})
    keyword = keyword.split('').map(s => characters[s] ? `\\${s}` : s).join('[\\s\\n]*')
    const reg = new RegExp(keyword, 'gmi')
    return [...content.matchAll(reg)] // matchAll结果是个迭代器，用扩展符展开得到数组
}
const replaceMatchResult = (textNodes, textList, matchList) => {
    // 对于每一个匹配结果，可能分散在多个标签中，找出这些标签，截取匹配片段并用font标签替换出
    for (let i = matchList.length - 1; i >= 0; i--) {
        const match = matchList[i]
        const matchStart = match.index, matchEnd = matchStart + match[0].length // 匹配结果在拼接字符串中的起止索引
        // 遍历文本信息列表，查找匹配的文本节点
        for (let textIdx = 0; textIdx < textList.length; textIdx++) {
            const { text, startIdx, endIdx } = textList[textIdx] // 文本内容、文本在拼接串中开始、结束索引
            if (endIdx < matchStart) continue // 匹配的文本节点还在后面
            if (startIdx >= matchEnd) break // 匹配文本节点已经处理完了
            let textNode = textNodes[textIdx] // 这个节点中的部分或全部内容匹配到了关键词，将匹配部分截取出来进行替换
            const nodeMatchStartIdx = Math.max(0, matchStart - startIdx) // 匹配内容在文本节点内容中的开始索引
            const nodeMatchLength = Math.min(endIdx, matchEnd) - startIdx - nodeMatchStartIdx // 文本节点内容匹配关键词的长度
            if (nodeMatchStartIdx > 0) textNode = textNode.splitText(nodeMatchStartIdx) // textNode取后半部分
            if (nodeMatchLength < textNode.wholeText.length) textNode.splitText(nodeMatchLength)
            const span = document.createElement('span')
            span.innerText = text.substr(nodeMatchStartIdx, nodeMatchLength)
            span.className = 'keyword'
            textNode.parentNode.replaceChild(span, textNode)
        }
    }
}
const replaceKeywords = (htmlString, keyword) => {
    if (!keyword) return htmlString
    const div = document.createElement('div')
    div.innerHTML = htmlString
    const textNodes = getTextNodeList(div)
    const textList = getTextInfoList(textNodes)
    const content = textList.map(({ text }) => text).join('')
    const matchList = getMatchList(content, keyword)
    replaceMatchResult(textNodes, textList, matchList)
    return div.innerHTML
}
export {replaceURL, isVisible, formatJson, formatDate, paddingZero, download, requestGroup, deepClone, goToAnchor, getQueryVariable, replaceKeywords}
