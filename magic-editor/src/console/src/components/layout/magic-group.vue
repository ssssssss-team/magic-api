<template>
  <div class="ma-request-wrapper">
    <div class="ma-api-info">
      <label>分组名称</label>
      <magic-input v-model="info.name" placeholder="请输入分组名称" style="flex:1"/>
      <label>分组路径</label>
      <magic-input v-model="info.path" placeholder="请输入分组路径" style="flex:2;margin-right: 10px"/>
      <button class="ma-button" @click="doSave">保存</button>
    </div>
    <div class="ma-request-parameters">
      <ul class="not-select">
        <li v-for="(item, key) in navs" :key="key" :class="{ selected: showIndex === key }" @click="showIndex = key;">{{ item }}
        </li>
      </ul>
      <div class="ma-layout">
        <div class="not-select ma-sider">
          <div @click="addRow"><i class="ma-icon ma-icon-plus"/></div>
          <div @click="removeRow"><i class="ma-icon ma-icon-minus"/></div>
        </div>
        <div v-show="showIndex === 0" class="ma-layout-container">
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
            <div v-for="(item, key) in info.paths" :key="key"
                 class="ma-table-row ma-table-request-row">
              <div :class="{ focus: pathIndex === key && !item.name }" style="flex:1">
                <magic-input :focus="() => (pathIndex = key)" :value.sync="item.name" style="width: 100%"/>
              </div>
              <div style="flex:1">
                <magic-input :focus="() => (pathIndex = key)" :value.sync="item.value" style="width: 100%"/>
              </div>
              <div style="width:100px">
                <magic-select :border="false" :focus="() => (pathIndex = key)" :options="types"
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
        <div v-show="showIndex === 1" class="ma-layout-container">
          <div class="ma-header ma-table-row">
            <div>键</div>
            <div>值</div>
            <div>描述</div>
          </div>
          <div class="ma-content">
            <div v-for="(item, key) in info.options" :key="key" class="ma-table-row">
              <div :class="{ focus: optionIndex === key && !item.name }">
                <magic-select :focus="() => (optionIndex = key)"
                              :inputable="true"
                              :options="defaultOptions"
                              :select="value => onSelect(value, key)"
                              :value.sync="item.name" style="width: 100%"/>
              </div>
              <div>
                <magic-input :focus="() => (optionIndex = key)" :value.sync="item.value" style="width: 100%"/>
              </div>
              <div>
                <magic-input :focus="() => (optionIndex = key)" :value.sync="item.description" style="width: 100%"/>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import MagicInput from '@/components/common/magic-input.vue'
import MagicSelect from '@/components/common/magic-select.vue'
import request from "@/api/request"
import { requestGroup } from '@/scripts/utils.js'
import bus from "@/scripts/bus"
import contants from "@/scripts/contants.js"

export default {
  name: 'MagicGroup',
  props: {
    info: Object
  },
  components: {
    MagicInput,
    MagicSelect
  },
  data() {
    return {
      navs: ['路径变量','分组选项'],
      types: [
        {value: 'String', text: 'String'},
        {value: 'Integer', text: 'Integer'},
        {value: 'Double', text: 'Double'},
        {value: 'Long', text: 'Long'},
        {value: 'Short', text: 'Short'},
        {value: 'Float', text: 'Float'},
        {value: 'Byte', text: 'Byte'},
      ],
      validates: [
        {value: 'pass', text: '不验证'},
        {value: 'expression', text: '表达式验证'},
        {value: 'pattern', text: '正则验证'},
      ],
      defaultOptions: null,
      optionsMap: {},
      optionIndex: 0,
      showIndex: 0,
      pathIndex: 0
    }
  },
  mounted() {
    let map = {}
    request.send('/options').success(data => {
      data = data || []
      data = data.concat(contants.OPTIONS)
      this.defaultOptions = data&&data.map(e => {
        let item = {text: e[0], value: e[0], description: e[1], defaultValue: e[2]}
        this.optionsMap[item.value] = item;
        return item;
      })
    })
  },
  methods: {
    onSelect(value, index) {
      if (this.info.options[index]) {
        this.info.options[index].name = value;
        let item = this.optionsMap[value];
        if (item) {
          if (item.description) {
            this.info.options[index].description = item.description
          }
          if (item.defaultValue) {
            this.info.options[index].value = item.defaultValue
          }
        }
      }
    },
    doSave() {
      if (!this.info.paths) {
        this.$magicAlert({
          content: '请先添加或选择分组'
        })
        return
      }
      let saveObj = {...this.info}
      saveObj.paths =  saveObj.paths.filter(it => it.name)
      saveObj.options =  saveObj.options.filter(it => it.name)
      bus.$emit('status', `准备保存分组「${saveObj.name}」`)
      requestGroup('group/update', saveObj).success(data => {
        bus.$emit('update-group')
        bus.$emit('report', 'group_update')
        bus.$emit('status', `保存分组「${saveObj.name}」成功!`)
      })
    },
    addRow() {
      if (!this.info.paths) {
        this.$magicAlert({
          content: '请先添加或选择分组'
        })
        return
      }
      if (this.showIndex === 0) {
        this.info.paths.push({name: '', value: '', description: ''})
        this.pathIndex = this.info.paths.length - 1
      }else{
        this.info.options.push({name: '', value: '', description: ''})
        this.optionIndex = this.info.options.length - 1
      }
      this.$forceUpdate()
    },
    removeRow() {
      if (!this.info.paths) {
        this.$magicAlert({
          content: '请先添加或选择分组'
        })
        return
      }
      if (this.showIndex === 0) {
        this.info.paths.splice(this.pathIndex, 1)
        if (this.info.paths.length === 0) {
          this.pathIndex = 0
          this.addRow()
        } else if (this.info.paths.length <= this.pathIndex) {
          this.pathIndex = this.info.paths.length - 1
        }
      }else{
        this.info.options.splice(this.optionIndex, 1)
        if (this.info.options.length === 0) {
          this.optionIndex = 0
          this.addRow()
        } else if (this.info.options.length <= this.optionIndex) {
          this.optionIndex = this.info.options.length - 1
        }
      }
      this.$forceUpdate()
    }
  }
}
</script>