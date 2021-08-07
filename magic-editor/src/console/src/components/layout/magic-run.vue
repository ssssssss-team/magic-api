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
import {isVisible, deepClone, download as downloadFile} from '@/scripts/utils.js'
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
    bus.$on('update-response-blob',(contentType, blob, headers) => {
      this.contentType = contentType
      let disposition = headers['content-disposition'];
      if(disposition){
        try {
          let filename = disposition.replace(/.*filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/, '$1')
          downloadFile(blob, decodeURIComponent(filename))
          return
        } catch (_e){

        }
      }
      this.objectUrl = URL.createObjectURL(blob)
      //

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
        if (!bodyStr || ['{}','[]'].indexOf(bodyStr.replace(/\s/g,"")) > -1) {
          this.responseBody = []
          return false
        }
        let ret = parseJson(bodyStr)
        if(ret){
          this.responseBody = this.valueCopy(ret, [this.info.responseBodyDefinition || []]);
          this.forceUpdate = !this.forceUpdate
        }
    },

    valueCopy(newBody, oldBody, arrayFlag = false) {
      if (oldBody.length == 0) {
        return newBody
      }
      let that = this;
      newBody.map(item => {
        let oldItemArr = oldBody.filter(old => {
          if (arrayFlag) {
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
