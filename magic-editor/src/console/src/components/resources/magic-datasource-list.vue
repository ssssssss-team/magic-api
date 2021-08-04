<template>
  <div style="width: 100%;height: 100%;background: var(--toolbox-background)">
    <div class="ma-tree-wrapper">
      <div class="ma-tree-toolbar">
        <div class="ma-tree-toolbar-search">
          <i class="ma-icon ma-icon-search"></i>
          <input placeholder="输入关键字搜索" @input="e => doSearch(e.target.value)"/>
        </div>
        <div>
          <div class="ma-tree-toolbar-btn" title="新建数据源" @click="toogleDialog(true,true)">
            <i class="ma-icon ma-icon-group-add"></i>
          </div>
          <div class="ma-tree-toolbar-btn" title="刷新数据源" @click="initData()">
            <i class="ma-icon ma-icon-refresh"></i>
          </div>
        </div>
      </div>
      <ul v-show="!showLoading">
        <template v-for="(item,index) in datasources" >
        <li v-if="item._searchShow === true || item._searchShow === undefined" :key="index" @click="showDetail(item)" @contextmenu.prevent="e => datasourceContextMenu(e, item)">
          <i class="ma-icon ma-icon-datasource"/>
          <label>{{item.name || '主数据源'}}</label>
          <span>({{item.key || 'default'}})</span>
        </li>
        </template>
      </ul>
      <div class="loading" v-show="showLoading">
        <div class="icon">
          <i class="ma-icon ma-icon-refresh "></i>
        </div>
        加载中...
      </div>
      <div class="no-data" v-show="!showLoading && (!datasources || datasources.length === 0)">无数据</div>
    </div>
    <magic-dialog width="400px" height="450px" v-model="showDialog" :title="datasourceObj.id ? '修改数据源:' + datasourceObj.name : '创建数据源'" align="right" @onClose="toogleDialog(false)">
      <template #content>
        <div class="ds-form">
          <label>名称</label>
          <magic-input :value.sync="datasourceObj.name" placeholder="数据源名称，仅做展示使用"/>
        </div>
        <div class="ds-form">
          <label>Key</label>
          <magic-input :value.sync="datasourceObj.key" placeholder="数据库key，后续代码中使用"/>
        </div>
        <div class="ds-form">
          <label>URL</label>
          <magic-input :value.sync="datasourceObj.url" placeholder="请输入jdbcurl，如：jdbc:mysql://localhost"/>
        </div>
        <div class="ds-form">
          <label>用户名</label>
          <magic-input :value.sync="datasourceObj.username" placeholder="请输入数据库用户名"/>
        </div>
        <div class="ds-form">
          <label>密码</label>
          <magic-input :value.sync="datasourceObj.password" type="password" placeholder="请输入数据库密码"/>
        </div>
        <div class="ds-form">
          <label>驱动类</label>
          <magic-select :inputable="true" :border="true" :value.sync="datasourceObj.driverClassName" :options="drivers" :placeholder="'驱动类，可选，内部自动识别，也可以手动输入指定'"/>
        </div>
        <div class="ds-form">
          <label>类型</label>
          <magic-select :inputable="true" :border="true" :value.sync="datasourceObj.type" :options="datasourceTypes" :placeholder="'数据源类型，可选，也可以手动输入指定'"/>
        </div>
        <div class="ds-form">
          <label>maxRows</label>
          <magic-input :value.sync="datasourceObj.maxRows" placeholder="最多返回条数，-1未不限制"/>
        </div>
        <div class="ds-form">
          <label>其它配置</label>
          <div ref="editor" class="ma-editor" style="width: 100%;height:150px"></div>
        </div>
      </template>
      <template #buttons>
        <button class="ma-button active" @click="doSave">{{ datasourceObj.id ? '修改' : '创建' }}</button>
        <button class="ma-button" @click="doTest">测试连接</button>
        <button class="ma-button" @click="toogleDialog(false)">取消</button>
      </template>
    </magic-dialog>
  </div>
</template>

<script>
import bus from '@/scripts/bus.js'
import request from '@/api/request.js'
import MagicDialog from '@/components/common/modal/magic-dialog.vue'
import MagicInput from '@/components/common/magic-input.vue'
import {formatJson, isVisible, replaceURL} from '@/scripts/utils.js'
import JavaClass from '@/scripts/editor/java-class.js'
import * as monaco from 'monaco-editor/esm/vs/editor/editor.api'
import store from "@/scripts/store";
import MagicSelect from "@/components/common/magic-select";

export default {
  name: 'MagicDatasourceList',
  components: {
    MagicSelect,
    MagicDialog,
    MagicInput
  },
  data() {
    return {
      bus: bus,
      datasources: [],
      showDialog:false,
      datasourceObj: {
        id: "",
        name: "",
        key: "",
        url: "",
        username: "",
        password: "",
        driverClassName: "",
        maxRows: "-1",
        type: ""
      },
      drivers: [
          'com.mysql.jdbc.Driver',
          'com.mysql.cj.jdbc.Driver',
          'oracle.jdbc.driver.OracleDriver',
          'org.postgresql.Driver',
          'com.microsoft.sqlserver.jdbc.SQLServerDriver',
          'com.ibm.db2.jcc.DB2Driver',
      ].map(it => { return {text: it, value: it} }),
      datasourceTypes: [
          'com.zaxxer.hikari.HikariDataSource',
          'com.alibaba.druid.pool.DruidDataSource',
          'org.apache.tomcat.jdbc.pool.DataSource',
          'org.apache.commons.dbcp2.BasicDataSource',
      ].map(it => { return {text: it, value: it} }),
      editor: null,
      // 是否展示loading
      showLoading: true
    }
  },
  methods: {
    layout() {
      this.$nextTick(() => {
        if (this.editor && isVisible(this.$refs.editor)) {
          this.editor.layout()
        }
      })
    },
    doSearch(keyword) {
      keyword = keyword.toLowerCase()
      this.datasources.forEach(it => {
        it._searchShow = keyword ? (it.name&&it.name.toLowerCase().indexOf(keyword) > -1) || (it.key && it.key.toLowerCase().indexOf(keyword) > -1) : true;
      })
      this.$forceUpdate();
    },
    datasourceContextMenu(event,item){
      if(item.id){
        this.$magicContextmenu({
          menus: [{
            label : '删除数据源',
            onClick: ()=>this.deleteDataSource(item)
          }],
          event,
          zIndex: 9999
        });
      }
      return false;
    },
    // 初始化数据
    initData() {
      this.showLoading = true
      this.datasources = []
      bus.$emit('status', '正在初始化数据源列表')
      return new Promise((resolve) => {
        request.send('datasource/list').success(data => {
          this.datasources = data;
          JavaClass.setExtensionAttribute('org.ssssssss.magicapi.modules.SQLModule', this.datasources.filter(it => it.key).map(it => {
            return {
              name: it.key,
              type: 'org.ssssssss.magicapi.modules.SQLModule',
              comment: it.name
            }
          }))
          setTimeout(() => {
            this.showLoading = false
          }, 500)
          bus.$emit('status', '数据源初始化完毕')
          resolve()
        })
      })
    },
    showDetail(item){
      if(!item.id){
        this.$magicAlert({
          content : '该数据源不能被修改'
        })
      }else{
        bus.$emit('status', `加载数据源「${item.name}」详情`)
        request.send('datasource/detail',{id : item.id}).success(res => {
          this.datasourceObj = res;
          this.toogleDialog(true)
          bus.$emit('status', `数据源「${item.name}」详情加载完毕`)
        });
      }
    },
    getDataSourceObj(){
      let temp = {
        id: this.datasourceObj.id,
        name: this.datasourceObj.name,
        key: this.datasourceObj.key,
        maxRows: this.datasourceObj.maxRows,
        type: this.datasourceObj.type,
        driverClassName: this.datasourceObj.driverClassName,
        username: this.datasourceObj.username,
        password: this.datasourceObj.password,
        url: this.datasourceObj.url,
      }
      let value = this.editor.getValue();
      let json = {};
      try{
        json = JSON.parse(value)
      }catch(e){
      }
      for(let key in json){
        if(!temp[key]){
          temp[key] = json[key]
        }
      }
      return temp;
    },
    doTest(){
      bus.$emit('status', `测试数据源连接...`)
      request.send('datasource/test',JSON.stringify(this.getDataSourceObj()),{
        method: 'post',
        headers: {
          'Content-Type': 'application/json'
        },
        transformRequest: []
      }).success(msg => {
        if(!msg){
          this.$magicAlert({
            content : '连接成功'
          })
          bus.$emit('status', `数据源连接成功`)
        }else{
          bus.$emit('status', `数据源连接失败：${msg}`)
          this.$magicAlert({
            title: '测试连接失败',
            content : msg
          })
        }
      })
    },
    doSave(){
      if(!this.datasourceObj.username){
        this.$magicAlert({
          content : '数据源名称不能为空'
        })
      }else if(!this.datasourceObj.key){
        this.$magicAlert({
          content : '数据源key不能为空'
        })
      }else{
        bus.$emit('status', `保存数据源「${this.datasourceObj.name}」...`)
        request.send('datasource/save',JSON.stringify(this.getDataSourceObj()),{
          method: 'post',
          headers: {
            'Content-Type': 'application/json'
          },
          transformRequest: []
        }).success(dsId => {
          bus.$emit('status', `数据源「${this.datasourceObj.name}」保存成功`)
          this.showDialog = false;
          this.initDataSourceObj()
          this.initData();

        })
      }
    },
    initDataSourceObj(){
      this.datasourceObj = {
        id: "",
        name: "",
        key: "",
        url: "",
        username: "",
        password: "",
        maxRows: "-1",
        type: ""
      }
    },
    toogleDialog(show,clear){
      this.showDialog = show;
      if(show){
        if(clear){
          this.initDataSourceObj()
        }
        bus.$emit('status', `准备编辑数据源`)
        let temp = {...this.datasourceObj}
        delete temp.id
        delete temp.name
        delete temp.key
        delete temp.maxRows
        delete temp.type
        delete temp.driverClassName
        delete temp.username
        delete temp.password
        delete temp.url
        if(!this.editor){
          this.editor = monaco.editor.create(this.$refs.editor, {
            minimap: {
              enabled: false
            },
            language: 'json',
            fixedOverflowWidgets: true,
            folding: true,
            wordWrap: 'on',
            theme: store.get('skin') || 'default',
            value: formatJson(temp) || '{\r\n\t\r\n}'
          })
        }else{
          bus.$emit('status', `编辑数据源「${this.datasourceObj.name}」`)
          this.editor.setValue(formatJson(temp))
        }
        this.layout();
      }
    },
    // 删除接口
    deleteDataSource(item) {
      bus.$emit('status', `准备删除函数「${item.name}(${item.key})」`)
      this.$magicConfirm({
        title: '删除数据源',
        content: `是否要删除数据源「${item.name}(${item.key})」`,
        onOk: () => {
          request.send('datasource/delete', {id: item.id}).success(data => {
            if (data) {
              bus.$emit('status', `数据源「${item.name}(${item.key})」已删除`)
              this.initData();
            } else {
              this.$magicAlert({content: '删除失败'})
            }
          })
        }
      })
    }
  },
  mounted() {
    this.bus.$on('logout', () => this.datasources = []);
    this.bus.$on('refresh-resource',() => {
      this.initData()
    })
  }
}
</script>

<style>
@import './magic-resource.css';
</style>
<style scoped>

ul li {
  line-height: 20px;
  padding-left: 10px;
}
ul li:hover{
  background: var(--toolbox-list-hover-background);
}
.ds-form{
  margin-bottom: 5px;
  display: flex;
}
.ds-form label{
  margin-right: 5px;
  display: inline-block;
  width: 60px;
  text-align: right;
  height: 22px;
  line-height: 22px;
}
.ds-form > div{
  flex: 1;
}
.ds-form label:nth-of-type(2){
  margin: 0 5px;
}
.ma-editor span{
  color: unset;
}
.ma-tree-wrapper{
  width: 100%;
  height: 100%;
}
.ma-tree-wrapper .loading i {
  color: var(--color);
  font-size: 20px;
}
.ma-tree-wrapper .loading .icon {
  width: 20px;
  margin: 0 auto;
  animation: rotate 1s linear infinite;
}
.ma-tree-wrapper .loading {
  color: var(--color);
  position: absolute;
  text-align: center;
  width: 100%;
  top: 50%;
  margin-top: -20px;
}
.ma-tree-wrapper .no-data {
  color: var(--color);
  position: absolute;
  top: 50%;
  left: 50%;
  margin-left: -20px;
}
</style>
