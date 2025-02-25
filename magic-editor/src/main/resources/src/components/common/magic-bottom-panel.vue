<template>
  <div class="ma-bottom-content-item">
    <div class="not-select">
      <label>{{ title }}</label>
      <span v-for="button in buttons" :key="'ma_button_' + button.name" :title="button.name"><i
          :class="'ma-icon ma-icon' + button.icon"></i></span>
      <span title="最小化" @click="$emit('update:selectedTab', null)"><i class="ma-icon ma-icon-minimize"></i></span>
    </div>
    <div class="ma-item-body">
      <slot></slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MagicBottomPanel',
  props: {
    title: String,
    buttons: Array,
  },
}
</script>

<style scoped>
.ma-bottom-content-item {
  background: #fff;
  overflow: auto;
  height: 100%;
  position: relative;
  color: var(--color);
}

.ma-bottom-content-item > div:first-child {
  background: var(--background);
  border-bottom: 1px solid var(--tab-bar-border-color);
  height: 24px;
  line-height: 24px;
  padding-left: 10px;
}

.ma-bottom-content-item > div:first-child > span {
  color: var(--icon-color);
  cursor: pointer;
  padding: 0 3px;
  display: inline-block;
  height: 24px;
  line-height: 24px;
}

.ma-bottom-content-item > div:first-child > span:last-child {
  float: right;
  margin-right: 12px;
}

.ma-bottom-content-item .ma-item-body {
  position: absolute;
  top: 24px;
  bottom: 0px;
  width: 100%;
  overflow: auto;
}
</style>
