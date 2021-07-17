<template>
  <div ref="container" :style="themeStyle" class="ma-container" tabindex="0">
    <magic-loading v-if="loading" :title.sync="config.title" :version.sync="config.version" />
    <magic-login v-if="showLogin" :onLogin="onLogin" />
    <!-- 顶部Header -->
    <magic-header :config="config" :themeStyle.sync="themeStyle" class="not-select" />
    <ul class="ma-toolbar-container not-select">
      <li v-for="(item, index) in toolbars" :key="'toolbar_' + index" :class="{ selected: toolbarIndex === index }" @click="toolbarIndex = toolbarIndex === index ? -1 : index">{{ item }}<i class="ma-icon ma-icon-list"></i></li>
    </ul>
    <!-- 中间主要内容 -->
    <div class="ma-main-container">
      <div class="ma-middle-container">
        <magic-api-list v-show="toolbarIndex === 0" ref="apiList" :style="{ width: toolboxWidth }" class="not-select" />
        <magic-function-list v-show="toolbarIndex === 1" ref="functionList" :style="{ width: toolboxWidth }" class="not-select" />
        <magic-datasource-list v-show="toolbarIndex === 2" ref="datasourceList" :style="{ width: toolboxWidth }" class="not-select" />
        <div ref="resizer" class="ma-resizer-x" @mousedown="doResizeX"></div>
        <magic-script-editor class="ma-editor-container" />
      </div>
      <!-- 底部区域 -->
      <magic-options />
    </div>
    <!-- 状态条 -->
    <magic-status-bar :config="config" />
  </div>
</template>

<script>
import '@/assets/index.css'
import '@/assets/iconfont/iconfont.css'
import bus from '@/scripts/bus.js'
import MagicLoading from './common/magic-loading.vue'
import MagicHeader from './layout/magic-header.vue'
import MagicLogin from './layout/magic-login.vue'
import MagicStatusBar from './layout/magic-status-bar.vue'
import MagicOptions from './layout/magic-options.vue'
import MagicApiList from './resources/magic-api-list.vue'
import MagicFunctionList from './resources/magic-function-list.vue'
import MagicDatasourceList from './resources/magic-datasource-list.vue'
import MagicScriptEditor from './editor/magic-script-editor.vue'
import request from '@/api/request.js'
import contants from '@/scripts/contants.js'
import MagicWebSocket from '@/scripts/websocket.js'
import store from '@/scripts/store.js'
import Key from '@/scripts/hotkey.js'
import {replaceURL,getQueryVariable} from '@/scripts/utils.js'
import {defineTheme} from '@/scripts/editor/theme.js'
import defaultTheme from '@/scripts/editor/default-theme.js'
import darkTheme from '@/scripts/editor/dark-theme.js'
import JavaClass from '@/scripts/editor/java-class.js'
import JsonWorker from '@/scripts/workers/json.worker.js'
import EditorWorker from '@/scripts/workers/editor.worker.js'

self.MonacoEnvironment = {
  getWorker: function(moduleId, label) {
    if (label === 'json') {
      return new JsonWorker()
    }
    return new EditorWorker()
  }
}
export default {
  name: 'MagicEditor',
  props: {
    config: {
      type: Object,
      required: true
    }
  },
  components: {
    MagicHeader,
    MagicStatusBar,
    MagicApiList,
    MagicFunctionList,
    MagicScriptEditor,
    MagicOptions,
    MagicLoading,
    MagicLogin,
    MagicDatasourceList
  },
  data() {
    return {
      loading: true,
      toolbars: ['接口', '函数', '数据源'],
      toolbarIndex: 0,
      toolboxWidth: 'auto', //工具条宽度
      themeStyle: {},
      showLogin: false,
      websocket: null,
      onLogin: () => {
        this.showLogin = false
        this.$refs.apiList.initData()
        this.$refs.functionList.initData()
        this.$refs.datasourceList.initData()
        bus.$emit('login');
      }
    }
  },
  beforeMount() {
    contants.BASE_URL = this.config.baseURL || ''
    contants.SERVER_URL = this.config.serverURL || ''
    let link = `${location.protocol}//${location.host}${location.pathname}`;
    if (contants.BASE_URL.startsWith('http')) { // http开头
      link = contants.BASE_URL
    } else if (contants.BASE_URL.startsWith('/')) { // / 开头的
      link = link + contants.BASE_URL
    } else {
      link = link + '/' + contants.BASE_URL
    }
    this.websocket = new MagicWebSocket(replaceURL(link.replace(/^http/, 'ws') + '/console'))
    contants.DEFAULT_EXPAND = this.config.defaultExpand !== false
    this.config.version = contants.MAGIC_API_VERSION_TEXT
    this.config.title = this.config.title || 'magic-api'
    this.config.themes = this.config.themes || {}
    this.config.defaultTheme = this.config.defaultTheme || 'default'
    this.config.header = this.config.header || {
      skin: true,
      document: true,
      repo: true,
      qqGroup: true
    }
    contants.AUTO_SAVE = this.config.autoSave !== false
    if (this.config.decorationTimeout !== undefined) {
      contants.DECORATION_TIMEOUT = this.config.decorationTimeout
    }
    this.config.request = this.config.request || {
      beforeSend: config => config,
      onError: err => Promise.reject(err)
    }
    this.config.response = this.config.response || {
      onSuccess: resp => resp,
      onError: err => Promise.reject(err)
    }
    request.setBaseURL(contants.BASE_URL)
    request.getAxios().interceptors.request.use(
      config => {
        if (this.config.request.beforeSend) {
          return this.config.request.beforeSend(config)
        }
        return config
      },
      err => {
        if (this.config.request.onError) {
          return this.config.request.onError(err)
        }
        return Promise.reject(err)
      }
    )
    request.getAxios().interceptors.response.use(
      resp => {
        if (this.config.response.onSuccess) {
          return this.config.response.onSuccess(resp)
        }
        return resp
      },
      err => {
        if (this.config.response.onError) {
          return this.config.response.onError(err)
        }
        return Promise.reject(err)
      }
    )
    defineTheme('default', defaultTheme)
    defineTheme('dark', darkTheme)
    Object.keys(this.config.themes || {}).forEach(themeKey => {
      defineTheme(themeKey, this.config.themes[themeKey])
    })
  },
  mounted() {
    if(this.config.blockClose !== false){
      window.onbeforeunload = () => '系统可能不会保存您所做的更改。'
    }
    this.bindKey()
    Promise.all([JavaClass.initClasses(), JavaClass.initImportClass(), this.loadConfig()])
      .then(e => {
        this.hideLoading()
        this.login()
        if (this.config.checkUpdate !== false) {
          this.checkUpdate()
        }
      })
      .catch(e => {
        this.hideLoading()
        this.$magicAlert({
          title: '加载失败',
          content: '请检查配置项baseURL是否正确！'
        })
      })
    bus.$on('search-open', item => {
      if (item.type === 1) {
        this.toolbarIndex = 0
      } else if (item.type === 2) {
        this.toolbarIndex = 1
      }
    })
    bus.$on('logout', () => this.showLogin = true)
    this.open()
  },
  destroyed() {
    bus.$off();
    Key.unbind();
    this.websocket.close();
  },
  methods: {
    // 隐藏loading
    hideLoading() {
      this.loading = false
      if (typeof hideMaLoading === 'function') {
        hideMaLoading()
      }
    },
    bindKey() {
      let element = this.$refs.container
      // 绑定保存快捷键
      Key.bind(element, Key.Ctrl | Key.S, () => bus.$emit('doSave'))
      // 绑定测试快捷键
      Key.bind(element, Key.Ctrl | Key.Q, () => bus.$emit('doTest'))
      // 绑定F8快捷键，恢复断点
      Key.bind(element, Key.F8, () => bus.$emit('doContinue'))
      // 绑定F6快捷键，进入下一步
      Key.bind(element, Key.F6, () => bus.$emit('doStepInto'))
    },
    async loadConfig() {
      request
          .execute({url: '/config.json'})
          .then(res => {
            contants.config = res.data
            // 如果在jar中引用，需要处理一下SERVER_URL
            if (this.config.inJar && location.href.indexOf(res.data.web) > -1) {
              let host = location.href.substring(0, location.href.indexOf(res.data.web))
              contants.SERVER_URL = replaceURL(host + '/' + (res.data.prefix || ''))
            }
          })
          .catch(e => {
            this.$magicAlert({
              title: '加载配置失败',
              content: (e.response.status || 'unknow') + ':' + (JSON.stringify(e.response.data) || 'unknow')
            })
          })
    },
    doResizeX() {
      let rect = this.$refs.resizer.getBoundingClientRect()
      let container = this.$refs.apiList
      if (this.toolbarIndex === 1) {
        container = this.$refs.functionList
      } else if (this.toolbarIndex === 2) {
        container = this.$refs.datasourceList
      }
      let width = container.$el.clientWidth
      document.onmousemove = e => {
        let move = e.clientX - rect.x + +width
        if (move >= 274 && move < 700) {
          this.toolboxWidth = move + 'px'
        }
      }
      document.onmouseup = () => {
        document.onmousemove = document.onmouseup = null
        this.$refs.resizer.releaseCapture && this.$refs.resizer.releaseCapture()
      }
      bus.$emit('update-window-size')
    },
    async login() {
      contants.HEADER_MAGIC_TOKEN_VALUE = store.get(contants.HEADER_MAGIC_TOKEN) || contants.HEADER_MAGIC_TOKEN_VALUE
      request.send('/login').success(isLogin => {
        if (isLogin) {
          this.onLogin()
        } else {
          this.showLogin = true
        }
      })
    },
    async checkUpdate() {
      fetch('https://img.shields.io/maven-metadata/v.json?label=maven-central&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Forg%2Fssssssss%2Fmagic-api%2Fmaven-metadata.xml')
          .then(response => {
            if (response.status === 200) {
              response.json().then(json => {
                if (contants.config.version !== json.value.replace('v', '')) {
                  if (json.value !== store.get(contants.IGNORE_VERSION)) {
                    this.$magicConfirm({
                      title: '更新提示',
                      content: `检测到已有新版本${json.value}，是否更新？`,
                      ok: '更新日志',
                      cancel: '残忍拒绝',
                      onOk: () => {
                        window.open('http://www.ssssssss.org/changelog.html')
                      },
                      onCancel: () => {
                        store.set(contants.IGNORE_VERSION, json.value)
                      }
                    })
                  }
                  bus.$emit('status', `版本检测完毕，最新版本为：${json.value},建议更新！！`)
                } else {
                  bus.$emit('status', `版本检测完毕，当前已是最新版`)
                }
              })
            } else {
              bus.$emit('status', '版本检测失败')
            }
          })
          .catch(ignore => {
            bus.$emit('status', '版本检测失败')
          })
    },
    /**
     * 传入id来打开对应api或者function
     */
    open(openIds) {
      openIds = openIds || getQueryVariable('openIds')
      if (openIds) {
        if (typeof openIds === 'string') {
          openIds = openIds.split(',')
        }
        openIds.forEach(id => {
          this.$refs.apiList.openItemById(id)
          this.$refs.functionList.openItemById(id)
        })
      }
    }
  },
  watch: {
    toolbarIndex() {
      bus.$emit('update-window-size')
    }
  }
}
</script>
<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.ma-main-container {
  position: absolute;
  top: 30px;
  left: 22px;
  bottom: 24px;
  right: 0px;
  display: flex;
  flex-direction: column;
}

.ma-middle-container {
  flex: 1;
  display: flex;
  overflow: auto;
  background: var(--middle-background);
  border-bottom: 1px solid var(--border-color);
}

.ma-toolbar-container {
  background: var(--background);
  border-right: 1px solid var(--toolbox-border-color);
  width: 22px;
  position: absolute;
  left: 0;
  bottom: 24px;
  top: 30px;
}

.ma-toolbar-container > li {
  padding: 6px 3px;
  cursor: pointer;
  letter-spacing: 2px;
  text-align: center;
  color: var(--color);
  border-bottom: 1px solid var(--toolbox-border-color)
}

.ma-toolbar-container > li > i {
  color: var(--icon-color);
  font-size: 14px;
  padding-top: 3px;
  display: inline-block;
}

.ma-toolbar-container > li:hover {
  background: var(--hover-background);
}

.ma-toolbar-container > li.selected {
  background: var(--selected-background);
  color: var(--selected-color);
}

.ma-resizer-x {
  float: left;
  width: 10px;
  height: 100%;
  margin-left: -5px;
  background: none;
  cursor: e-resize;
  z-index: 1000;
}
</style>
