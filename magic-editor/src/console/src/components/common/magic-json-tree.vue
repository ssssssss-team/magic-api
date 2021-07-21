<template>
   <ul class="ma-tree">
    <li v-for="(item, index) in jsonData" :key="index" @click="handleItemClick(item, index)">
      <!-- 解决子组件不强制刷新 -->
      <div v-show="forceUpdate"></div>
      <div class="item-inline"  :class="item.selected ? 'tree-item item-selected' : 'tree-item'">
        <magic-json-tree-format :item="item" :index="index" :dataLength="jsonData.length" :indentLevel="indentLevel"/>
        <div class="item-inline">
          <template  v-if="item.dataType === 'Object' || item.dataType === 'Array'" >
            <img :src="imgObject" v-if="item.dataType === 'Object'"/>
            <img :src="imgArray" v-else/>
          </template>

          {{item.level > 0 ? item.name : ''}}{{item.dataType !== 'Object' && item.dataType !== 'Array' && item.name !== '' ? ':' : ''}}
          <span :style="item.dataType | color">
              {{item.dataType === 'String' ? (item.value === 'null' || item.value == null ? 'null' : '"' + item.value + '"') : item.value}}
            </span>
        </div>
      </div>
      <template  v-if="item.dataType === 'Object' || item.dataType === 'Array'" >
        <magic-json-tree :jsonData="item.children" :indentLevel="indentLevel | createLevel(jsonData, item, index)" :forceUpdate="forceUpdate" v-on="$listeners"/>
      </template>
    </li>
  </ul>
</template>

<script>
  import MagicJsonTreeFormat from './magic-json-tree-format'
  import {deepClone} from "../../scripts/utils";

  export default {
    name: 'MagicJsonTree',
    data() {
      return {
        imgArray: require('../../assets/images/array.gif'),
        imgObject: require('../../assets/images/object.gif'),
      }
    },
    props: {
      jsonData: {
        type: Array,
        required: true
      },
      indentLevel: Array,
      forceUpdate: Boolean
    },
    filters: {
      createLevel(indentLevel, jsonData, item, index) {
        if (!indentLevel) {
          indentLevel = []
        }
        indentLevel[item.level] = jsonData.length === index + 1 ? 1 : 0
        return deepClone(indentLevel)
      },
      color(dataType) {
        let color = "color: #42b983; margin-left: 5px;";
        switch (dataType) {
          case 'Integer':
          case 'Double':
          case 'Long':
          case 'Short':
          case 'Float':
          case 'Byte':
            color = "color: #fc1e70; margin-left: 5px;";
            break;
          case 'Boolean':
            color = "color: #e08331; margin-left: 5px;";
            break;
        }
        return color;
      }
    },
    components: {
      MagicJsonTreeFormat
    },
    methods: {
      handleItemClick(item, index) {
        this.$emit('jsonClick', item)
        this.jsonData.map(item => {
          item.selected = false;
        })
        this.jsonData[index].selected = !this.jsonData[index].selected
        event.stopPropagation()
      }
    }
  }
</script>
<style>
  .ma-tree {
    font-size: 14px;
    line-height: 14px;
  }

  .ma-tree .tree-item {
    cursor: pointer;
  }

  .ma-tree .item-selected {
    background-color: var(--toolbox-list-hover-background);
  }

  .item-inline {
    display: flex;
    flex-direction: row;
    align-items: center;
  }
</style>
