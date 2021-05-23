<template>
  <div class="ma-tree-item">
    <slot v-if="item.folder" :item="item" name="folder"></slot>
    <slot v-else :item="item" name="file"></slot>
    <slot :item="item"></slot>
    <div v-show="item.opened" class="ma-tree-sub-items">
      <magic-tree-item v-for="(subItem, index) in data" :key="'ma_tree_item_' +index" :data="subItem.children"
                       :item="subItem">
        <template v-for="(value, key) in $scopedSlots" v-slot:[key]="{ item }">
          <slot :item="item" :name="key"></slot>
        </template>
      </magic-tree-item>
    </div>
  </div>
</template>

<script>

export default {
  name: 'MagicTreeItem',
  props: {
    item: Object,
    data: Array
  }
}
</script>
<style></style>
