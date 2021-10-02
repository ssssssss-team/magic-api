<template>
  <div class="ma-tree-wrapper">
    <div class="ma-tree-toolbar">
      <div class="ma-tree-toolbar-search"><i class="ma-icon ma-icon-search"></i><input placeholder="输入关键字搜索"
                                                                                       @input="e => doSearch(e.target.value)"/>
      </div>
      <div>
        <div class="ma-tree-toolbar-btn" title="新建分组" @click="openCreateGroupModal()">
          <i class="ma-icon ma-icon-group-add"></i>
        </div>
        <div class="ma-tree-toolbar-btn" title="刷新接口" @click="initData()">
          <i class="ma-icon ma-icon-refresh"></i>
        </div>
        <div class="ma-tree-toolbar-btn" title="折叠" @click="rebuildTree(true)">
          <i class="ma-icon ma-icon-folding"></i>
        </div>
        <div v-show="treeSort === false" class="ma-tree-toolbar-btn" title="当前为字母降序" @click="treeSortHandle()">
          <i class="ma-icon ma-icon-descending"></i>
        </div>
        <div v-show="treeSort !== false" class="ma-tree-toolbar-btn" title="当前为字母升序" @click="treeSortHandle()">
          <i class="ma-icon ma-icon-ascending"></i>
        </div>
      </div>
    </div>
    <magic-tree :data="tree" :forceUpdate="forceUpdate" :loading="showLoading">
      <template #folder="{ item }">
        <div
            v-if="item._searchShow !== false"
            :id="'magic-api-list-' + item.id"
            :class="{ 'ma-tree-select': item.selectRightItem }"
            :draggable="true"
            :style="{ 'padding-left': 17 * item.level + 'px' }"
            :title="(item.name || '') + '(' + (item.path || '') + ')'"
            class="ma-tree-item-header ma-tree-hover"
            @click="bus.$emit('api-group-selected', item)"
            @dblclick="$set(item, 'opened', !item.opened)"
            @dragenter="e => draggable(item, e, 'dragenter')"
            @contextmenu.prevent="e => folderRightClickHandle(e, item)"
            @dragstart.stop="e => draggable(item, e, 'dragstart')"
            @dragend.stop="e => draggable(item, e, 'dragend')"
            @dragover.prevent
        >
          <i :class="item.opened ? 'ma-icon-arrow-bottom' : 'ma-icon-arrow-right'" class="ma-icon" @click="$set(item, 'opened', !item.opened)"/>
          <i class="ma-icon ma-icon-list"></i>
          <label>{{ item.name }}</label>
          <span>({{ item.path }})</span>
        </div>
      </template>
      <template #file="{ item }">
        <div
            v-if="item._searchShow !== false"
            :class="{ 'ma-tree-select': item.selectRightItem || item.tmp_id === currentFileItem.tmp_id }"
            :draggable="true"
            :style="{ 'padding-left': 17 * item.level + 'px' }"
            class="ma-tree-hover"
            :title="item.method + ':' + (item.name || '') + '(' + (item.path || '') + ')'"
            @click="open(item)"
            @dragenter="e => draggable(item, e, 'dragenter')"
            @contextmenu.prevent="e => fileRightClickHandle(e, item)"
            @dragstart.stop="e => draggable(item, e, 'dragstart')"
            @dragend.stop="e => draggable(item, e, 'dragend')"
            @dragover.prevent
        >
          <i class="ma-svg-icon" :class="['request-method-' + item.method]" />
          <label>{{ item.name }}</label>
          <span>({{ item.path }})</span>
          <i class="ma-icon ma-icon-lock" v-if="item.lock === '1'"></i>
        </div>
      </template>
    </magic-tree>
    <magic-dialog v-model="createGroupObj.visible" :title="createGroupObj.id ? '修改分组:' + createGroupObj.tmpName : '创建分组'"
                  align="right"
                  @onClose="createGroupAction(false)">
      <template #content>
        <label>分组名称：</label>
        <magic-input v-model="createGroupObj.name"/>
        <div style="height: 2px"></div>
        <label>分组前缀：</label>
        <magic-input v-model="createGroupObj.path"/>
      </template>
      <template #buttons>
        <button class="ma-button active" @click="createGroupAction(true)">{{ createGroupObj.id ? '修改' : '创建' }}</button>
        <button class="ma-button" @click="createGroupAction(false)">取消</button>
      </template>
    </magic-dialog>
    <magic-dialog v-model="groupChooseVisible" title="复制分组" align="right" :moveable="false" width="340px" height="390px"
                  className="ma-tree-wrapper">
      <template #content>
        <magic-group-choose ref="groupChoose" rootName="接口分组" type="1" height="300px" max-height="300px"/>
      </template>
      <template #buttons>
        <button class="ma-button active" @click="copyGroup">复制</button>
      </template>
    </magic-dialog>
  </div>
</template>

<script>
import bus from '../../scripts/bus.js'
import MagicTree from '../common/magic-tree.vue'
import request from '@/api/request.js'
import MagicDialog from '@/components/common/modal/magic-dialog.vue'
import MagicInput from '@/components/common/magic-input.vue'
import MagicGroupChoose from '@/components/resources/magic-group-choose.vue'
import { replaceURL, download as downloadFile, requestGroup, goToAnchor, deepClone } from '@/scripts/utils.js'
import contants from '@/scripts/contants.js'
import Key from '@/scripts/hotkey.js'

export default {
  name: 'MagicApiList',
  props: {
    apis: Array
  },
  components: {
    MagicTree,
    MagicDialog,
    MagicInput,
    MagicGroupChoose
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
      groupChooseVisible: false,
      recentlyOpenedVisible: false,
      srcId: '',
      // 新建分组对象
      createGroupObj: {
        visible: false,
        id: '',
        name: '',
        path: '',
        parentId: '',
        type: '1',
        children: []
      },
      // 换成一个临时对象，修改使用
      tempGroupObj: {},
      // 当前打开的文件
      currentFileItem: {},
      // 绑定给magic-tree组件，用来触发子组件强制更新
      forceUpdate: true,
      // 拖拽的item
      draggableItem: {},
      draggableTargetItem: {},
      // 是否展示tree-loading
      showLoading: true,
      // 缓存一个openId
      tmpOpenId: []
    }
  },
  methods: {
    doSearch(keyword) {
      keyword = keyword.toLowerCase();
      let loopSearch = (row, parentName, parentPath) => {
        if (row.folder) {
          row.children.forEach(it => loopSearch(it, parentName + '/' + (row.name || ''), parentPath + '/' + (row.path || '')))
          row._searchShow = (row.name || '').toLowerCase().indexOf(keyword) > -1 || row.children.some(it => it._searchShow)
        } else {
          row._searchShow = replaceURL(parentName + '/' + (row.name || '')).toLowerCase().indexOf(keyword) > -1 || replaceURL(parentPath + '/' + (row.path || '')).toLowerCase().indexOf(keyword) > -1
        }
      }
      this.tree.forEach(it => loopSearch(it, '', ''))
      this.changeForceUpdate()
    },
    open(item) {
      bus.$emit('status', `查看接口「${item.name}(${item.path})」详情`)
      bus.$emit('open', item)
      this.currentFileItem = item
    },
    recentlyOpened(item){
      this.open(item)
      this.recentlyOpenedVisible = false
    },
    // 初始化数据
    initData() {
      bus.$emit('status', '正在初始化接口列表')
      this.showLoading = true
      this.tree = []
      return new Promise((resolve) => {
        request.send('group/list?type=1').success(data => {
          this.listGroupData = data || []
          bus.$emit('status', '接口分组加载完毕')
          request.send('list').success(data => {
            this.listChildrenData = data || []
            this.initTreeData()
            this.openItemById()
            this.showLoading = false
            bus.$emit('status', '接口信息加载完毕')
            resolve()
          })
        })
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
        element._type = 'api'
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
      this.tree = [...arrayToTree(this.listGroupData, {id: 0}, '', '', 0), ...groupItem['root']]
      this.sortTree()
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
            this.$set(element, 'opened', folding !== true)
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
      if (this.currentFileItem.tmp_id) {
        this.open(this.currentFileItem)
      }
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
    // 文件夹右键菜单
    folderRightClickHandle(event, item) {
      this.$set(item, 'selectRightItem', true)
      this.$magicContextmenu({
        menus: [
          {
            label: '新建接口',
            icon: 'ma-icon-plus',
            onClick: () => {
              let newItem = {
                id: '',
                tmp_id: new Date().getTime() + '' + Math.floor(Math.random() * 1000),
                _type: 'api',
                method: '',
                path: '',
                script: null,
                name: '未定义名称',
                parameters: null,
                paths: null,
                option: null,
                lock: '0',
                requestBody: null,
                headers: null,
                responseBody: null,
                responseHeader: null,
                description: null,
                level: item.level + 1,
                groupName: item.tmpName,
                groupPath: item.tmpPath,
                groupId: item.id
              }
              this.pushFileItemToGroup(this.tree, newItem)
              this.open(newItem)
            }
          },
          {
            label: '刷新接口',
            icon: 'ma-icon-refresh',
            divided: true,
            onClick: () => {
              this.initData()
            }
          },
          {
            label: '新建分组(Alt+G)',
            icon: 'ma-icon-group-add',
            onClick: () => {
              this.openCreateGroupModal(null, item)
            }
          },
          {
            label: '修改分组',
            icon: 'ma-icon-update',
            onClick: () => {
              this.openCreateGroupModal(item)
            }
          },
          {
            label: '复制分组',
            icon: 'ma-icon-copy',
            onClick: () => {
              this.srcId = item.id
              this.groupChooseVisible = true
              this.$refs.groupChoose.initData()
            }
          },
          {
            label: '删除分组',
            icon: 'ma-icon-delete',
            divided: true,
            onClick: () => {
              this.deleteGroupAction(item)
            }
          },
          {
            label: '移动到根节点',
            icon: 'ma-icon-move',
            onClick: () => {
              item.parentId = '0'
              bus.$emit('status', `准备移动接口分组「${item.name}」至根节点`)
              requestGroup('group/update', item).success(data => {
                bus.$emit('report', 'group_update')
                // 先删除移动前的分组
                this.deleteOrAddGroupToTree(this.tree, item, true)
                // 再把移动后的分组放进去
                this.deleteOrAddGroupToTree(this.tree, item)
                this.rebuildTree()
                this.initCreateGroupObj()
                this.changeForceUpdate()
                bus.$emit('status', `接口分组「${item.name}」已移动至根节点`)
              })
            }
          },
          {
            label: '导出',
            icon: 'ma-icon-download',
            onClick: () => {
              bus.$emit('status', `准备导出接口分组「${item.name}」相关接口`)
              request.send(`/download?groupId=${item.id}`,null,{
                headers: {
                  'Content-Type': 'application/json'
                },
                responseType: 'blob'
              }).success(blob => {
                downloadFile(blob,`${item.name}.zip`)
                bus.$emit('status', `接口分组「${item.name}」相关接口已导出`)
              })
            }
          }
        ],
        event,
        zIndex: 9999,
        destroy: () => {
          this.$set(item, 'selectRightItem', false)
        }
      })
      return false
    },
    // 文件右键菜单
    fileRightClickHandle(event, item) {
      this.$set(item, 'selectRightItem', true)
      this.$magicContextmenu({
        menus: [
          {
            label: '复制接口',
            icon: 'ma-icon-copy',
            onClick: () => {
              if (!item.id) {
                this.$magicAlert({content: '请先保存在复制！'})
                return
              }
              bus.$emit('status', `复制接口「${item.name}」`)
              let newItem = {
                ...deepClone(item),
                copy: true
              }
              newItem.name = newItem.name + '(复制)'
              newItem.tmp_id = new Date().getTime() + '' + Math.floor(Math.random() * 1000)
              newItem.selectRightItem = false
              this.pushFileItemToGroup(this.tree, newItem)
              this.open(newItem)
            }
          },
          {
            label: '复制路径',
            icon: 'ma-icon-copy',
            divided: true,
            onClick: () => {
              this.copyPathToClipboard(item)
            }
          },
          {
            label: '复制相对路径',
            icon: 'ma-icon-copy',
            divided: true,
            onClick: () => {
              this.copyPathToClipboard(item, true)
            }
          },
          {
            label: `${item.lock === '1' ? '解锁' : '锁定'}`,
            icon: `ma-icon-${item.lock === '1' ? 'unlock' : 'lock'}`,
            onClick: () => {
              let action = item.lock === '1' ? '解锁接口' : '锁定接口';
              request.send(item.lock === '1' ? 'unlock' : 'lock', {id: item.id}).success(data => {
                if (data) {
                  bus.$emit('status', `${action}「${item.name}(${item.path})」`)
                  bus.$emit('report', `api_${item.lock === '1' ? 'unlock' : 'lock'}`)
                  item['lock'] = item.lock === '1' ? '0' : '1';
                  this.changeForceUpdate()
                } else {
                  this.$magicAlert({content: `${action}失败`})
                }
              })
            }
          },
          {
            label: '刷新接口',
            icon: 'ma-icon-refresh',
            divided: true,
            onClick: () => {
              this.initData()
            }
          },
          {
            label: '删除接口',
            icon: 'ma-icon-delete',
            divided: true,
            onClick: () => {
              this.deleteApiInfo(item)
            }
          }
        ],
        event,
        zIndex: 9999,
        destroy: () => {
          this.$set(item, 'selectRightItem', false)
        }
      })
      return false
    },
    copyGroup(){
      let target = this.$refs.groupChoose.getSelected()
      if(target && this.srcId){
        this.groupChooseVisible = false
        request.send('group/copy', { src: this.srcId, target }).success(() => {
          this.initData();
        })
      }
    },
    // 删除接口
    deleteApiInfo(item) {
      bus.$emit('status', `准备删除接口「${item.name}(${item.path})」`)
      this.$magicConfirm({
        title: '删除接口',
        content: `是否要删除接口「${item.name}(${item.path})」`,
        onOk: () => {
          if (item.id) {
            request.send('delete', {id: item.id}).success(data => {
              if (data) {
                bus.$emit('status', `接口「${item.name}(${item.path})」已删除`)
                bus.$emit('report', 'script_delete')
                item['delete'] = true
                this.open(item)
                this.deleteOrAddGroupToTree(this.tree, item, true)
              } else {
                this.$magicAlert({content: '删除失败'})
              }
            })
          } else {
            item['delete'] = true
            this.open(item)
            this.deleteOrAddGroupToTree(this.tree, item, true)
          }
        }
      })
    },
    // 打开新建分组弹窗
    openCreateGroupModal(item, parentItem) {
      if (item) {
        for (const key in this.createGroupObj) {
          this.createGroupObj[key] = item[key]
        }
        this.createGroupObj.tmpName = this.createGroupObj.name
        this.tempGroupObj = item
      }
      if (parentItem) {
        this.createGroupObj.parentId = parentItem.id
      }
      this.createGroupObj.visible = true
    },
    // 保存|修改分组
    createGroupAction(flag) {
      if (flag === true) {
        // 验证数据
        if (!this.createGroupObj.name) {
          this.$magicAlert({content: '分组名称不能为空'})
          return false
        }
        // id存在发送更新请求，不存在发送新增请求
        if (this.createGroupObj.id) {
          requestGroup('group/update', this.createGroupObj).success(data => {
            bus.$emit('report', 'group_update')
            this.tempGroupObj.name = this.createGroupObj.name
            this.tempGroupObj.path = this.createGroupObj.path
            this.rebuildTree()
            this.initCreateGroupObj()
            this.tempGroupObj = {}
          })
        } else {
          requestGroup('group/create', this.createGroupObj).success(data => {
            this.createGroupObj.id = data
            this.createGroupObj.folder = true
            this.createGroupObj.paths = []
            this.createGroupObj.options = []
            bus.$emit('report', 'group_create')
            bus.$emit('status', `分组「${this.createGroupObj.name}」创建成功`)
            this.deleteOrAddGroupToTree(this.tree, this.createGroupObj)
            this.rebuildTree()
            const id = this.createGroupObj.id
            this.$nextTick(() => {
              goToAnchor('#magic-api-list-' + id)
            })
            this.initCreateGroupObj()
          })
        }
      } else {
        this.initCreateGroupObj()
      }
    },
    // 初始化createGroupObj对象
    initCreateGroupObj() {
      this.createGroupObj = {
        visible: false,
        id: '',
        name: '',
        path: '',
        parentId: '',
        type: '1',
        children: [],
        paths: [],
        options: []
      }
    },
    // 删除分组
    deleteGroupAction(item) {
      bus.$emit('status', `准备删除接口分组「${item.name}」`)
      this.$magicConfirm({
        title: '删除接口分组',
        content: `是否要删除接口分组「${item.name}」`,
        onOk: () => {
          request.send('group/delete', {groupId: item.id}).success(data => {
            if (data) {
              bus.$emit('report', 'group_delete')
              bus.$emit('status', `接口分组「${item.name}」已删除`)
              // 递归通知编辑页面关闭tab
              let noticeCloseTab = arr => {
                arr.forEach(element => {
                  if (element.folder !== true) {
                    element['delete'] = true
                    this.open(element)
                  } else if (element.children && element.children.length > 0) {
                    noticeCloseTab(element.children)
                  }
                })
              }
              noticeCloseTab(item.children || [])
              this.deleteOrAddGroupToTree(this.tree, item, true)
            } else {
              this.$magicAlert({content: '删除失败'})
            }
          })
        }
      })
    },
    // 复制接口路径到剪贴板
    copyPathToClipboard(fileItem, relative) {
      let path = replaceURL((relative ? '' : contants.SERVER_URL) + '/' + fileItem.groupPath + '/' + fileItem.path)
      try {
        var copyText = document.createElement('textarea')
        copyText.style = 'position:absolute;left:-99999999px'
        document.body.appendChild(copyText)
        copyText.innerHTML = path
        copyText.readOnly = false
        copyText.select()
        document.execCommand('copy')
        bus.$emit('status', `接口路径「${path}」复制成功`)
      } catch (e) {
        this.$magicAlert({title: '复制接口路径失败，请手动复制', content: path})
        console.error(e)
      }
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
    // 处理删除和添加分组
    deleteOrAddGroupToTree(tree, item, flag) {
      // 如果是添加到根目录
      if (flag !== true && item.folder === true && (!item.parentId || item.parentId === '0')) {
        item.level = 0
        item.tmpName = ('/' + item.name).replace(new RegExp('(/)+', 'gm'), '/')
        item.tmpPath = ('/' + item.path).replace(new RegExp('(/)+', 'gm'), '/')
        item.folder = true
        this.$set(item, 'opened', contants.DEFAULT_EXPAND)
        this.$set(item, 'selectRightItem', false)
        tree.push(item)
        return true
      }
      // 标记是否找到对应的item，找到了就退出递归
      let find = false
      for (const index in tree) {
        const element = tree[index]
        // 分组
        if (item.folder === true) {
          // 排除分组类型
          if (flag === true && element.folder === true && element.id === item.id) {
            // 删除
            tree.splice(index, 1)
            find = true
            this.changeForceUpdate()
          } else if (flag !== true && element.folder === true && element.id === item.parentId) {
            // 新增
            item.level = element.level + 1
            item.tmpName = (element.tmpName + '/' + item.name).replace(new RegExp('(/)+', 'gm'), '/')
            item.tmpPath = (element.tmpPath + '/' + item.path).replace(new RegExp('(/)+', 'gm'), '/')
            item.folder = true
            this.$set(item, 'opened', contants.DEFAULT_EXPAND)
            this.$set(item, 'selectRightItem', false)
            element.children.push(item)
            find = true
            this.changeForceUpdate()
          } else if (element.children && element.children.length > 0) {
            find = this.deleteOrAddGroupToTree(element.children, item, flag)
          }
        } else {
          // 接口
          if (flag === true && element.folder !== true && element.id === item.id) {
            // 删除
            tree.splice(index, 1)
            find = true
            this.changeForceUpdate()
          } else if (flag !== true && element.folder === true && element.id === item.groupId) {
            // 新增
            item.level = element.level + 1
            item.groupName = element.tmpName
            item.groupPath = element.tmpPath
            element.children.push(item)
            find = true
            this.changeForceUpdate()
          } else if (element.children && element.children.length > 0) {
            find = this.deleteOrAddGroupToTree(element.children, item, flag)
          }
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
    draggable(item, event, type) {
      switch (type) {
          // 开始拖拽
        case 'dragstart':
          this.draggableItem = item
          break
          // 拖拽到某个元素上
        case 'dragenter':
          this.draggableTargetItem = item
          break
          // 结束拖拽
        case 'dragend':
          // 目标对象必须是分组
          if (this.draggableTargetItem.folder === true) {
            // 移动分组
            if (this.draggableItem.folder === true && this.draggableItem.parentId !== this.draggableTargetItem.id && this.draggableItem.id !== this.draggableTargetItem.id) {
              // 检测移动的目录是否是自己的子目录
              let checkChildrenFolder = arr => {
                let flag = arr.some(item => item.id === this.draggableTargetItem.id)
                if (flag) {
                  return flag
                }
                for (let i = 0; i < arr.length; i++) {
                  if (arr[i].children && arr[i].children.length > 0) {
                    if (checkChildrenFolder(arr[i].children || [])) {
                      return true
                    }
                  }
                }
                return false
              }
              if (checkChildrenFolder(this.draggableItem.children) === false) {
                let params = JSON.parse(JSON.stringify(this.draggableItem))
                params.parentId = this.draggableTargetItem.id
                bus.$emit('status', `准备移动接口分组「${params.name}」`)
                requestGroup('group/update', params).success(data => {
                  bus.$emit('report', 'group_update')
                  // 先删除移动前的分组
                  this.deleteOrAddGroupToTree(this.tree, this.draggableItem, true)
                  // 再把移动后的分组放进去
                  this.deleteOrAddGroupToTree(this.tree, params)
                  this.rebuildTree()
                  this.initCreateGroupObj()
                  this.changeForceUpdate()
                  bus.$emit('status', `接口分组「${params.name}」移动成功`)
                })
              } else {
                this.$magicAlert({content: `不能移到${this.draggableTargetItem.name}`})
              }
            } else if (this.draggableItem.parentId !== this.draggableTargetItem.id) {
              // 移动接口
              // 接口不能在目标分组的第一级children里
              if (this.draggableTargetItem.children.some(item => item.id === this.draggableItem.id) === false) {
                bus.$emit('status', `准备移动接口「${this.draggableItem.name}」`)
                request.send('api/move', {
                  id: this.draggableItem.id,
                  groupId: this.draggableTargetItem.id
                }).success(data => {
                  // 先删除移动前的接口
                  this.deleteOrAddGroupToTree(this.tree, this.draggableItem, true)
                  // 再把移动后的接口放进去
                  this.draggableItem.groupId = this.draggableTargetItem.id
                  this.deleteOrAddGroupToTree(this.tree, this.draggableItem)
                  this.rebuildTree()
                  this.initCreateGroupObj()
                  this.changeForceUpdate()
                  bus.$emit('status', `接口「${this.draggableItem.name}」移动成功`)
                })
              }
            }
          }
          break
      }
    },
    // 根据id获取item
    getItemById(id) {
      function handle(items) {
        for(const index in items) {
          const item = items[index]
          if (item.id === id) {
            return item
          } else if (item.children && item.children.length > 0) {
            let find = handle(item.children)
            if (find) {
              return find
            }
          }
        }
      }
      return handle(this.tree)
    },
    getGroupsById(id){
      let item = this.getItemById(id);
      const items = [];
      while(item){
        items.push(item)
        if(item.parentId === '0'){
          break;
        }
        item = this.getItemById(item.parentId)
      }
      return items
    },
    position(id){
      this.$nextTick(()=> {
        this.rebuildTree(false)
        this.listChildrenData.forEach(item => item.selectRightItem = item.id === id || item.tmp_id === id)
        goToAnchor('.ma-tree-select')
      })
    },
    // 根据id打开对应item
    openItemById(openId) {
      // 证明当前请求还没有请求到数据
      if (this.listChildrenData.length === 0) {
        this.tmpOpenId.push(openId)
      } else {
        if (!this.tmpOpenId.includes(openId)) {
          this.tmpOpenId.push(openId)
        }
        this.tmpOpenId.forEach(id => {
          const cache = this.getItemById(id)
          if (cache) {
            this.$nextTick(() => {
              this.open(cache)
            })
          }
        })
        this.tmpOpenId = []
      }
    }
  },
  mounted() {
    this.bus.$on('logout', () => this.tree = [])
    this.bus.$on('opened', item => {
      this.currentFileItem = item
    })
    this.bus.$on('delete-api', item => {
      this.deleteApiInfo(item)
    })
    this.bus.$on('refresh-resource', () => {
      this.initData()
    })
    this.bus.$on('update-group', () => {
      this.rebuildTree()
      this.initCreateGroupObj()
      this.changeForceUpdate()
    })
    let element = document.getElementsByClassName('ma-container')[0]
    // 新建分组快捷键
    Key.bind(element, Key.Alt | Key.G, () => this.openCreateGroupModal())
  }
}
</script>

<style>
@import './magic-resource.css';
</style>
