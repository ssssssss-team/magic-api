<template>
  <div class="ma-bottom-container">
    <div v-show="selectedTab" ref="resizer" class="ma-resizer-y" @mousedown="doResizeY"></div>
    <div v-show="selectedTab" ref="content" :style="{ height: contentHeight }" class="ma-bottom-content-container">
      <magic-bottom-panel v-for="item in tabs" :key="'bottom_tab_content_' + item.id"
                          :buttons="item.buttons" :class="{ visible: selectedTab === item.id }" :selectedTab.sync="selectedTab"
                          :title="item.name">
        <component v-bind:is="item.commponent" :info.sync="info"/>
      </magic-bottom-panel>
    </div>
    <ul class="ma-bottom-tab not-select">
      <li v-for="item in tabs" :key="'bottom_tab_' + item.id" :class="{ selected: selectedTab === item.id }"
          @click="selectedTab = selectedTab === item.id ? null : item.id"><i :class="'ma-icon ma-icon-' + item.icon"/>{{
          item.name
        }}
      </li>
    </ul>
  </div>
</template>

<script>
import MagicBottomPanel from '@/components/common/magic-bottom-panel.vue'
import MagicRequest from './magic-request.vue'
import MagicOption from './magic-option.vue'
import MagicGroup from './magic-group.vue'
import MagicRun from './magic-run.vue'
import MagicDebug from './magic-debug.vue'
import MagicLog from './magic-log.vue'
import MagicSettings from './magic-settings.vue'
import MagicFunction from './magic-function.vue'
import bus from '@/scripts/bus.js'

export default {
  name: 'MagicOptions',
  data() {
    return {
      info: {},
      isApi: true,
      contentHeight: '300px',
      selectedTab: '',
      tabs: [],
      apiTabs: [
        {id: 'request', name: '接口信息', icon: 'parameter', commponent: MagicRequest},
        {id: 'options', name: '接口选项', icon: 'options', commponent: MagicOption},
        {id: 'result', name: '执行结果', icon: 'run', commponent: MagicRun},
        {id: 'debug', name: '调试信息', icon: 'debug-info', commponent: MagicDebug},
        {id: 'log', name: '运行日志', icon: 'log', commponent: MagicLog},
        {id: 'setting', name: '全局参数', icon: 'settings', commponent: MagicSettings}
      ],
      functionTabs: [
        {id: 'function', name: '函数信息', icon: 'parameter', commponent: MagicFunction},
      ],
      apiGroupTabs : [
        {id: 'group', name: '分组信息', icon: 'parameter', commponent: MagicGroup}
      ]
    }
  },
  mounted() {
    this.tabs = this.apiTabs
    bus.$on('api-group-selected', group => {
      this.info = group;
      if (this.tabs !== this.apiGroupTabs) {
        this.tabs = this.apiGroupTabs;
        this.selectedTab = this.tabs[0].id
      }
    })
    bus.$on('opened', info => {
      this.isApi = info._type === 'api';
      this.info = info
      if (this.isApi) {
        if (this.tabs !== this.apiTabs) {
          this.tabs = this.apiTabs;
          this.selectedTab = this.tabs[0].id
        }
        this.$nextTick(() => {
          bus.$emit('update-request-body', info.requestBody);
          bus.$emit('update-response-body', info.responseBody);
        })
      } else {
        if (this.tabs !== this.functionTabs) {
          this.tabs = this.functionTabs;
          this.selectedTab = this.tabs[0].id
        }
      }
    })
    bus.$on('switch-tab', target => {
      if(this.apiTabs.some(it => it.id === target)){
        this.tabs = this.apiTabs;
      }else if(this.functionTabs.some(it => it.id === target)){
        this.tabs = this.functionTabs;
      }else if(this.apiGroupTabs.some(it => it.id === target)){
        this.tabs = this.apiGroupTabs;
      }
      this.$set(this, 'selectedTab', target)
      bus.$emit('update-window-size')
    })
  },
  components: {
    MagicBottomPanel
  },
  methods: {
    doResizeY() {
      let box = this.$refs.content.getClientRects()[0]
      document.onmousemove = e => {
        if (e.clientY > 150) {
          var move = box.height - (e.clientY - box.y)
          if (move > 30) {
            this.contentHeight = move + 'px'
            bus.$emit('update-window-size')
          }
        }
      }
      document.onmouseup = () => {
        document.onmousemove = document.onmouseup = null
        this.$refs.resizer.releaseCapture && this.$refs.resizer.releaseCapture()
      }
      bus.$emit('update-window-size')
    }
  },
  watch: {
    selectedTab() {
      bus.$emit('update-window-size')
    }
  }
}
</script>

<style>
.ma-bottom-container {
  background: var(--background);
}

.ma-bottom-container .ma-bottom-content-container {
  border-bottom: 1px solid var(--tab-bar-border-color);
  height: 300px;
}

.ma-bottom-container .ma-bottom-content-container > div {
  display: none;
}

.ma-bottom-container .ma-bottom-content-container > .visible {
  display: block;
}

.ma-bottom-tab li {
  float: left;
  cursor: pointer;
  padding: 0 10px;
  height: 24px;
  line-height: 24px;
  color: var(--color);
}

.ma-bottom-tab li i {
  color: var(--icon-color);
  padding: 0 2px;
  display: inline-block;
}

.ma-bottom-tab li:hover {
  background: var(--hover-background);
}

.ma-bottom-tab li.selected {
  background: var(--selected-background);
}

.ma-resizer-y {
  position: absolute;
  width: 100%;
  height: 10px;
  margin-top: -5px;
  background: none;
  cursor: n-resize;
}

.ma-nav {
  border-bottom: 1px solid var(--tab-bar-border-color);
  height: 24px;
}

.ma-nav li {
  display: inline-block;
  height: 24px;
  line-height: 24px;
  padding: 0 10px;
  cursor: pointer;
}

.ma-nav li.selected {
  background: var(--selected-background);
}

.ma-nav li:hover:not(.selected) {
  background: var(--hover-background);
}

.ma-layout {
  display: flex;
  flex: auto;
  flex-direction: row;
  height: 100%;
}

.ma-layout .ma-layout-container {
  flex: auto;
  height: 100%;
}

.ma-layout .ma-header > * {
  padding: 0 2px;
  border-right: none !important;
}

.ma-layout .ma-table-row > * {
  display: inline-block;
  width: 60%;
  height: 23px;
  line-height: 23px;
  border-bottom: 1px solid var(--input-border-color);
  border-right: 1px solid var(--input-border-color);
  background: var(--background);
}

.ma-layout .ma-table-row input:focus {
  border-color: var(--input-border-foucs-color);
}

.ma-layout .ma-table-row input {
  border-color: transparent;
  border-top-color: transparent;
  border-right-color: transparent;
  border-bottom-color: transparent;
  border-left-color: transparent;
}

.ma-layout .ma-table-row > *:first-child,
.ma-layout .ma-table-row > *:last-child {
  width: 20%;
}

.ma-layout .ma-content {
  flex: auto;
  overflow-x: hidden;
  height: calc(100% - 50px);
}

.ma-layout .ma-sider {
  border-left: 1px solid var(--tab-bar-border-color);
}

.ma-layout .ma-sider > * {
  width: 18px;
  height: 18px;
  line-height: 18px;
  margin: 3px;
  text-align: center;
  padding: 0;
  color: var(--icon-color);
  border-radius: 2px;
}

.ma-layout .ma-sider > *:hover {
  background: var(--hover-background);
}

.ma-request-parameters > ul li {
  display: inline-block;
  height: 24px;
  line-height: 24px;
  padding: 0 10px;
  cursor: pointer;
}

.ma-request-parameters > ul li.selected {
  background: var(--selected-background);
}

.ma-request-parameters > ul li:hover {
  background: var(--hover-background);
}

.ma-request-parameters > ul {
  border-bottom: 1px solid var(--tab-bar-border-color);
  height: 24px;
}
</style>
