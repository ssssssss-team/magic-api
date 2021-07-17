<template>
  <div class="ma-todo">
    <div class="ma-layout">
      <div class="not-select ma-sider">
        <div @click="getTodoList"><i class="ma-icon ma-icon-refresh" /></div>
      </div>
      <div class="ma-layout-container" v-show="!showLoading">
        <div class="ma-header ma-table-row">
          <div>名称</div>
          <div>行号：内容</div>
        </div>
        <div class="ma-content">
          <div v-for="(item, key) in todoList" :key="'todo_' + key" class="ma-table-row content-bg" @click="openItem(item)">
            <div>
              <i class="ma-svg-icon" v-if="item.type === 1" :class="['request-method-' + item.cache.method]" />
              <i class="ma-svg-icon" v-if="item.type === 2" :class="['icon-function']" />
              <label>{{ item.cache.name }}</label>
              <span>({{ item.cache.path }})</span>
            </div>
            <div>
              <label style="padding-left: 5px">{{ item.line }}：</label>
              <label class="todo-item" >{{ item.text }}</label>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="loading" v-show="showLoading">
      <div class="icon">
        <i class="ma-icon ma-icon-refresh "></i>
      </div>
      加载中...
    </div>
    <div class="no-data" v-show="!showLoading && (!todoList || todoList.length === 0)">暂无数据</div>
  </div>
</template>

<script>
import request from '@/api/request.js'
import bus from '@/scripts/bus.js'
export default {
  name: 'MagicTodo',
  data() {
    return {
      todoList: [],
      // 是否展示loading
      showLoading: false
    }
  },
  mounted() {
    bus.$on('login', ()=> this.getTodoList())
  },
  methods: {
    getTodoList() {
      this.showLoading = true
      request.send('todo').success(data => {
        this.todoList = data
        const $parent = this.$parent.$parent.$parent.$refs
        this.todoList.forEach(item => {
          let cache
          if (item.type === 1) {
            cache = $parent.apiList.getItemById(item.id)
          } else if (item.type === 2) {
            cache = $parent.functionList.getItemById(item.id)
          }
          item.cache = cache || {}
        })
        setTimeout(() => {
          this.showLoading = false
        }, 500)
      })
    },
    openItem(item) {
      const $parent = this.$parent.$parent.$parent.$refs
      if (item.type === 1) {
        $parent.apiList.openItemById(item.id)
      } else if (item.type === 2) {
        $parent.functionList.openItemById(item.id)
      }
    }
  }
}
</script>

<style scoped>
.ma-todo {
  background: var(--background);
  height: 100%;
  width: 100%;
  position: relative;
  outline: 0;
}

.ma-todo .ma-layout {
  height: 100%;
}

.ma-todo .ma-layout .ma-content .content-bg {
  cursor: pointer;
}

.ma-todo .ma-layout .ma-content .content-bg span {
  color: var(--toolbox-list-span-color);
}

.ma-todo .ma-layout .ma-content .content-bg:nth-child(even) {
  background: var(--table-even-background);
}
.ma-todo .ma-layout .ma-content .content-bg .todo-item{
  font-style: italic;
  color:var(--todo-color)
}
.ma-todo .ma-layout .ma-content .content-bg:hover {
  background: var(--toolbox-list-hover-background);
}
.ma-layout .ma-sider {
  border: none;
  border-right: 1px solid var(--tab-bar-border-color);
}

.ma-layout .ma-table-row > * {
  width: 30% !important;
  background: none;
}

.ma-layout .ma-table-row > *:last-child {
  width: 70% !important;
}

.ma-todo .loading i {
  color: var(--color);
  font-size: 20px;
}
.ma-todo .loading .icon {
  width: 20px;
  margin: 0 auto;
  animation: rotate 1s linear infinite;
}
.ma-todo .loading {
  color: var(--color);
  position: absolute;
  text-align: center;
  width: 100%;
  top: 50%;
  margin-top: -20px;
}
.ma-todo .no-data {
  color: var(--color);
  position: absolute;
  top: 50%;
  left: 50%;
  margin-left: -20px;
}
</style>
