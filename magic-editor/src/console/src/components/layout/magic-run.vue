<template>
  <div class="ma-run">
    <div ref="resultEditor"></div>
  </div>
</template>

<script>
import bus from '@/scripts/bus.js'
import * as monaco from 'monaco-editor'
import store from '@/scripts/store.js'
import {isVisible} from '@/scripts/utils.js'

export default {
  name: 'MagicRun',
  props: {
    info: Object,
  },
  data() {
    return {
      resultEditor: null,
    }
  },
  updated() {
    this.updateSize()
  },
  mounted() {
    this.createEditor()
    bus.$on('update-response-body', (responseBody) => {
      this.resultEditor && this.resultEditor.setValue(responseBody || '')
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
        }
      })
    },
    updateSize() {
      if (this.resultEditor == null) {
        this.createEditor()
      }
      this.layout()
    },
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
</style>
