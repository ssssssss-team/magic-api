<template>
    <magic-tree :data="tree" :forceUpdate="forceUpdate" :style="{ height, maxHeight}" style="overflow: auto" :loading="showLoading > 0">
      <template #folder="{ item }">
        <div
            :style="{ 'padding-left': 17 * item.level + 'px' }"
            :title="`${item.name||''}${item.parentId !== 'root' ? '(' + (item.path || '') + ')' : ''}`"
            class="ma-tree-item-header ma-tree-hover"
            @click.stop="$set(item,'opened',!item.opened)"
        >
          <magic-checkbox v-model="item.selected" :checked-half="item.checkedHalf" @change="e => doSelected(item,e)"/>
          <i :class="item.opened ? 'ma-icon-arrow-bottom' : 'ma-icon-arrow-right'" class="ma-icon" />
          <i class="ma-icon ma-icon-list"></i>
          <label>{{ item.name }}</label>
          <span v-if="item.parentId !== 'root'">({{ item.path }})</span>
        </div>
      </template>
      <template #file="{ item }">
        <div
            :style="{ 'padding-left': 17 * item.level + 'px' }"
            class="ma-tree-hover"
            :title="(item.name || '') + '(' + (item.path || '') + ')'"
            @click.stop="doSelected(item,item.selected = !item.selected)"
        >
          <magic-checkbox v-model="item.selected" @change="e => doSelected(item,e)"/>
          <i v-if="item._type === 'api'" class="ma-svg-icon" :class="['request-method-' + item.method]" />
          <i v-if="item._type === 'function'" class="ma-svg-icon icon-function" />
          <i v-if="item._type === 'datasource'" class="ma-icon ma-icon-datasource" />
          <label>{{ item.name }}</label>
          <span>({{ item.path }})</span>
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
  name: 'MagicResourceChoose',
  props: {
    height: {
      type: String,
      required: true
    },
    maxHeight: {
      type: String,
      required: true
    },
    refreshData: Boolean
  },
  components: {
    MagicCheckbox,
    MagicTree
  },
  data() {
    return {
      bus: bus,
      // 分组list数据
      listGroupData: [],
      // 接口list数据
      listChildrenData: [],
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
      this.showLoading = 3
      this.tree = []
      this.listChildrenData = []
      this.listGroupData = [
          { id: 'api',_type: 'root', name: '1.接口列表', parentId: 'root', path:'', selected: false, checkedHalf: false},
          { id: 'function',_type: 'root', name: '2.函数列表', parentId: 'root', path:'', selected: false, checkedHalf: false},
          { id: 'datasource',_type: 'root', name: '3.数据源', parentId: 'root', path:'', selected: false, checkedHalf: false}
      ]
      request.send('group/list?type=1').success(data => {
        data = data || []
        this.listGroupData.push(...data.map(it => {
          it.parentId = it.parentId == '0' ? 'api' : it.parentId;
          it.selected = false;
          it.checkedHalf = false;
          it._type = 'group';
          return it;
        }))
        request.send('list').success(data => {
          data = data || []
          this.listChildrenData.push(...data.map(it => {
            it._type = 'api';
            it.selected = false;
            return it;
          }))
          this.initTreeData()
          this.showLoading--
        })
      })
      request.send('group/list?type=2').success(data => {
        data = data || []
        this.listGroupData.push(...data.map(it => {
          it.parentId = it.parentId == '0' ? 'function' : it.parentId;
          it.selected = false;
          it.checkedHalf = false;
          it._type = 'group'
          return it;
        }))
        request.send('function/list').success(data => {
          data = data || []
          this.listChildrenData.push(...data.map(it => {
            it._type = 'function';
            it.selected = false;
            return it;
          }))
          this.initTreeData()
          this.showLoading--
        })
      })
      request.send('datasource/list').success(data => {
        data = data || []
        this.listChildrenData.push(...data.filter(it => it.id).map(it => {
          it._type = 'datasource';
          it.selected = false;
          it.path = it.key;
          it.groupId = 'datasource'
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
        this.$set(element, 'opened', contants.DEFAULT_EXPAND)
        // 缓存一个name和path给后面使用
        element.tmpName = element.name.indexOf('/') === 0 ? element.name : '/' + element.name
        element.tmpPath = element.path.indexOf('/') === 0 ? element.path : '/' + element.path
      })
      // 2.把所有的接口列表放入分组的children
      this.listChildrenData.forEach((element, index) => {
        element.tmp_id = element.id
        if (groupItem[element.groupId]) {
          groupItem[element.groupId].push(element)
        } else {
          element.groupName = ''
          element.groupPath = ''
          groupItem['root'].push(element)
        }
      })
      // 3.将分组列表变成tree,并放入接口列表,分组在前,接口在后
      let arrayToTree = (arr, parentItem, groupName, groupPath, level) => {
        //  arr 是返回的数据  parendId 父id
        let temp = []
        let treeArr = arr
        treeArr.forEach((item, index) => {
          if (item.parentId == parentItem.id) {
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
      let array = []
      let process = (node) => {
        array.push({
          type: node._type || 'group',
          id: node.id
        })
        node.children && node.children.filter(it => it.selected).forEach(it => process(it))
      }
      this.tree.filter(it => it.selected).forEach(it => process(it))
      return array
    },
    doSelectAll(flag) {
      let process = (node) => {
        node.selected = flag
        if(node.folder){
          node.checkedHalf = false
        }
        node.children && node.children.forEach(it => process(it))
      }
      this.tree.forEach(it => process(it))
    },
    doSelected(item,selected) {
      let process = node => {
        node.selected = selected
        node.checkedHalf = !selected
        node.children&&node.children.forEach(it => process(it))
      }
      item.selected = selected;
      item.children&&item.children.forEach(it => process(it))
      if(item.folder){
        item.checkedHalf = false
      }
      this.getParents(item.folder ? item.parentId : item.groupId).forEach(node => {
        node.selected = node.children.some(it => it.selected)
        node.checkedHalf = node.children.some(it => !it.selected || it.checkedHalf)
      })
    },
    // 重新构建tree的path和name,第一个参数表示是否全部折叠
    rebuildTree(folding) {
      let buildHandle = (arr, parentItem, level) => {
        arr.forEach(element => {
          element.level = level
          // 处理分组
          if (element.folder === true) {
            element.tmpName = (parentItem.tmpName + '/' + element.name).replace(new RegExp('(/)+', 'gm'), '/')
            element.tmpPath = (parentItem.tmpPath + '/' + element.path).replace(new RegExp('(/)+', 'gm'), '/')
            if (folding === true) {
              this.$set(element, 'opened', false)
            }
            if (element.children && element.children.length > 0) {
              buildHandle(element.children, element, level + 1)
            }
          } else {
            // 处理接口
            element.groupName = parentItem.tmpName
            element.groupPath = parentItem.tmpPath
            element.groupId = parentItem.id
          }
        })
      }
      buildHandle(this.tree, {tmpName: '', tmpPath: ''}, 0)
      this.sortTree()
    },
    treeSortHandle(flag) {
      this.treeSort = !this.treeSort
      this.sortTree()
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
    // 将文件类型的对象，放入到点击的同级
    pushFileItemToGroup(tree, newItem) {
      // 标记是否找到对应的item，找到了就退出递归
      let find = false
      for (const index in tree) {
        const element = tree[index]
        // 排除分组类型
        if (element.folder === true && element.id === newItem.groupId) {
          this.$set(element.children, element.children.length, newItem)
          this.changeForceUpdate()
          return true
        } else if (element.children && element.children.length > 0) {
          find = this.pushFileItemToGroup(element.children, newItem)
        }
        if (find === true) {
          return true
        }
      }
    },
    // 强制触发子组件更新
    changeForceUpdate() {
      this.forceUpdate = !this.forceUpdate
    },
    getParents(id){
      let findId = id;
      let result = [];
      let handle= (items) => {
        items.forEach(item => {
          if (item.id === findId) {
            result.push(item)
            findId = item.parentId;
            if(item.id !== 'root'){
              handle(this.tree)
            }
          } else if (item.children && item.children.length > 0) {
            handle(item.children)
          }
        })
      }
      handle(this.tree)
      return result
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
