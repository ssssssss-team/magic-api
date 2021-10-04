<template>
  <magic-dialog v-model="visible" title="最近打开" align="right" :moveable="false" width="340px" height="420px" padding="0"
                className="ma-tree-wrapper">
    <template #content>
      <div style="height: 380px; overflow: auto">
        <div v-for="(it, i) in fullScripts" :key="i" @click="open(it)" class="ma-tree-item">
          <div class="ma-tree-hover" style="padding-left: 5px;">
            <i v-if="it._type === 'api'" :class="'ma-svg-icon request-method-' + it.method"></i>
            <i v-if="it._type === 'function'" class="ma-svg-icon icon-function"></i>
            <label>{{ displayText(it.groupName + '/' + it.name) }}({{ displayText(it.groupPath + '/' + it.path) }})</label>
          </div>
        </div>
        <div v-show="fullScripts.length === 0" class="no-data-tip">
          最近没有打开过的接口或函数
        </div>
      </div>
    </template>
  </magic-dialog>
</template>
<script>
import bus from '../../scripts/bus.js'
import Key from '@/scripts/hotkey.js'
import MagicDialog from '@/components/common/modal/magic-dialog.vue'
import store from '@/scripts/store.js'

export default {
  name: 'MagicRecentOpened',
  components: {MagicDialog},
  data() {
    return {
      visible: false,
      scripts: []
    }
  },
  mounted() {
    bus.$on('close', item => {
      if (item.id) {
        let index = this.scripts.findIndex(it => it[1] === item.id)
        if (index > -1) {
          this.scripts.splice(index, 1)
        }
        this.scripts.unshift([item._type, item.id])
        if(this.scripts.length > 30){
          this.scripts.splice(30, this.scripts.length)
        }
        store.set('recent_opened', this.scripts)
      }
    })
    let element = document.getElementsByClassName('ma-container')[0]
    // 最近快捷键
    Key.bind(element, Key.Ctrl | Key.E, () => this.show())
  },
  computed: {
    fullScripts() {
      const $parent = this.$parent.$refs
      let list = this.scripts.map(item => {
        if (item[0] === 'api') {
          return $parent.apiList.getItemById(item[1])
        } else if (item[0] === 'function') {
          return $parent.functionList.getItemById(item[1])
        }
      })
      let filtered = list.filter(it => it)
      if (filtered.length !== this.scripts.length) {
        this.$nextTick(() => {
          this.scripts = filtered.map(it => [it._type, it.id]);
          store.set('recent_opened', this.scripts)
        })
      }
      return list.filter(it => it);
    }
  },
  methods: {
    show() {
      let str = store.get('recent_opened')
      if (str) {
        try {
          this.scripts = JSON.parse(str)
        } catch (e) {
        }
      }
      this.visible = true
    },
    open(item) {
      bus.$emit('open', item)
      this.visible = false
    },
    displayText(str) {
      return str.replace(/\/+/g, '/')
    }
  }
}
</script>
<style>
@import './magic-resource.css';
.ma-dialog-content .no-data-tip {
  line-height: 380px;
  text-align: center;
}
</style>