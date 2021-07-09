<template>
  <div>
    <magic-dialog title="全局搜索" :value="showDialog" align="right" width="700px" padding="0" @onClose="close()">
      <template #content>
        <div class="ma-search-container">
          <magic-input v-model="inputText" width="100%" placeholder="请输入关键字进行搜索"></magic-input>
          <div v-show="searchList.length > 0" class="ma-search-result-container">
            <div class="ma-search-result-item" :class="{ selected: selectedItem.id === item.id }" v-for="(item, index) in searchList" :key="'ma-search-result-item' + index" @click="resultItemHandle(item)" @dblclick="resultItemDbHandle(item)">
              <div class="label" v-html="item.text"></div>
              <div class="name">{{ item.cache ? item.cache.groupName + '/' + item.cache.name : '' }}</div>
              <div class="line" v-text="item.line"></div>
            </div>
          </div>
          <div v-show="searchList.length > 0" class="display-text">
            {{ getDisplayText(selectedItem) }}
          </div>
          <div v-show="searchList.length > 0">
            <div id="searchEditor" ref="searchEditor" style="height: 300px;"></div>
          </div>
          <div v-show="searchList.length === 0" class="no-data-tip">
            没有搜索到内容
          </div>
        </div>
      </template>
    </magic-dialog>
  </div>
</template>

<script>
import * as monaco from 'monaco-editor'
import MagicDialog from '@/components/common/modal/magic-dialog.vue'
import MagicInput from '@/components/common/magic-input.vue'
import store from '@/scripts/store.js'
import request from '@/api/request.js'
import bus from '@/scripts/bus.js'
import { TokenizationRegistry } from 'monaco-editor/esm/vs/editor/common/modes.js'
import { tokenizeToString } from 'monaco-editor/esm/vs/editor/common/modes/textToHtmlTokenizer.js'
export default {
  name: 'MagicSearch',
  components: {
    MagicDialog,
    MagicInput
  },
  data() {
    return {
      showDialog: false,
      searchEditor: null,
      inputText: '',
      selectedItem: {},
      searchList: [],
      searchListFlag: null,
      listItemClickFlag: null
    }
  },
  watch: {
    inputText(val) {
      this.getResult(val)
    }
  },
  methods: {
    show() {
      this.showDialog = true
    },
    close() {
      this.showDialog = false
    },
    initEditor() {
      if (this.searchList.length > 0 && !this.searchEditor) {
        this.searchEditor = monaco.editor.create(document.getElementById('searchEditor'), {
          minimap: {
            enabled: false
          },
          language: 'magicscript',
          folding: true,
          lineDecorationsWidth: 0,
          wordWrap: 'on',
          readOnly: true,
          scrollBeyondLastLine: false,
          theme: store.get('skin') || 'default'
        })
      }
    },
    setValue(value) {
      if (this.searchEditor) {
        this.searchEditor.setValue(value)
        let matches = this.searchEditor.getModel().findMatches(this.inputText);
        if(matches && matches.length > 0){
          this.searchEditor.setSelections(matches.map(it => {
            const range = it.range;
            return {
              positionColumn: range.endColumn,
              positionLineNumber: range.endLineNumber,
              selectionStartColumn: range.startColumn,
              selectionStartLineNumber: range.startLineNumber
            }
          }))
        }
      }
    },
    async getHighlight(value){
      const support = await TokenizationRegistry.getPromise('magicscript');
      if (support) {
        return tokenizeToString(value, support);
      }
      return value;
    },
    getResult(text) {
      clearTimeout(this.searchListFlag)
      if (text) {
        this.searchListFlag = setTimeout(() => {
          request.send(`search?keyword=${text}`).success(data => {
            this.searchList = data
            this.buildSearchList(text)
            if(data && data.length > 0){
              this.selectedItem = data[0]
              this.getDetail()
            }
          })
        }, 600)
      } else {
        this.searchList = []
      }
    },
    buildSearchList(text) {
      this.selectedItem = {}
      this.setValue('')
      const $parent = this.$parent.$parent.$refs
      this.searchList.forEach(async item => {
        // 增加关键字高亮
        //item.text = item.text.replace(new RegExp(text, 'g'), `<span class="keyword">${text}</span>`)
        item.text = (await this.getHighlight(item.text)).replaceAll(/(?<=>)(.|\s)*?(?=<\/?\w+[^<]*>)/g, it=> it.replaceAll(text, `<span class="keyword">${text}</span>`))
        if (item.type === 1) {
          item.cache = $parent.apiList.getItemById(item.id)
        } else if (item.type === 2) {
          item.cache = $parent.functionList.getItemById(item.id)
        }
      })
      this.$nextTick(() => {
        this.initEditor()
      })
    },
    // 单击事件
    resultItemHandle(item) {
      clearTimeout(this.listItemClickFlag)
      this.listItemClickFlag = setTimeout(() => {
        this.selectedItem = item
        this.getDetail()
      }, 300)
    },
    // 双击事件
    resultItemDbHandle(item) {
      clearTimeout(this.listItemClickFlag)
      const $parent = this.$parent.$parent.$refs
      if (item.type === 1) {
        $parent.apiList.open(item.cache)
      } else if (item.type === 2) {
        $parent.functionList.open(item.cache)
      }
      this.close()
      bus.$emit('search-open', item)
    },
    getDetail() {
      // 1接口，2函数
      if (this.selectedItem.id) {
        request.send(`/${this.selectedItem.type == 1 ? '' : 'function/'}get?id=${this.selectedItem.id}`).success(data => {
          this.setValue(data.script)
        })
      }
    },
    getDisplayText(item) {
      if (item.cache && item.cache.id && !item.cache.empty) {
        return `${item.cache.groupName}/${item.cache.name}(${item.cache.groupPath}/${item.cache.path})`.replace(/\/+/g, '/')
      }
      return ''
    }
  },
  destroyed() {
    if (this.searchEditor) {
      this.searchEditor.dispose()
    }
  }
}
</script>

<style>
.ma-search-container{
  overflow: hidden;
}
.ma-search-container .ma-search-result-container {
  overflow: auto;
  height: 200px;
}
.ma-search-result-container{
  background: var(--toolbox-background);
  margin-top: -4px;
}
.ma-search-container .ma-search-result-container .ma-search-result-item {
  display: flex;
  padding: 0 5px;
  line-height: 20px;
}
.ma-search-container .ma-search-result-container .ma-search-result-item:hover,
.ma-search-container .ma-search-result-container .ma-search-result-item.selected {
  background: var(--toolbox-list-hover-background);
}
.ma-search-container .ma-search-result-container .ma-search-result-item .label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ma-search-container .ma-search-result-container .ma-search-result-item .label .keyword {
  background: #FFDE7B;
  color: #000000;
}
.ma-search-result-item .name,.ma-search-result-item .line{
  color: var(--toolbox-list-span-color)
}
.ma-search-container .ma-search-result-container .ma-search-result-item .line {
  padding-left: 5px;
}
.ma-search-container .display-text {
  padding: 0 10px;
  height: 30px;
  border-top: 1px solid var(--border-color);
}
.ma-search-container .no-data-tip {
  line-height: 530px;
  text-align: center;
}
</style>
