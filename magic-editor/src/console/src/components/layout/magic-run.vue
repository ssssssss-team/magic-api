<template>
  <div class="ma-run" style="display: flex; flex-direction: row;">
    <div style="width: 40%" v-show="!contentType">
      <div ref="resultEditor" class="ma-body-editor"></div>
    </div>
    <div style="flex: 1;" v-show="!contentType">
      <magic-json :jsonData="responseBody || []" :forceUpdate="forceUpdate" :height="layoutHeight" type="response"></magic-json>
    </div>
    <iframe v-if="contentType" class="ma-response-body-container" style="padding:5px;" :src="this.objectUrl">
    </iframe>
  </div>
</template>

<script>

import MagicJson from '@/components/common/magic-json.vue'
import bus from '@/scripts/bus.js'
import * as monaco from 'monaco-editor'
import store from '@/scripts/store.js'
import {isVisible} from '@/scripts/utils.js'
import {parseJson} from '@/scripts/parsing/parser.js'

export default {
  name: 'MagicRun',
  props: {
    info: Object,
  },
  components: {
    MagicJson
  },
  data() {
    return {
      resultEditor: null,
      responseBody: [],
      forceUpdate: false,
      layoutHeight: '255px',
      contentType: '',
      objectUrl: null
    }
  },
  watch: {
    responseBody: {
      handler(responseBodyArr) {
        this.info.responseBodyDefinition = responseBodyArr[0]
      },
      deep: true
    }
  },
  updated() {
    this.updateSize()
  },
  mounted() {
    this.createEditor()
    bus.$on('update-response-body', (responseBody) => {
      this.contentType = null
      if(this.objectUrl){
        URL.revokeObjectURL(this.objectUrl)
        this.objectUrl = null
      }
      this.resultEditor && this.resultEditor.setValue(responseBody || '')
      this.updateResponseBody(responseBody)
    })
    bus.$on('update-response-body-definition', (responseBodyDefinition) => {
      this.responseBody = responseBodyDefinition ? [responseBodyDefinition] : []
    })
    bus.$on('update-response-blob',(contentType, blob) => {
      this.contentType = contentType
      this.objectUrl = URL.createObjectURL(blob)
    })
  },
  methods: {
    createEditor() {
      this.resultEditor = monaco.editor.create(this.$refs.resultEditor, {
        minimap: {
          enabled: false,
        },
        language: 'json',
        fixedOverflowWidgets: true,
        folding: true,
        readOnly: true, //只读模式
        lineDecorationsWidth: 35,
        wordWrap: 'on',
        value: this.info.responseBody || '',
        theme: store.get('skin') || 'default'
      })
      bus.$on('update-window-size', () => this.updateSize())
    },
    layout() {
      this.$nextTick(() => {
        if (this.resultEditor && isVisible(this.$refs.resultEditor)) {
          this.resultEditor.layout()
          if (this.$refs.resultEditor && this.$refs.resultEditor.firstChild) {
            this.layoutHeight = this.$refs.resultEditor.firstChild.style.height
          }
        }
      })
    },
    updateSize() {
      if (this.resultEditor == null) {
        this.createEditor()
      }
      this.layout()
    },
    updateResponseBody(bodyStr) {
        if (['{}','[]'].indexOf(bodyStr.replace(/\s/g,"")) > -1) {
          this.responseBody = []
          return false
        }
        let ret = parseJson(bodyStr)
        if(ret){
          this.responseBody = ret;
          this.forceUpdate = !this.forceUpdate
        }
    },
    processBody(body, level) {
      let arr = [], that = this
      Object.keys(body).forEach((key) => {
        let param = {
          name: 'Array' !== this.getType(body) ? key : 'Array' === this.getType(body) && 'Object' !== that.getType(body[key]) ? key : '',
          value: 'Object' !== that.getType(body[key]) && 'Array' !== that.getType(body[key]) ? body[key] : '',
          dataType: this.getType(body[key]),
          validateType: '',
          expression: '',
          error: '',
          description: '',
          children: [],
          level: level + 1,
          selected: false
        }
        if ('Object' === that.getType(body[key]) || 'Array' === that.getType(body[key])) {
          param.children = that.processBody('Array' === that.getType(body[key]) ? deepClone([body[key][0]]) : body[key], level + 1);
        }
        arr.push(param)

      })
      return arr;
    },
    getType(object) {
      if (Object.prototype.toString.call(object) === '[object Number]') {
        if(object.toString().indexOf('.') !==-1 && parseInt(object) !== parseFloat(object)) {
          return "Double";
        }
        // if (object >= -128 && object <= 127) {
        //   return "Byte";
        // }
        // if (object >= -32768 && object <= 32767) {
        //   return "Short";
        // }
        if (object >= -2147483648 && object <= 2147483647) {
          return "Integer";
        }
        return "Long";
      }
      if (Object.prototype.toString.call(object) === '[object String]') {
        return "String";
      }
      if (Object.prototype.toString.call(object) === '[object Boolean]') {
        return "Boolean";
      }
      if (Object.prototype.toString.call(object) === '[object Array]') {
        return "Array";
      }
      if (Object.prototype.toString.call(object) === '[object Object]') {
        return "Object";
      }
      return "String";
    },
    valueCopy(newBody, oldBody, arrayFlag = false) {
      if (oldBody.length == 0) {
        return newBody
      }
      let that = this;
      newBody.map(item => {
        let oldItemArr = oldBody.filter(old => {
          if (old.level === 0 || arrayFlag) {
            return old
          }
          return old.name === item.name
        })
        if (oldItemArr.length > 0) {
          if (item.dataType === 'Object' || item.dataType === 'Array') {
            item.children = that.valueCopy(item.children, oldItemArr[0].children, item.dataType === 'Array')
          } else {
            item.expression = oldItemArr[0].expression
          }
          item.name = oldItemArr[0].name
          item.description = oldItemArr[0].description
        }

      })
      return deepClone(newBody);
    }
  },
  destroyed() {
    if (this.resultEditor) {
      this.resultEditor.dispose()
    }
  }
}
</script>

<style scoped>
div.ma-run{
  background: var(--background);
  height: 100%;
  width: 100%;
  position: relative;
}

div.ma-run > div {
  width: 100%;
  height: 100%;
}
.ma-body-editor {
  width: 100%;
  height: 100%;
}
.ma-response-body-container{
  width: 100%;
  height: 100%;
  border: none;
}
</style>
