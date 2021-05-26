<template>
  <div class="ma-editor-container">
    <div class="ma-wrapper">
      <ul ref="scrollbar" class="ma-tab not-select">
        <li
            v-for="(item, index) in scripts"
            :key="'api_' + item.tmp_id || item.id"
            :class="{ selected: selected === item }"
            :title="item.name"
            @click="open(item)" @contextmenu.prevent="e => tabsContextmenuHandle(e, item, index)"
        >
          <i class="ma-svg-icon" v-if="item._type === 'api'" :class="['request-method-' + item.method]" />
          <i class="ma-svg-icon" v-if="item._type !== 'api'" :class="['icon-function']" />
          {{item.name}}
          <i class="ma-icon ma-icon-close" @click.stop="close(item.id || item.tmp_id)"/>
        </li>
      </ul>
    </div>

    <div ref="editor"></div>
    <div v-show="!scripts.length" class="ma-empty-container">
      <div class="ma-hot-key">
        <p>
          保存<em>Ctrl + S</em><br/>
          测试<em>Ctrl + Q</em><br/>
          代码提示<em>Alt + /</em><br/>
          恢复断点<em>F8</em><br/>
          步进<em>F6</em>
        </p>
      </div>
    </div>

    <magic-dialog :moveable="false" :title="'历史记录：' + (info && info.name)" :value="showHsitoryDialog"
                  align="right" height="750px" maxWidth="inherit" padding="none" width="80%"
                  @onClose="showHsitoryDialog = false">
      <template #content>
        <magic-history ref="history"/>
      </template>
      <template #buttons>
        <button
            class="ma-button active"
            @click="
            () => {
              $refs.history.reset()
              showHsitoryDialog = false
            }
          "
        >
          恢复
        </button>
        <button class="ma-button" @click="showHsitoryDialog = false">关闭</button>
      </template>
    </magic-dialog>
    <magic-dialog :value="showImageDialog" title="图片结果" @onClose="showImageDialog = false">
      <template #content>
        <p align="center"><img :src="imageUrl"/></p>
      </template>
      <template #buttons>
        <button class="ma-button" @click="showImageDialog = false">OK</button>
      </template>
    </magic-dialog>
  </div>
</template>

<script>
import * as monaco from 'monaco-editor'
import {initializeMagicScript} from '@/scripts/editor/magic-script.js'
import bus from '@/scripts/bus.js'
import MagicDialog from '@/components/common/modal/magic-dialog.vue'
import MagicHistory from './magic-history.vue'
import request from '@/api/request.js'
import contants from '@/scripts/contants.js'
import * as utils from '@/scripts/utils.js'
import store from '@/scripts/store.js'
import {Parser} from '@/scripts/parsing/parser.js'
import tokenizer from '@/scripts/parsing/tokenizer.js'
import {TokenStream} from '@/scripts/parsing/index.js'
import RequestParameter from '@/scripts/editor/request-parameter.js';

export default {
  name: 'MagicScriptEditor',
  components: {
    MagicDialog,
    MagicHistory
  },
  data() {
    return {
      scripts: [],
      selected: null,
      info: null,
      editor: null,
      showImageDialog: false,
      imageUrl: '',
      showHsitoryDialog: false
    }
  },
  mounted() {
    let scrollbar = this.$refs.scrollbar
    let handler = e => {
      if (scrollbar.contains(e.target)) {
        let delta = e.wheelDelta || e.detail
        scrollbar.scrollLeft += delta > 0 ? -100 : 100
      }
    }
    document.addEventListener('DOMMouseScroll', handler, false)
    document.addEventListener('mousewheel', handler, false)
    initializeMagicScript()
    this.editor = monaco.editor.create(this.$refs.editor, {
      minimap: {
        enabled: false
      },
      language: 'magicscript',
      folding: true,
      lineDecorationsWidth: 35,
      wordWrap: 'on',
      theme: store.get('skin') || 'default'
    })
    this.editor.addAction({
      id: 'editor.action.triggerSuggest.extension',
      label: '触发代码提示',
      precondition: '!suggestWidgetVisible && !markersNavigationVisible && !parameterHintsVisible && !findWidgetVisible',
      run: () => {
        this.editor.trigger(null, 'editor.action.triggerSuggest', {})
      }
    })
    this.editor.addCommand(
        monaco.KeyMod.Alt | monaco.KeyCode.US_SLASH,
        () => {
          let triggerParameterHints = this.editor.getAction('editor.action.triggerParameterHints')
          let triggerSuggest = this.editor.getAction('editor.action.triggerSuggest.extension')
          triggerParameterHints.run().then(() => {
            setTimeout(() => {
              if (triggerSuggest.isSupported()) {
                triggerSuggest.run()
              }
            }, 0)
          })
        },
        '!findWidgetVisible && !inreferenceSearchEditor && !editorHasSelection'
    )
    this.editor.onMouseDown(e => {
      if (e.target.element.classList.contains('codicon')) {
        return
      }
      if (e.target.detail && e.target.detail.offsetX && e.target.detail.offsetX >= 0 && e.target.detail.offsetX <= 90) {
        var line = e.target.position.lineNumber
        if (
            this.editor
                .getModel()
                .getLineContent(line)
                .trim() === ''
        ) {
          return
        }
        let decorations = this.editor.getLineDecorations(line)
        let decoration = decorations.filter(it => it.options.linesDecorationsClassName === 'breakpoints')
        if (decoration && decoration.length > 0) {
          this.editor.getModel().deltaDecorations([decoration[0].id], [])
        } else {
          this.editor.getModel().deltaDecorations(
              [],
              [
                {
                  range: new monaco.Range(line, 1, line, 1),
                  options: {
                    isWholeLine: true,
                    linesDecorationsClassName: 'breakpoints',
                    className: 'breakpoint-line'
                  }
                }
              ]
          )
        }
        this.info.ext.decorations = this.editor.getModel().getAllDecorations()
      }
    })
    this.editor.onDidChangeModelContent(() => {
      if (this.info) {
        this.info.script = this.editor.getValue()
        if (this.timeout) {
          clearTimeout(this.timeout)
        }
        this.timeout = setTimeout(() => this.doValidate(), 500)
      }
    })
    window.onresize = () => bus.$emit('update-window-size')
    bus.$on('update-window-size', this.layout)
    bus.$on('open', this.open)
    bus.$on('changed', this.changed)
    bus.$on('doSave', this.doSave)
    bus.$on('viewHistory', this.viewHistory)
    bus.$on('doTest', this.doTest)
    bus.$on('doContinue', this.doContinue)
    bus.$on('doStepInto', this.doStepInto)
    bus.$on('logout', this.closeAll)
    bus.$on('ready-delete', () => {
      if (this.info) {
        bus.$emit('delete-api', this.info)
      }
    })
    let javaTypes = {
      'String': 'java.lang.String',
      'Integer': 'java.lang.Integer',
      'Double': 'java.lang.Double',
      'Long': 'java.lang.Long',
      'Byte': 'java.lang.Byte',
      'Short': 'java.lang.Short',
      'Float': 'java.lang.Float',
      'MultipartFile': 'org.springframework.web.multipart.MultipartFile',
      'MultipartFiles': 'java.util.List',
    }
    RequestParameter.setEnvironment(() => {
      let env = {};
      if (this.info && this.info._type === 'api') {
        this.info.parameters.forEach(it => {
          env[it.name] = javaTypes[it.dataType || 'String'] || 'java.lang.Object';
        });
        this.info.paths.forEach(it => {
          env[it.name] = javaTypes[it.dataType || 'String'] || 'java.lang.Object';
        });
      }
      return env;
    })
  },
  methods: {
    doValidate() {
      try {
        let parser = new Parser(new TokenStream(tokenizer(this.editor.getValue())))
        parser.parse()
        monaco.editor.setModelMarkers(this.editor.getModel(), 'validate', [{}])
      } catch (e) {
        if (e.span) {
          let line = e.span.getLine()
          monaco.editor.setModelMarkers(this.editor.getModel(), 'validate', [
            {
              startLineNumber: line.lineNumber,
              endLineNumber: line.endLineNumber,
              startColumn: line.startCol,
              endColumn: line.endCol,
              message: e.message,
              severity: monaco.MarkerSeverity.Error
            }
          ])
        }
      }
    },
    layout() {
      this.$nextTick(() => {
        if (utils.isVisible(this.$refs.editor)) {
          this.$nextTick(() => this.editor.layout())
        }
      })
    },
    open(item) {
      if (item.delete) {
        this.close(item.id || item.tmp_id)
        return
      }
      let id = item.id
      this.selected = item
      let isNew = id === '' && !this.scripts.some(it => it.tmp_id === item.tmp_id);
      let isApi = item._type === 'api';
      let info = this.scripts.filter(it => it.id === id)[0]
      if (this.info) {
        this.info.ext.scrollTop = this.editor.getScrollTop();
      }
      if (!item.ext) {
        this.$set(item, 'ext', {
          logs: [],
          debuging: false,
          sessionId: '',
          variables: [],
          decorations: [],
          debugDecorations: [],
          debugDecoration: null,
          save: true,
          loading: false,
          scrollTop: 0
        })
      }
      if (item.ext.loading) {
        return;
      }
      if (item.copy !== true && (info || isNew)) {
        if (isNew) {
          if (isApi) {
            item.headers = item.headers || []
            item.option = item.option || []
            item.paths = item.paths || []
            item.requestBody = item.requestBody || '';
            item.method = contants.API_DEFAULT_METHOD
          }
          item.parameters = item.parameters || []
          item.ext.save = false
          this.scripts.push(item)
        } else if (info) {
          item = info
          this.selected = item
        }
        this.info = item
        this.editor.setValue(item.script || '')
        this.editor.getModel().deltaDecorations([], item.ext.decorations)
        this.editor.setScrollTop(item.ext.scrollTop);
        if (item.ext.debugDecoration) {
          item.ext.debugDecorations = this.editor.getModel().deltaDecorations([], [item.ext.debugDecoration])
        }
        bus.$emit('opened', item)
      } else {
        let process = data => {
          if (!data) {
            return data
          }
          if (Array.isArray(data)) {
            return data
          }
          let array = []
          for (let key in data) {
            array.push({
              name: key,
              value: data[key] === null ? '' : data[key].toString()
            })
          }
          return array
        }
        item.ext.loading = true;
        request.send(`/${isApi ? '' : 'function/'}get?id=${id}`).success(data => {
          if (isApi) {
            if (!Array.isArray(item.parameters)) {
              // v0.5.0以下版本处理
              item.option = process(JSON.parse(data.option || '[]'))
            } else {
              item.option = JSON.parse(data.option || '[]')
            }
            item.parameters = data.parameters;
            item.headers = data.headers;
            item.paths = data.paths;
            item.responseHeader = JSON.parse(data.responseHeader || '[]')
            item.responseBody = data.responseBody
            item.method = data.method
          }
          item.script = data.script
          item.description = data.description
          if (item.copy === true) {
            item.id = ''
            item.copy = false
          }
          this.scripts.push(item)
          this.info = item
          this.editor.setValue(item.script)
          this.editor.setScrollTop(item.ext.scrollTop);
          bus.$emit('opened', item)
        }).end(() => {
          item.ext.loading = false;
        })
      }
      this.layout()
    },
    deleteWrapperProperties(obj){
      delete obj.ext
      delete obj.groupName
      delete obj.groupPath
      delete obj._type
      delete obj.level
      delete obj.tmp_id
    },
    doSaveApi() {
      this.info.headers = this.info.headers || []
      let thisInfo = this.info
      let saveObj = {...this.info}
      this.deleteWrapperProperties(saveObj)
      delete saveObj.optionMap
      delete saveObj.responseHeader
      delete saveObj.running
      // saveObj.responseHeader = JSON.stringify(saveObj.responseHeader)
      saveObj.parameters = saveObj.parameters.filter(it => it.name)
      saveObj.paths = saveObj.paths.filter(it => it.name)
      saveObj.headers = saveObj.headers.filter(it => it.name)
      saveObj.option = JSON.stringify(saveObj.option)
      // saveObj.requestHeader = JSON.stringify(saveObj.requestHeader.filter(it => it.name))
      return request.send('/save', JSON.stringify(saveObj), {
        method: 'post',
        headers: {
          'Content-Type': 'application/json'
        },
        transformRequest: []
      }).success(id => {
        if (saveObj.id) {
          bus.$emit('script_save')
        } else {
          bus.$emit('script_add')
        }
        thisInfo.id = id
      })
    },
    doSaveFunction() {
      let thisInfo = this.info
      let saveObj = {...this.info}
      this.deleteWrapperProperties(saveObj)
      saveObj.parameters = saveObj.parameters.filter(it => it.name)
      return request.send('/function/save', JSON.stringify(saveObj), {
        method: 'post',
        headers: {
          'Content-Type': 'application/json'
        },
        transformRequest: []
      }).success(id => {
        if (saveObj.id) {
          bus.$emit('function_save')
        } else {
          bus.$emit('function_add')
        }
        thisInfo.id = id
      })
    },
    doSave() {
      if (this.selected) {
        if (this.info._type === 'api') {
          return this.doSaveApi();
        } else {
          return this.doSaveFunction();
        }
      }
    },
    doTest() {
      if (!this.selected) {
        this.$magicAlert({
          content: '请打开接口在执行测试'
        })
      } else {
        bus.$emit('switch-tab','request')
        if (this.info.running || this.info._type !== 'api') {
          return
        }
        if (contants.AUTO_SAVE) {
          // 自动保存
          let resp = this.doSave()
          resp && resp.end(() => this.internalTest())
        } else {
          this.internalTest()
        }
      }
    },
    internalTest() {
      this.editor.deltaDecorations(this.editor.getModel().getAllDecorations().filter(it => it.options.inlineClassName === 'squiggly-error').map(it => it.id), [])
      bus.$emit('status', '开始测试...')
      this.$set(this.info, 'running', true)
      let requestConfig = {
        baseURL: contants.SERVER_URL,
        url: utils.replaceURL('/' + this.info.groupPath + '/' + this.info.path),
        method: this.info.method,
        headers: {},
        responseType: 'json',
        withCredentials: true
      }
      this.info.paths.filter(it => it.value && it.value.trim()).forEach(it => {
        requestConfig.url = requestConfig.url.replace(new RegExp(`\{${it.name}\}`,"g"),it.value.trim())
      })
      // 先处理接口的路径变量，在处理分组的路径变量，顺序递归向上
      const groups = this.$parent.$refs.apiList.getGroupsById(this.info.groupId)
      groups.filter(group => group.paths && group.paths.length > 0).forEach(group => {
        group.paths.filter(it => it.value && it.value.trim()).forEach(it => {
          requestConfig.url = requestConfig.url.replace(new RegExp(`\{${it.name}\}`,"g"),it.value.trim())
        })
      })
      if(requestConfig.url.indexOf('{') > -1){
        this.$magicAlert({
          content: '请填写路径变量后在测试！'
        })
        this.$set(this.info, 'running', false)
        return;
      }
      this.info.headers
          .filter(it => it.name)
          .forEach(it => {
            requestConfig.headers[it.name] = it.value
          })
      let params = {}
      this.info.parameters
          .filter(it => it.name)
          .forEach(it => {
            params[it.name] = it.value
          })
      requestConfig.headers['Content-Type'] = 'application/x-www-form-urlencoded'
      if (requestConfig.method !== 'POST' || this.info.requestBody) {
        requestConfig.params = params
      } else {
        requestConfig.data = params
      }
      if (this.info.requestBody) {
        try {
          JSON.parse(this.info.requestBody)
          requestConfig.params = params
          requestConfig.data = this.info.requestBody
          requestConfig.headers['Content-Type'] = 'application/json'
          requestConfig.transformRequest = []
        } catch (e) {
          this.$magicAlert({
            content: 'RequestBody 参数有误，请检查！'
          })
          this.$set(this.info, 'running', false)
          return
        }
      }
      const info = this.info
      info.ext.eventSource = request.createConsole()
      info.ext.eventSource.addEventListener('create', e => {
        bus.$emit('report', 'run')
        this.$nextTick(() => this.sendTestRequest(info, requestConfig, e.data))
      })
      info.ext.eventSource.addEventListener('log', e => {
        let row = JSON.parse(e.data)
        row.timestamp = utils.formatDate(new Date())
        let throwable = row.throwable;
        delete row.throwable;
        info.ext.logs.push(row)
        if (throwable) {
          let messages = throwable.replace(/ /g, '&nbsp;').split('\n');
          for (let i = 0; i < messages.length; i++) {
            info.ext.logs.push({
              level: row.level,
              message: messages[i],
              throwable: true
            })
          }
        }

      })
      info.ext.eventSource.addEventListener('close', e => {
        info.ext.eventSource.close()
      })
    },
    viewHistory() {
      if (!this.selected) {
        return
      }
      if (!this.info.id) {
        this.$magicAlert({
          content: '当前是新增脚本,无法查看历史记录'
        })
        return
      }
      let url = `backups?id=${this.info.id}`;
      let isApi = this.info._type === 'api';
      if (!isApi) {
        url = 'function/' + url;
      }
      request.send(url).success(timestampes => {
        if (timestampes && timestampes.length > 0) {
          this.$refs.history.load(timestampes, this.info, this.editor, isApi)
          this.showHsitoryDialog = true
        } else {
          this.$magicAlert({
            title: '历史记录',
            content: '当前脚本无历史记录'
          })
        }
      })
    },
    doContinue(step) {
      if (!this.selected) {
        return
      }
      let target = this.info
      if (target.ext.debuging) {
        target.ext.debuging = false
        target.ext.variables = []
        let requestConfig = {
          ...target.ext.requestConfig
        }
        delete requestConfig.data
        delete requestConfig.params
        requestConfig.headers[contants.HEADER_REQUEST_CONTINUE] = true
        requestConfig.headers[contants.HEADER_REQUEST_STEP_INTO] = step === true
        this.sendTestRequest(target, requestConfig, target.ext.sessionId)
      }
    },
    doStepInto() {
      this.doContinue(true)
    },
    mergeGlobalSettings(requestConfig) {
      let parameters = JSON.parse(store.get('global-parameters') || '[]')
      let headers = JSON.parse(store.get('global-headers') || '[]')
      headers
          .filter(it => it.name)
          .forEach(item => {
            requestConfig.headers[item.name] = requestConfig.headers[item.name] || item.value
          })
      parameters
          .filter(it => it.name)
          .forEach(item => {
            if (requestConfig.params) {
              requestConfig.params[item.name] = requestConfig.params[item.name] || item.value
            } else {
              requestConfig.data[item.name] = requestConfig.data[item.name] || item.value
            }
          })
    },
    sendTestRequest(target, requestConfig, sessionId) {
      bus.$emit('switch-tab', 'log')
      target.ext.requestConfig = requestConfig
      target.ext.sessionId = sessionId
      requestConfig.headers[contants.HEADER_REQUEST_SESSION] = sessionId
      requestConfig.headers[contants.HEADER_MAGIC_TOKEN] = contants.HEADER_MAGIC_TOKEN_VALUE
      this.mergeGlobalSettings(requestConfig)
      requestConfig.headers[contants.HEADER_REQUEST_BREAKPOINTS] = this.editor
          .getModel()
          .getAllDecorations()
          .filter(it => it.options.linesDecorationsClassName === 'breakpoints')
          .map(it => it.range.startLineNumber)
          .join(',')
      request
          .execute(requestConfig)
          .then(res => {
            target.ext.debugDecorations && this.editor.deltaDecorations(target.ext.debugDecorations, [])
            target.ext.debugDecorations = target.ext.debugDecoration = null
            if (res.headers[contants.HEADER_RESPONSE_WITH_MAGIC_API] === 'true') {
              let data = res.data
              if (data.code === contants.RESPONSE_CODE_SCRIPT_ERROR) {
                bus.$emit('report', 'script_error')
                bus.$emit('status', '脚本执行出错..')
                // 脚本执行出错
                target.ext.debuging = target.running = false
                if (data.body) {
                  let line = data.body
                  if (this.info.id === target.id) {
                    let range = new monaco.Range(line[0], line[2], line[1], line[3] + 1)
                    let decorations = this.editor.deltaDecorations(
                        [],
                        [
                          {
                            range,
                            options: {
                              hoverMessage: {
                                value: data.message
                              },
                              inlineClassName: 'squiggly-error'
                            }
                          }
                        ]
                    )
                    this.editor.revealRangeInCenter(range)
                    this.editor.focus()
                    if(contants.DECORATION_TIMEOUT >= 0){
                      setTimeout(() => this.editor.deltaDecorations(decorations, []), contants.DECORATION_TIMEOUT)
                    }
                  }
                }
                target.responseBody = utils.formatJson(data.data)
                bus.$emit('switch-tab', 'result')
                bus.$emit('update-response-body', target.responseBody)
                target.ext.eventSource.close();
              } else if (data.code === contants.RESPONSE_CODE_DEBUG) {
                bus.$emit('report', 'debug_in')
                bus.$emit('status', '进入断点...')
                // 进入断点
                target.ext.debuging = true

                target.ext.variables = data.body.variables
                let range = data.body.range
                let decoration = {
                  range: new monaco.Range(range[0], 1, range[0], 1),
                  options: {
                    isWholeLine: true,
                    inlineClassName: 'debug-line',
                    className: 'debug-line'
                  }
                }
                target.ext.debugDecoration = decoration
                target.ext.debugDecorations = [this.editor.deltaDecorations([], [decoration])]
                bus.$emit('switch-tab', 'debug')
              } else {
                bus.$emit('status', '脚本执行完毕')
                // 执行完毕
                target.running = false
                bus.$emit('switch-tab', 'result')

                let contentType = res.headers[contants.HEADER_RESPONSE_MAGIC_CONTENT_TYPE]
                if (contentType === contants.HEADER_APPLICATION_STREAM) {
                  // 下载
                  var disposition = res.headers[contants.HEADER_CONTENT_DISPOSITION]
                  var filename = 'output'
                  if (disposition) {
                    filename = decodeURIComponent(disposition.substring(disposition.indexOf('filename=') + 9))
                  }
                  target.responseBody = utils.formatJson({filename})
                  bus.$emit('update-response-body', target.responseBody)
                  let a = document.createElement('a')
                  a.download = filename
                  let bstr = atob(data.data)
                  let n = bstr.length
                  let u8arr = new Uint8Array(n)
                  while (n--) {
                    u8arr[n] = bstr.charCodeAt(n)
                  }
                  a.href = window.URL.createObjectURL(new Blob([u8arr]))
                  a.click()
                  bus.$emit('report', 'output_blob')
                } else if (contentType && contentType.indexOf('image') === 0) {
                  // 图片
                  this.imageUrl = `data:${contentType};base64,${data.data}`
                  this.showImageDialog = true
                  target.responseBody = utils.formatJson(data.data)
                  bus.$emit('update-response-body', target.responseBody)
                  bus.$emit('report', 'output_image')
                } else if (data.code === contants.RESPONSE_NO_PERMISSION) {
                  this.$magicAlert({
                    title: '无权限',
                    content: '您没有权限执行测试'
                  })
                } else {
                  target.responseBody = utils.formatJson(data.data)
                  bus.$emit('update-response-body', target.responseBody)
                }
                target.ext.eventSource.close();
              }
            } else {
              target.ext.debuging = target.running = false
              bus.$emit('switch-tab', 'result')
              bus.$emit('status', '脚本执行完毕');
              // TODO 对于拦截器返回的会有警告，暂时先屏蔽掉。
              // this.$magicAlert({
              //   title: '警告',
              //   content: '检测到结果异常，请检查！'
              // })
              try {
                target.ext.eventSource.close();
                target.responseBody = utils.formatJson(res.data)
                bus.$emit('update-response-body', target.responseBody)
              } catch (ignored) {
              }
            }
          })
          .catch(error => {
            target.ext.debuging = target.running = false
            target.ext.eventSource.close();
            request.processError(error)
          })
    },
    close(id) {
      this.scripts.forEach((item, index) => {
        if (item.id === id || item.tmp_id === id) {
          this.scripts.splice(index, 1)
          if (this.selected === item) {
            let info
            if (index > 0) {
              info = this.scripts[index - 1]
            } else if (this.scripts.length > 0) {
              info = this.scripts[0]
            } else {
              this.selected = null
              return
            }
            this.open(info)
          }
        }
      })
      if (this.scripts.length === 0) {
        bus.$emit('opened', {empty: true})
      }
    },
    closeAll() {
      let items = [...this.scripts]
      items.forEach(element => {
        this.close(element.id || element.tmp_id)
      })
    },
    changed(info) {
      if (info && info === this.selected) {
        let index = -1
        this.scripts.forEach((item, i) => {
          if (item.id === info.id) {
            index = i
          }
        })
        if (index > -1) {
          this.scripts[index] = info
        }
      }
    },
    // tab右键菜单
    tabsContextmenuHandle(event, item, index) {
      this.$magicContextmenu({
        menus: [
          {
            label: '关闭',
            onClick: () => {
              this.close(item.id || item.tmp_id)
            }
          },
          {
            label: '关闭其他',
            divided: true,
            onClick: () => {
              let id = item.id || item.tmp_id
              let items = [...this.scripts]
              items.forEach(element => {
                let oid = element.id || element.tmp_id
                if (id !== oid) {
                  this.close(oid)
                }
              })
            }
          },
          {
            label: '关闭左侧',
            onClick: () => {
              let items = [...this.scripts]
              for (let i = 0; i < index; i++) {
                this.close(items[i].id || items[i].tmp_id)
              }
            }
          },
          {
            label: '关闭右侧',
            divided: true,
            onClick: () => {
              let items = [...this.scripts]
              for (let i = index + 1; i < items.length; i++) {
                this.close(items[i].id || items[i].tmp_id)
              }
            }
          },
          {
            label: '全部关闭',
            onClick: () => {
              this.closeAll()
            }
          }
        ],
        event,
        zIndex: 9999
      })
    }
  }
}
</script>

<style scoped>
.ma-editor-container {
  flex: 1;
  height: 100%;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  margin-left: -5px;
  position: relative;
}

.ma-wrapper {
  height: 30px;
  line-height: 30px;
  width: 100%;
  overflow: hidden;
  flex: none !important;
  border-bottom: 1px solid var(--tab-bar-border-color);
}

.ma-wrapper .ma-tab .ma-icon:first-child {
  font-size: 16px;
  padding-right: 3px;
}
.ma-wrapper .ma-tab .ma-svg-icon{
  height: 16px;
  margin-left: 0;
}

.ma-hot-key {
  position: absolute;
  top: 50%;
  margin-top: -60px;
  text-align: center;
  color: var(--empty-color);
  font-size: 16px;
  width: 100%;
}

.ma-hot-key p {
  display: inline-block;
  text-align: left;
  line-height: 30px;
}

.ma-hot-key p em {
  margin-left: 15px;
  font-style: normal;
  color: var(--empty-key-color);
}

.ma-empty-container {
  flex: none !important;
  position: absolute;
  z-index: 2;
  width: 100%;
  height: 100%;
  background: var(--empty-background);
}

ul {
  width: 100%;
  overflow: hidden;
  flex-wrap: nowrap;
  white-space: nowrap;
  list-style-type: none;
  display: flex;
  align-items: center;
  background: var(--background);
}

ul li {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 30px;
  line-height: 33px;
  background: var(--background);
  padding: 0 10px;
  border-bottom: 3px solid transparent;
  color: var(--color);
}

ul li.selected {
  border-bottom: 3px solid #4083c9;
  color: var(--selected-color);
}

ul li:hover {
  background: var(--hover-background);
}

ul li i {
  color: var(--icon-color);
  margin-left: 5px;
  font-size: 0.5em;
}

.ma-editor-container > div {
  flex: 1;
}
</style>
