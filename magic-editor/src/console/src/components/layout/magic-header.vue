<template>
  <div class="ma-header">
    <div class="ma-logo">
      <label :title="config.title">{{ config.title }}</label>
      <label class="version" :title="config.version">{{ config.version }}</label>
    </div>
    <div :title="displayText">{{ displayText }}</div>
    <span :class="{ disabled: isDisableTest }" class="ma-button-run" title="运行（Ctrl+Q）" @click="bus.$emit('doTest')">
      <i class="ma-icon ma-icon-run"></i>
    </span>
    <span title="保存（Ctrl+S）" @click="bus.$emit('doSave')">
      <i class="ma-icon ma-icon-save"></i>
    </span>
    <span title="全局搜索" @click="$refs.search.show()">
      <i class="ma-icon ma-icon-search"></i>
    </span>
    <span title="历史记录" @click="bus.$emit('viewHistory')">
      <i class="ma-icon ma-icon-history"></i>
    </span>
    <span title="上传接口" @click="upload">
      <i class="ma-icon ma-icon-upload"></i>
    </span>
    <span title="导出接口" @click="download">
      <i class="ma-icon ma-icon-download"></i>
    </span>
    <span v-if="config.header.skin !== false" title="换肤" @click.stop="skinVisible = true">
      <i class="ma-icon ma-icon-skin"></i>
    </span>
    <span v-if="config.header.repo !== false" title="Gitee"
          @click="open('https://gitee.com/ssssssss-team/magic-api')">
      <i class="ma-icon ma-icon-gitee"></i>
    </span>
    <span v-if="config.header.repo !== false" title="Github"
          @click="open('https://github.com/ssssssss-team/magic-api')">
      <i class="ma-icon ma-icon-git"></i>
    </span>
    <span v-if="config.header.qqGroup !== false" title="加入QQ群"
          @click="open('https://shang.qq.com/wpa/qunwpa?idkey=10faa4cf9743e0aa379a72f2ad12a9e576c81462742143c8f3391b52e8c3ed8d')">
      <i class="ma-icon ma-icon-qq"></i>
    </span>
    <span v-if="config.header.document !== false" title="帮助文档"
          @click="open('https://ssssssss.org')">
      <i class="ma-icon ma-icon-help"></i>
    </span>
    <div v-show="skinVisible" :style="{ right: skinRight }" class="ma-skin-selector">
      <ul>
        <li v-for="theme in Object.keys(Themes)" :key="'theme_' + theme" @click="switchTheme(theme)">{{ theme }}</li>
      </ul>
    </div>
    <magic-dialog title="上传接口" :value="showUploadDialog" align="right" @onClose="showUploadDialog = false">
      <template #content>
        <input type="file" style="display: none" ref="file" @change="onFileSelected" accept="application/zip">
        <magic-input icon="upload" :readonly="true" width="235px" placeholder="未选择文件" :onClick="choseFile"
                     :value="filename"/>
      </template>
      <template #buttons>
        <button class="ma-button active" @click="() => doUpload('increment')">增量上传</button>
        <button class="ma-button" @click="() => doUpload('full')">全量上传</button>
      </template>
    </magic-dialog>
    <magic-search ref="search" style="flex: none"></magic-search>
  </div>
</template>

<script>
import bus from '@/scripts/bus.js'
import {Themes} from '@/scripts/editor/theme.js'
import {download as downloadFile} from '@/scripts/utils.js'
import * as monaco from 'monaco-editor'
import store from '@/scripts/store.js'
import request from '@/api/request.js'
import MagicDialog from '@/components/common/modal/magic-dialog.vue'
import MagicInput from '@/components/common/magic-input.vue'
import MagicSearch from './magic-search.vue'

export default {
  name: 'MagicHeader',
  components: {
    MagicDialog,
    MagicInput,
    MagicSearch
  },
  props: {
    config: Object,
    themeStyle: Object,
  },
  data() {
    return {
      info: null,
      bus,
      Themes,
      skinVisible: false,
      showUploadDialog: false,
      filename: null,
      skinRight: 15 + ((this.config.header.repo ? 2 : 0) + (this.config.header.qqGroup ? 1 : 0) + (this.config.header.document ? 1 : 0)) * 25 + 'px',
    }
  },
  mounted() {
    this.$root.$el.addEventListener('click', () => (this.skinVisible = false))
    bus.$on('opened', (info) => {
      // 解决修改了接口不更新的问题
      this.info = null
      this.info = info
    })
    this.switchTheme(store.get('skin') || this.config.defaultTheme || 'default')
  },
  methods: {
    open(url) {
      window.open(url)
    },
    download() {
      request.send('/download', null, {
        responseType: 'blob'
      }).success(blob => {
        downloadFile(blob, 'magic-api-all.zip')
      });
    },
    onFileSelected() {
      if (this.$refs.file.files[0]) {
        this.filename = this.$refs.file.files[0].name;
      }
    },
    choseFile() {
      this.$refs.file.click();
    },
    upload() {
      this.showUploadDialog = true;
    },
    doUpload(mode) {
      let file = this.$refs.file.files[0];
      if (file) {
        this.showUploadDialog = false;
        let formData = new FormData();
        formData.append('file', file, this.filename);
        formData.append('mode', mode);
        request.send('/upload', formData, {
          method: 'post',
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }).success(() => {
          this.$magicAlert({
            content: '上传成功!'
          })
          bus.$emit('refresh-resource')
        })
        this.filename = '';
        this.$refs.file.value = '';
      }
    },
    switchTheme($theme) {
      Object.keys(this.themeStyle).forEach((key) => this.$delete(this.themeStyle, key))
      let theme = Themes[$theme]
      let keys = Object.keys(theme)
      keys.forEach((key) => {
        this.$set(this.themeStyle, `--${key}`, theme[key])
      })
      store.set('skin', $theme)
      monaco.editor.setTheme($theme)
      this.$emit('update:themeStyle', this.themeStyle)
    },
  },
  computed: {
    isDisableTest() {
      if (this.info && !this.info.empty && this.info._type === 'api') {
        return this.info.running === true
      }
      return true;
    },
    displayText() {
      if (this.info && !this.info.empty) {
        return `${this.info.groupName}/${this.info.name}(${this.info.groupPath}/${this.info.path})`.replace(/\/+/g, '/')
      }
      return ''
    },
  },
}
</script>

<style scoped>
.ma-header {
  height: 30px;
  line-height: 30px;
  text-align: right;
  color: var(--header-default-color);
  background: var(--background);
  display: flex;
  border-bottom: 1px solid var(--border-color);
  padding-right: 15px;
}

.ma-header > .ma-logo {
  float: left;
  color: var(--header-title-color);
  font-weight: bold;
  font-size: 0px;
  letter-spacing: 0px;
  background-repeat: no-repeat;
  background-size: 24px 24px;
  background-position: 0px 4px;
  padding-left: 25px;
}

.ma-header > div:not(.ma-logo):not(.ma-skin-selector) {
  flex: 1;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ma-header img {
  height: 24px;
  width: 22px;
  vertical-align: middle;
  margin-top: -6px;
}

.ma-header > div > label {
  font-size: 16px;
  display: inline-block;
  margin-right: 5px;
  text-align: left;
}

.ma-header > div > label.version {
  font-size: 12px;
  color: var(--header-version-color);
}

.ma-header > span {
  cursor: pointer;
  padding: 0 4px;
  height: 30px;
  line-height: 30px;
  display: inline-block;
  vertical-align: middle;
  border-radius: 2px;
  text-align: center;
}

.ma-header > span:last-child {
  margin-right: 15px;
}

.ma-header .ma-button-run {
  color: var(--button-run-color);
}

.ma-header > span:hover:not(.disabled) {
  background: var(--button-hover-background);
}

.ma-header > span.disabled {
  color: var(--button-disabled-background);
}

.ma-skin-selector {
  position: absolute;
  top: 30px;
  right: 100px;
  z-index: 20;
  background: var(--background);
  border: 1px solid var(--border-color);
  border-top: none;
}

.ma-skin-selector ul li:not(:last-child) {
  border-bottom: 1px solid var(--border-color);
  padding: 2px 5px;
}

ul li {
  height: 24px;
  line-height: 24px;
  text-align: center;
  cursor: pointer;
}
</style>
