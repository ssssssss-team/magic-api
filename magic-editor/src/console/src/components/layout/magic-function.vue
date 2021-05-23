<template>
  <div class="ma-request-wrapper">
    <div class="ma-api-info">
      <label>返回值</label>
      <magic-select :options="types" :value.sync="info.returnType" defaultValue="Object"/>
      <label>函数名称</label>
      <magic-input :value.sync="info.name" placeholder="请输入函数名称" width="255px"/>
      <label>函数路径</label>
      <magic-input :value.sync="info.path" placeholder="请输入函数路径" width="500px"/>
    </div>
    <div class="ma-request-parameters">
      <ul class="not-select">
        <li v-for="(item, key) in navs" :key="'request_item_' + key" :class="{ selected: showIndex === key }"
            @click="showIndex = key;">{{ item }}
        </li>
      </ul>
      <div class="ma-layout">
        <div v-show="showIndex === 0" class="ma-layout-container">
          <div class="ma-header ma-table-row">
            <div>参数名</div>
            <div>类型</div>
            <div>描述</div>
          </div>
          <div class="ma-content">
            <div v-for="(item, key) in info.parameters" :key="'request_parameter_' + key"
                 class="ma-table-row">
              <div :class="{ focus: parameterIndex === key && !item.name }">
                <magic-input :focus="() => (parameterIndex = key)" :value.sync="item.name"
                             style="width: 100%"/>
              </div>
              <div>
                <magic-select :focus="() => (parameterIndex = key)" :options="types" :value.sync="item.type"  style="width: 100%"/>
              </div>
              <div>
                <magic-input :focus="() => (parameterIndex = key)" :value.sync="item.description"
                             style="width: 100%"/>
              </div>
            </div>
          </div>
        </div>
        <div v-show="showIndex === 1" class="ma-layout-container" style="overflow: hidden; right: 0">
          <magic-textarea :value.sync="info.description" style="width: 100%; height: 100%; margin: 2px"/>
        </div>
        <div v-show="showIndex < 1" class="not-select ma-sider">
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
import MagicTextarea from '@/components/common/magic-textarea.vue'

export default {
  name: 'MagicFunction',
  props: {
    info: Object
  },
  components: {
    MagicInput,
    MagicSelect,
    MagicTextarea
  },
  data() {
    return {
      navs: ['函数参数', '函数描述'],
      types: [
        {value: 'java.lang.Number', text: '数值'},
        {value: 'java.lang.String', text: '字符串'},
        {value: 'java.util.Collection', text: '集合'},
        {value: 'java.util.Map', text: 'Map'},
        {value: 'java.lang.Object', text: 'Object'}
      ],
      showIndex: 0,
      parameterIndex: 0,
    }
  },
  methods: {
    addRow() {
      if (!this.info.parameters) {
        this.$magicAlert({
          content: '请先添加或选择接口'
        })
        return
      }
      if (this.showIndex === 0) {
        this.info.parameters.push({name: '', type: '', description: ''})
        this.parameterIndex = this.info.parameters.length - 1
      } else if (this.showIndex === 1) {
        this.info.requestHeader.push({name: '', type: '', description: ''})
        this.headerIndex = this.info.requestHeader.length - 1
      }
      this.$forceUpdate()
    },
    removeRow() {
      if (!this.info.parameters) {
        this.$magicAlert({
          content: '请先添加或选择函数'
        })
        return
      }
      this.info.parameters.splice(this.parameterIndex, 1)
      if (this.info.parameters.length === 0) {
        this.parameterIndex = 0
        this.addRow()
      } else if (this.info.parameters.length <= this.parameterIndex) {
        this.parameterIndex = this.info.parameters.length - 1
      }
      this.$forceUpdate()
    }
  }
}
</script>

<style scoped>
.ma-request-wrapper {
  background: var(--background);
  height: 100%;
  width: 100%;
  position: relative;
}

.ma-api-info {
  padding: 5px;
  border-bottom: 1px solid var(--tab-bar-border-color);
}

.ma-api-info * {
  display: inline-block;
}

.ma-api-info label {
  width: 75px;
  text-align: right;
  padding: 0 5px;
}

.ma-api-info input:last-child {
  width: calc(100% - 570px);
}

.ma-request-wrapper > div:not(.ma-api-info) {
  position: absolute;
  top: 33px;
  bottom: 0px;
  width: 100%;
  overflow: hidden;
  display: inline-block;
}

.ma-request-wrapper > div > h3 {
  color: var(--color);
  font-size: 12px;
  font-weight: inherit;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-bottom: 1px solid var(--tab-bar-border-color);
}

.ma-layout .ma-table-row > * {
  width: 20%;
}

.ma-layout .ma-table-row > *:last-child {
  width: 60%;
}
</style>
