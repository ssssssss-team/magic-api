<template>
  <div class="ma-event">
    <div class="ma-layout">
      <div class="not-select ma-sider">
        <div @click="clearLog"><i class="ma-icon ma-icon-clear" /></div>
      </div>
      <div class="ma-layout-container">
        <div class="ma-header ma-table-row">
          <div>时间</div>
          <div>事件内容</div>
        </div>
        <div class="ma-content">
          <div v-for="(item, key) in eventList" :key="'event_' + key" class="ma-table-row content-bg">
            <div>{{ item.timestamp }}</div>
            <div>{{ item.content }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import bus from '@/scripts/bus.js'
export default {
  name: 'MagicEvent',
  data() {
    return {
      eventList: [],
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    init(){
      this.eventList = bus.$getStatusLog();
      bus.$on('status', ()=>{
        this.$nextTick(()=> {
          this.eventList = bus.$getStatusLog();
        })
      })
      this.$forceUpdate()
    },
    clearLog(){
      bus.$clearStatusLog()
      this.$nextTick(()=> this.init())
    }
  }
}
</script>

<style scoped>
.ma-event {
  background: var(--background);
  height: 100%;
  width: 100%;
  position: relative;
  outline: 0;
}

.ma-event .ma-layout {
  height: 100%;
}

.ma-event .ma-layout .ma-content .content-bg span {
  color: var(--toolbox-list-span-color);
}

.ma-event .ma-layout .ma-content .content-bg:nth-child(even) {
  background: var(--table-even-background);
}
.ma-event .ma-layout .ma-content .content-bg:hover {
  background: var(--toolbox-list-hover-background);
}
.ma-layout .ma-table-row{
  display: flex;
}
.ma-layout .ma-table-row > * {
  width: 150px !important;
  background: none;
  padding:0 2px;
}

.ma-layout .ma-table-row > *:last-child {
  flex: 1;
  width: auto;
}
</style>
