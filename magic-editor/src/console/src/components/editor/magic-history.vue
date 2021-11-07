<template>
  <div class="ma-history">
    <ul class="not-select">
      <li v-for="(item, key) in timestampes" :key="'history_' + key" :class="{ selected: currentItem === item }"
          @click.stop="open(item)">
        {{ item.dateTime }} ({{item.createBy || 'guest'}})
      </li>
    </ul>
    <div class="version">
      <span class="version-time">{{ currentItem.dateTime }} ({{ currentItem.createBy || 'guest'}})</span>
      <span class="current">当前版本</span>
    </div>
    <div ref="diffEditor" class="diff-editor"></div>
  </div>
</template>

<script>
import * as monaco from 'monaco-editor'
import bus from '@/scripts/bus.js'
import {formatDate, isVisible} from '@/scripts/utils.js'
import request from '@/api/request.js'
import contants from "@/scripts/contants";

export default {
  name: 'MagicHistory',
  data() {
    return {
      displayText: '',
      timestampes: [],
      currentItem: {},
      scriptEditor: null,
      diffEditor: null,
      scriptModel: null,
      originalModel: null,
      isApi: true
    }
  },
  mounted() {
    this.diffEditor = monaco.editor.createDiffEditor(this.$refs.diffEditor, {
      enableSplitViewResizing: false,
      minimap: {
        enabled: false,
      },
      folding: false,
      lineDecorationsWidth: 20,
      fixedOverflowWidgets: false,
      fontFamily: contants.EDITOR_FONT_FAMILY,
      fontSize: contants.EDITOR_FONT_SIZE,
      fontLigatures: true
    })
    bus.$on('update-window-size', this.layout)
  },
  methods: {
    open(item) {
      this.currentItem = item
      request
          .send(this.isApi ? 'backup/get' : 'function/backup/get', {
            id: item.id,
            timestamp: item.timestamp,
          })
          .success((info) => {
            info = JSON.parse(info.content)
            this.originalModel = monaco.editor.createModel(info.script, 'magicscript');
            this.diffEditor.setModel({
              original: this.originalModel,
              modified: this.scriptModel,
            })
            this.layout()
          })
    },
    load(timestampes, item, scriptEditor, isApi) {
      this.isApi = isApi;
      this.currentItem = {}
      this.scriptEditor = scriptEditor
      this.scriptModel = monaco.editor.createModel(this.scriptEditor.getValue(), 'magicscript')
      this.originalModel = this.scriptModel;
      this.timestampes = timestampes.map((t) => {
        return {id: item.id, timestamp: t.createDate, dateTime: formatDate(t.createDate * 1), createBy: t.createBy}
      })
      if (this.timestampes.length > 0) {
        this.open(this.timestampes[0])
      }
    },
    layout() {
      this.$nextTick(() => {
        if (isVisible(this.$refs.diffEditor)) {
          this.$nextTick(() => this.diffEditor.layout())
        }
      })
    },
    reset() {
      this.scriptEditor.setValue(this.originalModel.getValue());
    }
  },
}
</script>

<style scoped>
.ma-history {
  overflow: auto;
  position: relative;
  width: 100%;
  height: 485px;
  border-top: 1px solid var(--border-color);
}

.ma-history ul {
  position: absolute;
  left: 0px;
  width: 210px;
  bottom: 5px;
  top: 0px;
  color: var(--color);
  overflow: auto;
  background: var(--toolbox-background);
}

.ma-history ul li {
  border-bottom: 1px solid var(--border-color);
  height: 20px;
  line-height: 20px;
  padding-left: 5px;
  white-space: nowrap;
}

.ma-history ul li:hover,
.ma-history ul li.selected {
  background: var(--history-select-background);
  color: var(--history-select-color);
}

.ma-history .version {
  position: absolute;
  left: 210px;
  right: 0px;
  line-height: 24px;
  height: 24px;
}

.ma-history .version span {
  float: left;
  display: block;
  padding: 0 10px;
}

.ma-history .version span.current {
  float: right;
}

.ma-history .diff-editor {
  position: absolute;
  left: 210px;
  right: 0px;
  top: 24px;
  bottom: 5px;
}
</style>
