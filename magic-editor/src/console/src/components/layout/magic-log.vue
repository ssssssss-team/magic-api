<template>
  <div ref="container" class="ma-log" @contextmenu.prevent="e=>onContextMenu(e)">
    <div v-for="(item, key) in logs" :key="'run_log_' + key" :class="{collapse: item.newline&&!item.expand}">
      <div>{{ item.timestamp }}</div>
      <div :style="{color :'var(--log-'+ item.level.toLocaleLowerCase() + '-color)'}">
        {{ item.level }}
      </div>
      <i class="ma-icon" :class="{'ma-icon-expand': !item.expand, 'ma-icon-collapse': item.expand}" v-if="item.newline" @click.stop="doExpand(item)"></i>
      <div :class="{throwable: item.throwable === true}" v-html="item.message"></div>
    </div>
  </div>
</template>

<script>
import bus from "@/scripts/bus";
import * as utils from "@/scripts/utils";
import contants from "@/scripts/contants";

export default {
  name: "MagicLog",
  props: {
    info: Object
  },
  data(){
    return {
      logs: []
    }
  },
  mounted() {
    bus.$on('ws_log', rows => this.onLogReceived(rows[0]))
  },
  methods: {
    doExpand(item){
      item.expand = !item.expand;
    },
    onLogReceived(row){
      row.timestamp = utils.formatDate(new Date())
      let throwable = row.throwable
      delete row.throwable
      row.message = (row.message || '').replace(/ /g, '&nbsp;').replace(/\n/g,'<br>')
      row.expand = false;
      if (throwable) {
        row.message += throwable.replace(/ /g, '&nbsp;').replace(/\n/g,'<br>')
        row.throwable = true
      }
      row.newline = row.message.indexOf('<br>') > -1
      if(this.logs.length >= contants.LOG_MAX_ROWS){
        this.logs.shift()
      }
      this.logs.push(row)
      let container = this.$refs.container;
      this.$nextTick(() => container.scrollTop = container.scrollHeight)
    },
    onContextMenu(event) {
      this.$magicContextmenu({
        event,
        menus: [{
          label: '清空日志',
          onClick: () => this.logs.splice(0)
        }]
      })
    }
  },
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
  display: inline;
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

.ma-log i{
  margin-right: -10px;
  margin-left: -10px;
  font-size: 12px;
  width: 20px;
  display: inline-block;
  height: 20px;
  text-align: center;
}
.ma-log .collapse{
  height: 20px;
  line-height: 20px;
  overflow: hidden;
}
</style>
