<template>
  <div class="ma-tree-container">
    <!-- 解决子组件不强制刷新 -->
    <div v-show="forceUpdate"></div>
    <template v-for="item in data">
      <magic-tree-item :key="'tree_' + item.id" :data="item.children" :item="item">
        <template v-for="(value, key) in $scopedSlots" v-slot:[key]="{ item }">
          <slot :item="item" :name="key"></slot>
        </template>
      </magic-tree-item>
    </template>
  </div>
</template>

<script>
import MagicTreeItem from './magic-tree-item.vue'

export default {
  name: 'MagicTree',
  props: {
    data: Array,
    // 解决子组件不强制刷新
    forceUpdate: Boolean
  },
  components: {
    MagicTreeItem
  }
}
</script>
<style></style>
