export default function (bus, constants, $i, Message, request) {
    return {
        // svg text
        getIcon: item => ['TASK', '#9012FE'],
        // 任务名称
        name: $i('task.name'),
        // 脚本语言
        language: 'magicscript',
        // 默认脚本
        defaultScript: `return 'Hello magic-api-task'`,
        // 执行测试的逻辑
        doTest: (opened) => {
            opened.running = true
            const info = opened.item
            const requestConfig = {
                baseURL: constants.SERVER_URL,
                url: '/task/execute',
                method: 'POST',
                responseType: 'json',
                headers: {},
                withCredentials: true
            }
            bus.$emit(Message.SWITCH_TOOLBAR, 'log')
            requestConfig.headers[constants.HEADER_REQUEST_CLIENT_ID] = constants.CLIENT_ID
            requestConfig.headers[constants.HEADER_REQUEST_SCRIPT_ID] = opened.item.id
            requestConfig.headers[constants.HEADER_MAGIC_TOKEN] = constants.HEADER_MAGIC_TOKEN_VALUE
            // 设置断点
            requestConfig.headers[constants.HEADER_REQUEST_BREAKPOINTS] = (opened.decorations || []).filter(it => it.options.linesDecorationsClassName === 'breakpoints').map(it => it.range.startLineNumber).join(',')
            const fullName = opened.path()
            bus.status(`开始测试定时任务「${fullName}」`)
            request.sendPost('/task/execute', { id: info.id }, requestConfig).success(res => {
                opened.running = false
            }).end(() => {
                bus.status(`定时任务「${fullName}」测试完毕`)
                opened.running = false
            })
        },
        // 是否允许执行测试
        runnable: true,
        // 是否需要填写路径
        requirePath: true,
        // 合并
        merge: item => item
    }
}
