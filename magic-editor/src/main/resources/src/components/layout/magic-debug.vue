<template>
  <div class="ma-debug-container">
    <ul>
      <li :class="{ disabled: !debuging }" title="继续(F8)" @click="bus.$emit('doContinue')"><i
          class="ma-icon ma-icon-continue"/></li>
      <li :class="{ disabled: !debuging }" title="单步(F6)" @click="bus.$emit('doStepInto')"><i
          class="ma-icon ma-icon-step-over"/></li>
    </ul>
    <div>
      <table>
        <thead>
        <tr>
          <th>变量名</th>
          <th>变量值</th>
          <th>变量类型</th>
        </tr>
        </thead>
        <tbody>
        <tr v-if="variables.length === 0">
          <td align="center" colspan="3">no message.</td>
        </tr>
        <tr v-for="(item,key) in variables" :key="'debug_var_' + key">
          <td>{{ item.name }}</td>
          <td><magic-structure :data="item.value" :type="item.type"/></td>
          <td>{{ item.type }}</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import bus from '@/scripts/bus.js'
import MagicStructure from '@/components/common/magic-structure.vue'
export default {
  name: 'MagicDebug',
  props: {
    info: Object
  },
  components: { MagicStructure },
  data() {
    return {
      bus
    }
  },
  computed: {
    debuging() {
      return this.info && this.info.ext && this.info.ext.debuging
    },
    variables() {
      return this.info && this.info.ext && this.info.ext.variables || []
    }
  }
}
</script>

<style scoped>
.ma-debug-container {
  height: 100%;
  width: 100%;
  position: relative;
  background: var(--background);
}

ul {
  position: absolute;
  top: 0px;
  left: 0;
  width: 24px;
  bottom: 0;
  border-right: 1px solid var(--tab-bar-border-color);
  background: var(--background);
}

ul li {
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  cursor: pointer;
}

ul li:not(.disabled):hover {
  background: var(--hover-background);
}

ul li i {
  color: var(--button-disabled-background);
}

ul li:first-child:not(.disabled) i {
  color: var(--icon-debug-color);
}

ul li:last-child:not(.disabled) i {
  color: var(--icon-step-color);
}

.ma-debug-container > div {
  position: absolute;
  left: 24px;
  top: 0px;
  right: 0px;
  bottom: 0px;
  overflow: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

table tr th:not(:last-child),
table tr td:not(:last-child) {
  border-right: 1px solid var(--table-col-border-color);
}

table tr th, table tr td {
  border-bottom: 1px solid var(--table-row-border-color);
  height: 24px;
  line-height: 24px;
  padding-left: 5px;
}

table tr th {
  text-align: left;
}

table tbody tr:nth-child(even) {
  background: var(--table-even-background);
}

</style>
