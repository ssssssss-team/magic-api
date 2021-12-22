<template>
  <div class="ma-log-wrapper" @contextmenu.prevent="e=>onContextMenu(e)">
    <div ref="container" class="ma-log">
      <div v-for="(item, key) in logs" :class="{ multiple: item.multiple, more: item.showMore }" :key="'run_log_' + key">
        <pre v-html="item.html"></pre>
        <span v-if="item.multiple" class="multiple" @click="item.showMore = !item.showMore">
        {{ item.showMore ? '点击隐藏多行日志' : `有 ${item.lines} 行日志被隐藏 点击显示`}}
      </span>
      </div>
    </div>
  </div>
</template>

<script>
import bus from "@/scripts/bus";

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
      // escape
      let html = text.replace(/[&<>]/gm, function (str) {
        return str === "&" ? "&amp;" : str === "<" ? "&lt;" : str === ">" ? "&gt;" : "";
      });
      html = html.replace(/(\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2}.\d{3}\s+)([^\s]+)( --- \[)(.{15})(] )(.{40})/gm,'$1 <span class="log-$2">$2</span>$3$4$5<span class="log-cyan">$6</span>')
      // 替换链接
      html = html.replace(/(https?:\/\/[^\s]+)/gm, '<a class="log-link" href="$1" target="blank">$1</a>')
      // 处理异常里的 at
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
.ma-log-wrapper{
  position: relative;
  overflow: auto;
  width: 100%;
  height: 100%;
  background: var(--run-log-background);
  padding: 5px;
}
.ma-log {
  position: absolute;
  font-size: 13.5px;
  height: 100%;
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
.ma-log span.multiple{
  opacity: 0.5;
  font-size: 13px;
  text-decoration: underline;
  cursor: pointer
}
</style>
