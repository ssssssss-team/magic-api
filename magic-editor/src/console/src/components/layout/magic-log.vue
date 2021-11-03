<template>
  <div ref="container" class="ma-log" @contextmenu.prevent="e=>onContextMenu(e)">
    <pre v-for="(item, key) in logs" :key="'run_log_' + key" v-html="item">
    </pre>
  </div>
</template>

<script>
import bus from "@/scripts/bus";
import Anser from 'anser'

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
      let html = Anser.linkify(Anser.ansiToHtml(Anser.escapeForHtml(row[0])));
      // 替换链接为新标签页打开
      html = html.replace(/<a /g,'<a target="blank" ');
      html = html.replace(/(\tat .*\()(.*?:\d+)(\).*?[\r\n])/g,'$1<span style="color:#808080;text-decoration: underline;">$2</span>$3')
      this.logs.push(html)
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
  font-size: 13.5px;
  height: 100%;
  background: var(--run-log-background);
  padding-top: 5px;
  padding-left: 5px;
}

.ma-log pre{
  line-height: 20px;
}
.ma-log >>> pre span{
  opacity: 1 !important;
}
</style>
