<template>
  <div class="ma-request-wrapper">
    <div class="ma-api-info">
      <label>请求方法</label>
      <magic-select :options="options" :value.sync="info.method" defaultValue="GET"/>
      <label>接口名称</label>
      <magic-input :value.sync="info.name" placeholder="请输入接口名称" style="flex:1"/>
      <label>请求路径</label>
      <magic-input :value.sync="info.path" placeholder="请输入接口路径" style="flex: 2"/>
    </div>
    <div class="ma-request-parameters">
      <ul class="not-select">
        <li v-for="(item, key) in navs" :key="'request_item_' + key" :class="{ selected: showIndex === key }"
            @click="showIndex = key;initRequestBodyDom();">{{ item }}
        </li>
      </ul>
      <div class="ma-layout">
        <div v-show="showIndex === 0" class="ma-layout-container">
          <div class="ma-header ma-table-row ma-table-request-row">
            <div style="width: 32px">必填</div>
            <div style="flex:1">Key</div>
            <div style="flex:1">Value</div>
            <div style="width:135px">参数类型</div>
            <div style="flex:1">默认值</div>
            <div style="width:100px">验证方式</div>
            <div style="flex:1">表达式或正则表达式</div>
            <div style="flex:1">验证说明</div>
            <div style="flex:2">Description</div>
          </div>
          <div class="ma-content">
            <div v-for="(item, key) in info.parameters" :key="'request_parameter_' + key"
                 class="ma-table-row ma-table-request-row">
              <div style="width: 32px">
                <magic-checkbox :focus="() => (parameterIndex = key)" :value.sync="item.required"/>
              </div>
              <div :class="{ focus: parameterIndex === key && !item.name }" style="flex:1">
                <magic-input :focus="() => (parameterIndex = key)" :value.sync="item.name" style="width: 100%"/>
              </div>
              <div style="flex:1">
                <magic-input :focus="() => (parameterIndex = key)" :value.sync="item.value" style="width: 100%"/>
              </div>
              <div style="width: 135px">
                <magic-select :border="false" :focus="() => (parameterIndex = key)" :options="types" :value.sync="item.dataType"
                              default-value="String" style="width: 100%"/>
              </div>
              <div style="flex:1">
                <magic-input :focus="() => (parameterIndex = key)" :value.sync="item.defaultValue" style="width: 100%"/>
              </div>
              <div style="width: 100px">
                <magic-select :border="false" :focus="() => (parameterIndex = key)" :options="validates" :value.sync="item.validateType"
                              default-value="pass" style="width: 100%"/>
              </div>
              <div style="flex:1">
                <magic-input :focus="() => (parameterIndex = key)" :value.sync="item.expression" style="width: 100%"/>
              </div>
              <div style="flex:1">
                <magic-input :focus="() => (parameterIndex = key)" :value.sync="item.error" style="width: 100%"/>
              </div>
              <div style="flex:2">
                <magic-input :focus="() => (parameterIndex = key)" :value.sync="item.description" style="width: 100%"/>
              </div>
            </div>
          </div>
        </div>
        <div v-show="showIndex === 1" class="ma-layout-container">
          <div class="ma-header ma-table-row ma-table-request-row">
            <div style="width:32px">必填</div>
            <div style="flex:1">Key</div>
            <div style="flex:1">Value</div>
            <div style="width: 100px">参数类型</div>
            <div style="flex:1">默认值</div>
            <div style="width: 100px">验证方式</div>
            <div style="flex:1">表达式或正则表达式</div>
            <div style="flex:1">验证说明</div>
            <div style="flex:2">Description</div>
          </div>
          <div class="ma-content">
            <div v-for="(item, key) in info.headers" :key="'request_header_' + key"
                 class="ma-table-row ma-table-request-row">
              <div style="width: 32px">
                <magic-checkbox :focus="() => (headerIndex = key)" :value.sync="item.required"/>
              </div>
              <div :class="{ focus: headerIndex === key && !item.name }" style="flex:1">
                <magic-input :focus="() => (headerIndex = key)" :value.sync="item.name" style="width: 100%"/>
              </div>
              <div style="flex:1">
                <magic-input :focus="() => (headerIndex = key)" :value.sync="item.value" style="width: 100%"/>
              </div>
              <div style="width:100px">
                <magic-select :border="false" :focus="() => (headerIndex = key)" :options="headerTypes"
                              :value.sync="item.dataType" default-value="String" style="width: 100%"/>
              </div>
              <div style="flex:1">
                <magic-input :focus="() => (headerIndex = key)" :value.sync="item.defaultValue" style="width: 100%"/>
              </div>
              <div style="width:100px">
                <magic-select :border="false" :focus="() => (headerIndex = key)" :options="validates"
                              :value.sync="item.validateType" default-value="pass" style="width: 100%"/>
              </div>
              <div style="flex:1">
                <magic-input :focus="() => (headerIndex = key)" :value.sync="item.expression" style="width: 100%"/>
              </div>
              <div style="flex:1">
                <magic-input :focus="() => (headerIndex = key)" :value.sync="item.error" style="width: 100%"/>
              </div>
              <div style="flex:2">
                <magic-input :focus="() => (headerIndex = key)" :value.sync="item.description" style="width: 100%"/>
              </div>
            </div>
          </div>
        </div>

        <div v-show="showIndex === 2" class="ma-layout-container">
          <div class="ma-header ma-table-row ma-table-request-row">
            <div style="flex: 1">Key</div>
            <div style="flex: 1">Value</div>
            <div style="width: 100px">参数类型</div>
            <div style="width: 100px">验证方式</div>
            <div style="flex: 1">表达式或正则表达式</div>
            <div style="flex: 1">验证说明</div>
            <div style="flex: 2">Description</div>
          </div>
          <div class="ma-content">
            <div v-for="(item, key) in info.paths" :key="'request_header_' + key"
                 class="ma-table-row ma-table-request-row">
              <div :class="{ focus: pathIndex === key && !item.name }" style="flex:1">
                <magic-input :focus="() => (pathIndex = key)" :value.sync="item.name" style="width: 100%"/>
              </div>
              <div style="flex:1">
                <magic-input :focus="() => (pathIndex = key)" :value.sync="item.value" style="width: 100%"/>
              </div>
              <div style="width:100px">
                <magic-select :border="false" :focus="() => (pathIndex = key)" :options="headerTypes"
                              :value.sync="item.dataType" default-value="String" style="width: 100%"/>
              </div>
              <div style="width: 100px">
                <magic-select :border="false" :focus="() => (pathIndex = key)" :options="validates"
                              :value.sync="item.validateType" default-value="pass" style="width: 100%"/>
              </div>
              <div style="flex:1">
                <magic-input :focus="() => (pathIndex = key)" :value.sync="item.expression" style="width: 100%"/>
              </div>
              <div style="flex:1">
                <magic-input :focus="() => (pathIndex = key)" :value.sync="item.error" style="width: 100%"/>
              </div>
              <div style="flex:2">
                <magic-input :focus="() => (pathIndex = key)" :value.sync="item.description" style="width: 100%"/>
              </div>
            </div>
          </div>
        </div>

        <div v-show="showIndex === 3" class="ma-layout-container">
          <div ref="bodyEditor" class="ma-body-editor"></div>
        </div>
        <div v-show="showIndex === 4" class="ma-layout-container" style="overflow: hidden; right: 0">
          <magic-textarea :value.sync="info.description" style="width: 100%; height: 100%; margin: 2px"/>
        </div>
        <div v-show="showIndex < 3" class="not-select ma-sider">
          <div @click="addRow"><i class="ma-icon ma-icon-plus"/></div>
          <div @click="removeRow"><i class="ma-icon ma-icon-minus"/></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import MagicInput from '@/components/common/magic-input.vue'
import MagicSelect from '@/components/common/magic-select.vue'
import MagicCheckbox from '@/components/common/magic-checkbox.vue'
import MagicTextarea from '@/components/common/magic-textarea.vue'
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api'
import {formatJson, isVisible} from '@/scripts/utils.js'
import bus from '@/scripts/bus.js'
import store from '@/scripts/store.js'

export default {
  name: 'MagicRequest',
  props: {
    info: Object
  },
  components: {
    MagicInput,
    MagicSelect,
    MagicTextarea,
    MagicCheckbox
  },
  data() {
    return {
      navs: ['请求参数', '请求Header', '路径变量',  '请求Body', '接口描述'],
      options: [
        {value: 'GET', text: 'GET'},
        {value: 'POST', text: 'POST'},
        {value: 'PUT', text: 'PUT'},
        {value: 'DELETE', text: 'DELETE'}
      ],
      headerTypes: [
        {value: 'String', text: 'String'},
        {value: 'Integer', text: 'Integer'},
        {value: 'Double', text: 'Double'},
        {value: 'Long', text: 'Long'},
        {value: 'Short', text: 'Short'},
        {value: 'Float', text: 'Float'},
        {value: 'Byte', text: 'Byte'},
      ],
      types: [
        {value: 'String', text: 'String'},
        {value: 'Integer', text: 'Integer'},
        {value: 'Double', text: 'Double'},
        {value: 'Long', text: 'Long'},
        {value: 'Short', text: 'Short'},
        {value: 'Float', text: 'Float'},
        {value: 'Byte', text: 'Byte'},
        {value: 'MultipartFile', text: 'MultipartFile'},
        {value: 'MultipartFiles', text: 'MultipartFiles'}
      ],
      validates: [
        {value: 'pass', text: '不验证'},
        {value: 'expression', text: '表达式验证'},
        {value: 'pattern', text: '正则验证'},
      ],
      showIndex: 0,
      parameterIndex: 0,
      headerIndex: 0,
      pathIndex: 0,
      bodyEditor: null,
      updating: false
    }
  },
  mounted() {
    bus.$on('update-request-body', (newVal) => {
      this.initRequestBodyDom()
      this.bodyEditor && this.bodyEditor.setValue(formatJson(newVal) || newVal || '{\r\n\t\r\n}')
    })
  },
  methods: {
    layout() {
      this.$nextTick(() => {
        if (this.bodyEditor && isVisible(this.$refs.bodyEditor)) {
          this.bodyEditor.layout()
        }
      })
    },
    addRow() {
      if (!this.info.parameters) {
        this.$magicAlert({
          content: '请先添加或选择接口'
        })
        return
      }
      if (this.showIndex === 0) {
        this.info.parameters.push({name: '', value: '', description: ''})
        this.parameterIndex = this.info.parameters.length - 1
      } else if (this.showIndex === 1) {
        this.info.headers.push({name: '', value: '', description: ''})
        this.headerIndex = this.info.headers.length - 1
      } else if (this.showIndex === 2) {
        this.info.paths.push({name: '', value: '', description: ''})
        this.pathIndex = this.info.paths.length - 1
      }
      this.$forceUpdate()
    },
    removeRow() {
      if (!this.info.parameters) {
        this.$magicAlert({
          content: '请先添加或选择接口'
        })
        return
      }
      if (this.showIndex === 0) {
        this.info.parameters.splice(this.parameterIndex, 1)
        if (this.info.parameters.length === 0) {
          this.parameterIndex = 0
          this.addRow()
        } else if (this.info.parameters.length <= this.parameterIndex) {
          this.parameterIndex = this.info.parameters.length - 1
        }
      } else if (this.showIndex === 1) {
        this.info.headers.splice(this.headerIndex, 1)
        if (this.info.headers.length === 0) {
          this.headerIndex = 0
          this.addRow()
        } else if (this.info.headers.length <= this.headerIndex) {
          this.headerIndex = this.info.headers.length - 1
        }
      } else if (this.showIndex === 2) {
        this.info.paths.splice(this.pathIndex, 1)
        if (this.info.paths.length === 0) {
          this.pathIndex = 0
          this.addRow()
        } else if (this.info.paths.length <= this.pathIndex) {
          this.pathIndex = this.info.paths.length - 1
        }
      }
      this.$forceUpdate()
    },
    initRequestBodyDom() {
      if (this.bodyEditor == null && this.showIndex === 3) {
        this.bodyEditor = monaco.editor.create(this.$refs.bodyEditor, {
          minimap: {
            enabled: false
          },
          language: 'json',
          fixedOverflowWidgets: true,
          folding: true,
          wordWrap: 'on',
          lineDecorationsWidth: 35,
          theme: store.get('skin') || 'default',
          value: formatJson(this.info.requestBody) || '{\r\n\t\r\n}'
        })
        this.layout()
        this.bodyEditor.onDidChangeModelContent(() => {
          this.info.requestBody = this.bodyEditor.getValue()
        })
        bus.$on('update-window-size', () => this.layout())
      }
    }
  },
  destroyed() {
    if (this.bodyEditor) {
      this.bodyEditor.dispose()
    }
  }
}
</script>

<style scoped>
.ma-body-editor {
  width: 100%;
  height: 100%;
}


</style>
