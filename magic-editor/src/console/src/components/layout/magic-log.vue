<template>
  <div ref="container" class="ma-log" @contextmenu.prevent="e=>onContextMenu(e)">
    <div v-for="(item, key) in logs" :key="'run_log_' + key">
      <div>{{ item.timestamp }}</div>
      <div v-show="!item.throwable" :style="{color :'var(--log-'+ item.level.toLocaleLowerCase() + '-color)'}">
        {{ item.level }}
      </div>
      <div :class="{throwable: item.throwable === true}" v-html="item.message"></div>
    </div>
  </div>
</template>

<script>
export default {
  name: "MagicLog",
  props: {
    info: Object
  },
  mounted() {

  },
  computed: {
    logs() {
      return this.info && this.info.ext && this.info.ext.logs || []
    }
  },
  methods: {
    onContextMenu(event) {
      this.$magicContextmenu({
        event,
        menus: [{
          label: '清空日志',
          onClick: () => this.info && this.info.ext && this.info.ext.logs && this.info.ext.logs.splice(0)
        }]
      })
    }
  },
  watch: {
    'info.ext.logs': {
      deep: true,
      handler(newVal) {
        let container = this.$refs.container;
        this.$nextTick(() => container.scrollTop = container.scrollHeight)
      }
    }
  }
};
</script>

<style scoped>
.ma-log {
  overflow: auto;
  font-size: 1.1em;
  height: 100%;
  background: var(--toolbox-background);
}

.ma-log > div > div {
  display: inline-block;
  line-height: 20px;
  white-space: nowrap;
}

.ma-log > div > div:first-child {
  padding: 0 5px;
}

.ma-log > div > div:nth-child(2) {
  width: 50px;
  padding: 0 5px;
  text-align: right;
  margin-left: 5px;
}

.ma-log > div > div:last-child {
  padding: 0 10px;
}

.ma-log .throwable {
  color: var(--log-error-color)
}
</style>
