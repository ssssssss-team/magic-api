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
    <span title="远程推送" @click="remotePush">
      <i class="ma-icon ma-icon-push"></i>
    </span>
    <span v-if="config.header.skin !== false" title="换肤" @click.stop="skinVisible = true">
      <i class="ma-icon ma-icon-skin"></i>
    </span>
    <span title="重新加载所有数据" @click="refresh">
      <i class="ma-icon ma-icon-refresh"></i>
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
    <magic-dialog v-model="exportVisible" title="导出" align="right" :moveable="false" width="340px" height="490px"
                  className="ma-tree-wrapper">
      <template #content>
        <magic-resource-choose ref="resourceExport" height="400px" max-height="400px"/>
      </template>
      <template #buttons>
        <button class="ma-button" @click="$refs.resourceExport.doSelectAll(true)">全选</button>
        <button class="ma-button" @click="$refs.resourceExport.doSelectAll(false)">取消全选</button>
        <button class="ma-button active" @click="doExport">导出</button>
      </template>
    </magic-dialog>
    <magic-dialog title="远程推送" v-model="showPushDialog" align="right" class="ma-remote-push-container ma-tree-wrapper"
                  width="400px" height="540px">
      <template #content>
        <magic-resource-choose ref="resourcePush" height="400px" max-height="400px"/>
        <div>
          <label>远程地址：</label>
          <magic-input placeholder="请输入远程地址" v-model="target" width="300px"/>
        </div>
        <div>
          <label>秘钥：</label>
          <magic-input placeholder="请输入秘钥" v-model="secretKey" width="300px"/>
        </div>
      </template>
      <template #buttons>
        <button class="ma-button" @click="$refs.resourcePush.doSelectAll(true)">全选</button>
        <button class="ma-button" @click="$refs.resourcePush.doSelectAll(false)">取消全选</button>
        <button class="ma-button active" @click="() => doPush('increment')">增量推送</button>
        <button class="ma-button" @click="() => doPush('full')">全量推送</button>
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
import MagicResourceChoose from '@/components/resources/magic-resource-choose.vue'
import MagicSearch from './magic-search.vue'

export default {
  name: 'MagicHeader',
  components: {
    MagicDialog,
    MagicInput,
    MagicSearch,
    MagicResourceChoose
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
      showPushDialog: false,
      exportVisible: false,
      filename: null,
      target: 'http://host:port/_magic-api-sync',
      secretKey: '123456789',
      skinRight: 40 + 'px',
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
    download() {
      this.exportVisible = true
      this.$refs.resourceExport.initData()
    },
    remotePush() {
      this.showPushDialog = true
      this.$refs.resourcePush.initData()
    },
    doExport() {
      let selected = this.$refs.resourceExport.getSelected()
      if (selected.length > 0) {
        request.send('/download', JSON.stringify(selected), {
          method: 'post',
          headers: {
            'Content-Type': 'application/json'
          },
          transformRequest: [],
          responseType: 'blob'
        }).success(blob => {
          downloadFile(blob, 'magic-api.zip')
        });
      }
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
    doPush(mode) {
      let selected = 'full' === mode ? [] : this.$refs.resourcePush.getSelected()
      let _push = () => {
        request.send('/push', JSON.stringify(selected), {
          method: 'post',
          headers: {
            'magic-push-target': this.target,
            'magic-push-secret-key': this.secretKey,
            'magic-push-mode': mode,
            'Content-Type': 'application/json'
          },
          transformRequest: []
        }).success(() => {
          this.$magicAlert({
            content: '推送成功!'
          })
          this.showPushDialog = false;
        })
      }
      if ('full' === mode) {
        this.$magicConfirm({
          title: '远程推送',
          content: `全量模式推送时，以本地数据为准全量覆盖更新,是否继续？`,
          ok: '继续',
          cancel: '取消',
          onOk: () => {
            _push()
          }
        });
      } else if (selected.length > 0) {
        _push()
      }
    },
    doUpload(mode) {
      let file = this.$refs.file.files[0];
      if (file) {
        let formData = new FormData();
        formData.append('file', file, this.filename);
        formData.append('mode', mode);
        let _upload = () => {
          this.showUploadDialog = false;
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
        if (mode === 'full') {
          this.$magicConfirm({
            title: '上传接口',
            content: `全量模式上传时，以上传的数据为准进行覆盖更新操作，可能会删除其他接口<br>在非全量导出时，建议使用增量更新，是否继续？`,
            ok: '继续',
            cancel: '取消',
            onOk: () => {
              _upload();
            }
          });
        } else {
          _upload();
        }
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
    refresh() {
      request.send('refresh').success(() => {
        bus.$emit('refresh-resource')
        bus.$emit('status', `刷新资源成功`)
      })
    }
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

.ma-header .ma-icon-push {
  color: var(--button-run-color);
  font-weight: bold;
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

.ma-remote-push-container label {
  width: 80px;
  text-align: right;
  display: inline-block;
}

ul li {
  height: 24px;
  line-height: 24px;
  text-align: center;
  cursor: pointer;
}
</style>
