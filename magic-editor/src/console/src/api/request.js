import axios from 'axios'
import Qs from 'qs'
import {modal} from '@/components/common/modal'
import {replaceURL} from '@/scripts/utils.js'
import contants from '@/scripts/contants.js'

const config = {
    // 请求路径
    baseURL: '',
    // 默认请求方法
    method: 'post',
    // 请求超时时间（毫秒）
    timeout: 0,
    // 是否携带cookie信息
    withCredentials: true,
    // 响应格式,可选项 'arraybuffer', 'blob', 'document', 'json', 'text', 'stream'
    responseType: 'json',
    // 自定义添加头部
    headers: {
        // ;charset=UTF-8
        'Content-Type': 'application/x-www-form-urlencoded'
    },
    // `transformRequest` 允许在向服务器发送前，修改请求数据
    // 只能用在 'PUT', 'POST' 和 'PATCH' 这几个请求方法
    // 后面数组中的函数必须返回一个字符串，或 ArrayBuffer，或 Stream
    transformRequest: [
        function (data) {
            if(data instanceof FormData){
                return data;
            }
            return Qs.stringify(data, {
                // a:[1,2] => a=1&a=2
                arrayFormat: 'repeat',
                // a[b]:1 => a.b=1
                allowDots: true
            })
        }
    ],
    paramsSerializer(data) {
        return Qs.stringify(data, {
            // a:[1,2] => a=1&a=2
            arrayFormat: 'repeat',
            // a[b]:1 => a.b=1
            allowDots: true
        })
    }
}

class HttpResponse {
    // 请求成功调用 code == 1的
    successHandle = null
    // 请求异常，包含js异常调用
    errorHandle = null

    endHandle = null

    constructor() {
    }

    // 异常回调，实际上的code != 1的
    exceptionHandle = (code, message) => {
        modal.magicAlert({
            title: `请求出错，异常代码(${code})`,
            content: message
        })
    }

    // 调用返回成功需要注入的变量
    success(handle) {
        this.successHandle = handle
        return this
    }

    exception(handle) {
        this.exceptionHandle = handle
        return this
    }

    // 调用异常需要注入的变量
    error(handle) {
        this.errorHandle = handle
        return this
    }

    end(handle) {
        this.endHandle = handle;
    }
}

class HttpRequest {
    _axios = null

    constructor() {
        this._axios = axios.create(config)
    }

    // 返回初始化过后的axios
    getAxios() {
        return this._axios
    }

    setBaseURL(baseURL) {
        config.baseURL = baseURL
    }

    execute(requestConfig) {
        let _config = {
            baseURL: config.baseURL,
            ...requestConfig
        }
        _config.headers = _config.headers || {};
        _config.headers[contants.HEADER_MAGIC_TOKEN] = contants.HEADER_MAGIC_TOKEN_VALUE
        return this._axios.request(_config);
    }

    processError(error) {
        if (error.response) {
            modal.magicAlert({
                title: `请求出错HttpStatus:(${error.response.status})`,
                content: JSON.stringify(error.response.data || '') || `请求出错HttpStatus:(${error.response.status})`
            })
        } else {
            modal.magicAlert({
                title: `请求出错`,
                content: error.message
            })
        }
        console.error(error)
    }

    // 发送默认请求
    send(url, params, newConfig) {
        let requestConfig = newConfig || config || {}
        requestConfig.url = url
        if (requestConfig.method === 'post') {
            requestConfig.data = params
        } else {
            requestConfig.params = params
        }
        requestConfig.baseURL = config.baseURL
        let httpResponse = new HttpResponse()
        this.execute(requestConfig)
            .then(response => {
                let data = response.data
                if(data instanceof Blob){
                    httpResponse.successHandle && httpResponse.successHandle(data, response)
                }else if (data.code === 1) {
                    httpResponse.successHandle && httpResponse.successHandle(data.data, response)
                } else {
                    httpResponse.exceptionHandle && httpResponse.exceptionHandle(data.code, data.message, response)
                }
            })
            .catch((error) => {
                if (typeof httpResponse.errorHandle === 'function') {
                    httpResponse.errorHandle(error.response.data, error.response, error)
                } else {
                    this.processError(error)

                }
            })
            .finally(() => {
                if (typeof httpResponse.endHandle === 'function') {
                    httpResponse.endHandle()
                }
            })
        return httpResponse
    }

    createConsole() {
        return new EventSource(replaceURL(config.baseURL + '/console'));
    }
}

export default new HttpRequest()
