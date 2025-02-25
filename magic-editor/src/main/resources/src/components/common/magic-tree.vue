<template>
  <div class="ma-tree-container">
    <!-- 解决子组件不强制刷新 -->
    <div v-show="forceUpdate"></div>
    <div v-show="!showLoading">
      <template v-for="item in data">
        <magic-tree-item :key="'tree_' + item.id" :data="item.children" :item="item">
          <template v-for="(value, key) in $scopedSlots" v-slot:[key]="{ item }">
            <slot :item="item" :name="key"></slot>
          </template>
        </magic-tree-item>
      </template>
    </div>
    <div class="loading" v-show="showLoading">
      <div class="icon">
        <i class="ma-icon ma-icon-refresh "></i>
      </div>
      加载中...
    </div>
    <div class="no-data" v-show="!showLoading && (!data || data.length === 0)">无数据</div>
  </div>
</template>

<script>
import MagicTreeItem from './magic-tree-item.vue'

export default {
  name: 'MagicTree',
  props: {
    data: Array,
    // 解决子组件不强制刷新
    forceUpdate: Boolean,
    loading: {
      type: Boolean,
      default: false
    },
    loadingTime: {
      type: Number,
      default: 500
    }
  },
  components: {
    MagicTreeItem
  },
  data() {
    return {
      showLoading: false
    }
  },
  watch: {
    loading(newVal) {
      if (newVal) {
        this.showLoading = newVal
      } else {
        setTimeout(() => {
          this.showLoading = newVal
        }, this.loadingTime)
      }
    }
  },
  mounted() {
    this.showLoading = this.loading
  }
}
</script>
<style>
.ma-tree-container {
  position: relative;
}
.ma-tree-container .loading i {
  color: var(--color);
  font-size: 20px;
}
.ma-tree-container .loading .icon .ma-icon {
  padding: 0;
}
.ma-tree-container .loading .icon {
  width: 20px;
  margin: 0 auto;
  line-height: normal;
  animation: rotate 1s linear infinite;
}
.ma-tree-container .loading {
  color: var(--color);
  position: absolute;
  text-align: center;
  width: 100%;
  top: 50%;
  margin-top: -20px;
}
.ma-tree-container .no-data {
  color: var(--color);
  position: absolute;
  top: 50%;
  left: 50%;
  margin-left: -20px;
}
</style>
