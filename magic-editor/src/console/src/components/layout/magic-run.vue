<template>
  <div class="ma-run" style="display: flex; flex-direction: row;">
    <div style="width: 40%">
      <div ref="resultEditor" class="ma-body-editor"></div>
    </div>
    <div style="flex: 1;">
      <magic-json :jsonData="responseBody || []" :forceUpdate="forceUpdate" :height="layoutHeight" type="response"></magic-json>
    </div>

  </div>
</template>

<script>

  import MagicJson from '@/components/common/magic-json.vue'
import bus from '@/scripts/bus.js'
import * as monaco from 'monaco-editor'
import store from '@/scripts/store.js'
import {isVisible, deepClone} from '@/scripts/utils.js'

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
      layoutHeight: '255px'
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
      this.resultEditor && this.resultEditor.setValue(responseBody || '')
      this.updateResponseBody(responseBody)
    })
    bus.$on('update-response-body-definition', (responseBodyDefinition) => {
      this.responseBody = responseBodyDefinition ? [responseBodyDefinition] : []
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
      try {
        let body = JSON.parse(bodyStr)
        let reqBody = []
        reqBody.push({
          name: '',
          value: '',
          dataType: this.getType(body),
          validateType: '',
          expression: '',
          error: '',
          description: '',
          children: this.processBody(body, 0),
          level: 0,
          selected: this.responseBody.length <= 0
        })

        this.responseBody = this.valueCopy(reqBody, this.responseBody)
        this.forceUpdate = !this.forceUpdate;
      } catch (e) {
        // console.error(e)
      }
    },
    processBody(body, level) {
      let arr = [], that = this
      Object.keys(body).forEach((key) => {
        let param = {
          name: 'Array' !== this.getType(body) ? key : '',
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
          param.children = that.processBody(body[key], level + 1);
        }
        arr.push(param)

      })
      return arr;
    },
    getType(object) {
      if (Object.prototype.toString.call(object) === '[object Number]') {
        if(parseInt(object) !== parseFloat(object)) {
          return "Float";
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
div.ma-run {
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
</style>
