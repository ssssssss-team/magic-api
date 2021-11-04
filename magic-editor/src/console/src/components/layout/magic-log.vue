<template>
  <div ref="container" class="ma-log" @contextmenu.prevent="e=>onContextMenu(e)">
    <div v-for="(item, key) in logs" :class="{ multiple: item.multiple, more: item.showMore }" :key="'run_log_' + key">
      <pre v-html="item.html"></pre>
      <span v-if="item.multiple" class="multiple" @click="item.showMore = !item.showMore">
        {{ item.showMore ? '点击隐藏多行日志' : `有 ${item.lines} 行日志被隐藏 点击显示`}}
      </span>
    </div>
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
      let text = row[0]
      let html = Anser.linkify(Anser.ansiToHtml(Anser.escapeForHtml(text)));
      // 替换链接为新标签页打开
      html = html.replace(/<a /g,'<a target="blank" ');
      html = html.replace(/(\tat .*\()(.*?:\d+)(\).*?[\r\n])/g,'$1<span style="color:#808080;text-decoration: underline;">$2</span>$3')
      let lines = text.split('\n').length;
      this.logs.push({
        html,
        multiple: lines > 3,
        lines: lines - 4,
        showMore: false
      })
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

.ma-log > div pre{
  line-height: 20px;
}
.ma-log > div.multiple pre{
  max-height: 60px;
  overflow: hidden;
}
.ma-log > div.multiple.more pre{
  max-height: none;
}
.ma-log >>> pre span{
  opacity: 1 !important;
}

.ma-log span.multiple{
  opacity: 0.5;
  font-size: 13px;
  text-decoration: underline;
  cursor: pointer
}
</style>
