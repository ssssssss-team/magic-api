<template>
  <div class="ma-settings">
    <ul class="ma-nav not-select">
      <li v-for="(item, key) in navs" :key="'response_item_' + key" :class="{ selected: showIndex == key }"
          @click="showIndex = key">
        {{ item }}
      </li>
    </ul>
    <div class="ma-layout">
      <div v-if="showIndex == 0" class="ma-layout-container">
        <div class="ma-header ma-table-row">
          <div>Key</div>
          <div>Value</div>
          <div>Description</div>
        </div>
        <div class="ma-content">
          <div v-for="(item, key) in parameters" :key="'request_parameter_' + key" class="ma-table-row">
            <div :class="{ focus: parameterIndex == key && !item.name }">
              <magic-input :focus="() => (parameterIndex = key)" :value.sync="item.name" style="width: 100%"/>
            </div>
            <div>
              <magic-input :focus="() => (parameterIndex = key)" :value.sync="item.value" style="width: 100%"/>
            </div>
            <div>
              <magic-input :focus="() => (parameterIndex = key)" :value.sync="item.description" style="width: 100%"/>
            </div>
          </div>
        </div>
      </div>
      <div v-if="showIndex == 1" class="ma-layout-container">
        <div class="ma-header ma-table-row">
          <div>Key</div>
          <div>Value</div>
          <div>Description</div>
        </div>
        <div class="ma-content">
          <div v-for="(item, key) in headers" :key="'request_header_' + key" class="ma-table-row">
            <div :class="{ focus: headerIndex == key && !item.name }">
              <magic-input :focus="() => (headerIndex = key)" :value.sync="item.name" style="width: 100%"/>
            </div>
            <div>
              <magic-input :focus="() => (headerIndex = key)" :value.sync="item.value" style="width: 100%"/>
            </div>
            <div>
              <magic-input :focus="() => (headerIndex = key)" :value.sync="item.description" style="width: 100%"/>
            </div>
          </div>
        </div>
      </div>
      <div v-if="showIndex < 2" class="not-select ma-sider">
        <div @click="addRow"><i class="ma-icon ma-icon-plus"/></div>
        <div @click="removeRow"><i class="ma-icon ma-icon-minus"/></div>
      </div>
    </div>
  </div>
</template>

<script>
import store from '@/scripts/store.js'
import MagicInput from '@/components/common/magic-input.vue'

export default {
  name: 'MagicSettings',
  props: {
    message: String
  },
  components: {
    MagicInput
  },
  data() {
    let parameters = JSON.parse(store.get('global-parameters') || '[]')
    let headers = JSON.parse(store.get('global-headers') || '[]')
    return {
      parameters,
      headers,
      navs: ['全局请求参数', '全局请求Header'],
      showIndex: 0,
      parameterIndex: 0,
      headerIndex: 0
    }
  },
  methods: {
    save() {
      store.set('global-parameters', this.parameters)
      store.set('global-headers', this.headers)
    },
    addRow() {
      if (this.showIndex == 0) {
        this.parameters.push({name: '', value: '', description: ''})
        this.parameterIndex = this.parameters.length - 1
      } else if (this.showIndex == 1) {
        this.headers.push({name: '', value: '', description: ''})
        this.headerIndex = this.headers.length - 1
      }
      this.$forceUpdate()
    },
    removeRow() {
      if (this.showIndex == 0) {
        this.parameters.splice(this.parameterIndex, 1)
        if (this.parameters.length == 0) {
          this.parameterIndex = 0
          this.addRow()
        } else if (this.parameters.length <= this.parameterIndex) {
          this.parameterIndex = this.parameters.length - 1
        }
      } else if (this.showIndex == 1) {
        this.headers.splice(this.headerIndex, 1)
        if (this.headers.length == 0) {
          this.headerIndex = 0
          this.addRow()
        } else if (this.headers.length <= this.headerIndex) {
          this.headerIndex = this.headers.length - 1
        }
      }
      this.$forceUpdate()
    }
  },
  watch: {
    headers: {
      deep: true,
      handler() {
        this.save()
      }
    },
    parameters: {
      deep: true,
      handler() {
        this.save()
      }
    }
  }
}
</script>

<style scoped>
.ma-settings {
  background: var(--background);
  height: 100%;
  width: 100%;
  position: relative;
  outline: 0;
}

.ma-settings .ma-layout {
  height: calc(100% - 25px);
}
</style>
