<template>
    <magic-tree :data="tree" :forceUpdate="forceUpdate" :style="{ height, maxHeight}" style="overflow: auto" :loading="showLoading > 0">
      <template #folder="{ item }">
        <div
            :style="{ 'padding-left': 17 * item.level + 'px' }"
            :title="`${item.name||''}${item.parentId !== 'root' ? '(' + (item.path || '') + ')' : ''}`"
            class="ma-tree-item-header ma-tree-hover"
            @click.stop="$set(item,'opened',!item.opened)"
        >
          <magic-checkbox :value="item.id === selectedItem" :checked-half="item.checkedHalf" @change="doSelected(item)"/>
          <i :class="item.opened ? 'ma-icon-arrow-bottom' : 'ma-icon-arrow-right'" class="ma-icon" />
          <i class="ma-icon ma-icon-list"></i>
          <label>{{ item.name }}</label>
          <span v-if="item.parentId !== 'root'">({{ item.path }})</span>
        </div>
      </template>
    </magic-tree>
</template>

<script>
import bus from '../../scripts/bus.js'
import MagicTree from '../common/magic-tree.vue'
import request from '@/api/request.js'
import contants from '@/scripts/contants.js'
import MagicCheckbox from "@/components/common/magic-checkbox";

export default {
  name: 'MagicGroupChoose',
  props: {
    height: {
      type: String,
      required: true
    },
    maxHeight: {
      type: String,
      required: true
    },
    rootName: {
      type: String,
      required: true
    },
    type: {
      type: String,
      required: true
    }
  },
  components: {
    MagicCheckbox,
    MagicTree
  },
  data() {
    return {
      selectedItem: '',
      bus: bus,
      // 分组list数据
      listGroupData: [],
      // 分组+接口tree数据
      tree: [],
      // 数据排序规则,true:升序,false:降序
      treeSort: true,
      // 绑定给magic-tree组件，用来触发子组件强制更新
      forceUpdate: true,
      // 是否展示tree-loading,0表示不展示,大于0表示展示
      showLoading: 0
    }
  },
  methods: {
    // 初始化数据
    initData() {
      this.showLoading = 1
      this.tree = []
      this.listGroupData = [
          { id: '0',_type: 'root', name: this.rootName, parentId: 'root', path:'', selected: false, checkedHalf: false}
      ]
      request.send(`group/list?type=${this.type}`).success(data => {
        data = data || []
        this.listGroupData.push(...data.map(it => {
          it.selected = false;
          it.checkedHalf = false;
          it._type = 'group';
          return it;
        }))
        this.initTreeData()
        this.showLoading--
      })
    },
    // 初始化tree结构数据
    initTreeData() {
      // 1.把所有的分组id存map,方便接口列表放入,root为没有分组的接口
      let groupItem = {root: []}
      this.listGroupData.forEach(element => {
        groupItem[element.id] = []
        element.folder = true
        this.$set(element, 'opened', true)
        // 缓存一个name和path给后面使用
        element.tmpName = element.name.indexOf('/') === 0 ? element.name : '/' + element.name
        element.tmpPath = element.path.indexOf('/') === 0 ? element.path : '/' + element.path
      })
      // 3.将分组列表变成tree,并放入接口列表,分组在前,接口在后
      let arrayToTree = (arr, parentItem, groupName, groupPath, level) => {
        //  arr 是返回的数据  parendId 父id
        let temp = []
        let treeArr = arr
        treeArr.forEach((item, index) => {
          if (item.parentId === parentItem.id) {
            item.level = level
            item.tmpName = groupName + item.tmpName
            item.tmpPath = groupPath + item.tmpPath
            // 递归调用此函数
            item.children = arrayToTree(treeArr, item, item.tmpName, item.tmpPath, level + 1)
            if (groupItem[item.id]) {
              groupItem[item.id].forEach(element => {
                element.level = item.level + 1
                element.groupName = item.tmpName
                element.groupPath = item.tmpPath
                element.groupId = item.id
                this.$set(item.children, item.children.length, element)
              })
            }
            this.$set(temp, temp.length, treeArr[index])
          }
        })
        return temp
      }
      this.tree = [...arrayToTree(this.listGroupData, {id: 'root'}, '', '', 0), ...groupItem['root']]
      this.sortTree()
    },
    getSelected() {
      return this.selectedItem
    },
    doSelected(item) {
      this.selectedItem = item.id
    },
    // 排序tree,分组在前,接口在后
    sortTree() {
      if (this.treeSort === null) {
        return
      }
      let sortItem = function (item1, item2) {
        return item1.name.localeCompare(item2.name, 'zh-CN')
      }
      let sortHandle = arr => {
        // 分组
        let folderArr = []
        // 接口
        let fileArr = []
        arr.forEach(element => {
          if (element.folder === true) {
            if (element.children && element.children.length > 0) {
              element.children = sortHandle(element.children)
            }
            folderArr.push(element)
          } else {
            fileArr.push(element)
          }
        })
        folderArr.sort(sortItem)
        fileArr.sort(sortItem)
        if (this.treeSort === false) {
          folderArr.reverse()
          fileArr.reverse()
        }
        return folderArr.concat(fileArr)
      }
      this.tree = sortHandle(this.tree)
      this.changeForceUpdate()
    },
    // 强制触发子组件更新
    changeForceUpdate() {
      this.forceUpdate = !this.forceUpdate
    }
  }
}
</script>

<style>
@import './magic-resource.css';
.ma-tree-wrapper .ma-checkbox input + label{
  width: 12px !important;
  height: 12px !important;
}
.ma-tree-wrapper .ma-checkbox input + label::after{
  width: 12px !important;
  height: 12px !important;
  line-height: 12px !important;
  top: 0 !important;
  left: 0 !important;
}
</style>
<style scoped>
.ma-checkbox{
  display: inline-block;
  width: 20px;
  height: 12px;
}
.ma-tree-wrapper .ma-tree-container{
  height: 100%;
}
</style>
