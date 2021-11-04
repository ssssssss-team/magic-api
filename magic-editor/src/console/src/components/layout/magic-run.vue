<template>
  <div class="ma-run">
    <ul class="not-select ma-nav-tab">
      <li v-for="(item, key) in navs" :key="'response_item_' + key" :class="{ selected: showIndex === key }" @click="showIndex = key;">{{ item }}
      </li>
    </ul>
    <div ref="resultEditor" class="ma-body-editor" v-show="showIndex === 0 && !contentType"></div>
    <iframe v-if="contentType && showIndex === 0" class="ma-response-body-container" style="padding:5px;" :src="this.objectUrl"></iframe>
    <div class="ma-layout" v-if="showIndex === 1">
      <div class="ma-layout-container">
        <div class="ma-header ma-table-row">
          <div>Key</div>
          <div>Value</div>
        </div>
        <div class="ma-content">
          <div v-for="(value, key) in responseHeaders" :key="'response_header_' + key" class="ma-table-row content-bg">
            <div>{{ key }}</div>
            <div>{{ value }}</div>
          </div>
        </div>
      </div>
    </div>
    <magic-json v-if="!contentType && showIndex === 2" :jsonData="responseBody || []" :forceUpdate="forceUpdate" type="response"></magic-json>
  </div>
</template>

<script>

import MagicJson from '@/components/common/magic-json.vue'
import bus from '@/scripts/bus.js'
import * as monaco from 'monaco-editor'
import store from '@/scripts/store.js'
import {isVisible, deepClone, download as downloadFile} from '@/scripts/utils.js'
import {parseJson} from '@/scripts/parsing/parser.js'
import contants from "@/scripts/contants";

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
      responseHeaders: {},
      forceUpdate: false,
      contentType: '',
      navs: ['Body','响应Header', '响应结构'],
      objectUrl: null,
      showIndex: 0
    }
  },
  watch: {
    responseBody: {
      handler(responseBodyArr) {
        this.info.responseBodyDefinition = deepClone(responseBodyArr[0], ['level'])
      },
      deep: true
    }
  },
  updated() {
    this.updateSize()
  },
  mounted() {
    this.createEditor()
    bus.$on('update-response-body', (responseBody, headers) => {
      this.contentType = null
      if(this.objectUrl){
        URL.revokeObjectURL(this.objectUrl)
        this.objectUrl = null
      }
      this.resultEditor && this.resultEditor.setValue(responseBody || '')
      this.updateResponseBody(responseBody)
      this.responseHeaders = headers
    })
    bus.$on('update-response-body-definition', (responseBodyDefinition) => {
      this.responseBody = responseBodyDefinition ? [responseBodyDefinition] : []
    })
    bus.$on('update-response-blob',(contentType, blob, headers) => {
      this.contentType = contentType
      this.responseHeaders = headers
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
        fontFamily: contants.EDITOR_FONT_FAMILY,
        fontSize: contants.EDITOR_FONT_SIZE,
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
            item.dataType = oldItemArr[0].dataType
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
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

div.ma-run > *:not(ul) {
  width: 100%;
  height: 100%;
  flex: 1;
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
.ma-layout-container .ma-header div,
.ma-layout-container .ma-content .ma-table-row div{
  width: 50%;
  padding-left: 5px;
  background: none;
}
.ma-run .ma-layout .ma-content .content-bg{
  cursor: pointer;
}
.ma-run .ma-layout .ma-content .content-bg:hover{
  background: var(--toolbox-list-hover-background);
}

</style>
