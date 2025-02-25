<template>
  <div class="ma-options">
    <div class="ma-layout">
      <div class="not-select ma-sider">
        <div @click="addRow"><i class="ma-icon ma-icon-plus"/></div>
        <div @click="removeRow"><i class="ma-icon ma-icon-minus"/></div>
      </div>
      <div class="ma-layout-container">
        <div class="ma-header ma-table-row">
          <div>键</div>
          <div>值</div>
          <div>描述</div>
        </div>
        <div class="ma-content">
          <div v-for="(item, key) in info.option" :key="'request_parameter_' + key" class="ma-table-row">
            <div :class="{ focus: optionIndex === key && !item.name }">
              <magic-select :focus="() => (optionIndex = key)" :inputable="true" :options="defaultOptions"
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
</template>

<script>
import request from '@/api/request.js'
import MagicInput from '@/components/common/magic-input.vue'
import MagicSelect from '@/components/common/magic-select.vue'
import contants from "@/scripts/contants.js"

export default {
  name: 'MagicOption',
  props: {
    info: Object
  },
  components: {
    MagicInput,
    MagicSelect
  },
  data() {
    return {
      defaultOptions: null,
      optionsMap: {},
      optionIndex: 0
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
      if (this.info.option[index]) {
        this.info.option[index].name = value;
        let item = this.optionsMap[value];
        if (item) {
          if (item.description) {
            this.info.option[index].description = item.description
          }
          if (item.defaultValue) {
            this.info.option[index].value = item.defaultValue
          }
        }
      }
    },
    addRow() {
      if (!this.info.option) {
        this.$magicAlert({
          content: '请先添加或选择接口'
        })
        return
      }
      this.info.option.push({name: '', value: '', description: ''})
      this.optionIndex = this.info.option.length - 1
      this.$forceUpdate()
    },
    removeRow() {
      this.info.option.splice(this.optionIndex, 1)
      if (this.info.option.length === 0) {
        this.optionIndex = 0
        this.addRow()
      } else if (this.info.option.length <= this.optionIndex) {
        this.optionIndex = this.info.option.length - 1
      }
      this.$forceUpdate()
    }
  }
}
</script>

<style scoped>
.ma-options {
  background: var(--background);
  height: 100%;
  width: 100%;
  position: relative;
  overflow: hidden;
}
</style>
