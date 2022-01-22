import MagicTask from './service/magic-task.js'
import localZhCN from './i18n/zh-cn.js'
import localEn from './i18n/en.js'
import MagicTaskInfo from './components/magic-task-info.vue'
import 'vite-plugin-svg-icons/register'
export default (opt) => {
    const i18n = opt.i18n
    // 添加i18n 国际化信息
    i18n.add('zh-cn', localZhCN)
    i18n.add('en', localEn)
    return {
        // 左侧资源
        resource: [{
            // 资源类型，和后端存储结构一致
            type: 'task',
            // 展示图标
            icon: '#magic-task-task',   // #开头表示图标在插件中
            // 展示名称
            title: 'task.name',
            // 运行服务
            service: MagicTask(opt.bus, opt.constants, i18n.format, opt.Message, opt.request),
        }],
        // 底部工具条
        toolbars: [{
            // 当打开的资源类型为 task 时显示
            type: 'task',
            // 工具条展示的标题
            title: 'task.title',
            // 展示图标
            icon: 'parameter',
            // 对应的组件
            component: MagicTaskInfo,
        }]
    }
}
