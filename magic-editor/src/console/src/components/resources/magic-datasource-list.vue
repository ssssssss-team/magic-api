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
      <ul>
        <template v-for="(item,index) in datasources" >
        <li v-if="item._searchShow === true || item._searchShow === undefined" :key="index" @click="showDetail(item.id)" @contextmenu.prevent="e => datasourceContextMenu(e, item)">
          <i class="ma-icon ma-icon-datasource"/>
          <label>{{item.name || '主数据源'}}</label>
          <span>({{item.key || 'default'}})</span>
        </li>
        </template>
      </ul>
    </div>
    <magic-dialog width="1000px" height="400px" v-model="showDialog" :title="dsId ? '修改数据源:' + dsName : '创建数据源'" align="right" @onClose="toogleDialog(false)">
      <template #content>
        <div class="ds-form">
          <label>数据源名称</label>
          <magic-input :value.sync="dsName"/>
          <label>key</label>
          <magic-input :value.sync="dsKey"/>
        </div>
        <div ref="editor" class="ma-editor" style="width: 100%;height:300px"></div>
      </template>
      <template #buttons>
        <button class="ma-button active" @click="doSave">{{ dsId ? '修改' : '创建' }}</button>
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

export default {
  name: 'MagicDatasourceList',
  components: {
    MagicDialog,
    MagicInput
  },
  data() {
    return {
      bus: bus,
      datasources: [],
      showDialog:false,
      dsName: "",
      dsKey : "",
      dsId : "",
      datasourceObj: {
        url: "",
        username: "",
        password: "",
        maxRows: "-1"
      },
      editor: null
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
      request.send('datasource/list').success(data => {
        this.datasources = data;
        JavaClass.setExtensionAttribute('org.ssssssss.magicapi.modules.SQLModule',this.datasources.filter(it => it.key).map(it => {
          return {
            name : it.key,
            type: 'org.ssssssss.magicapi.modules.SQLModule',
            comment: it.name
          }
        }))
      })
    },
    showDetail(id){
      if(!id){
        this.$magicAlert({
          content : '该数据源不能被修改'
        })
      }else{
        request.send('datasource/detail',{id : id}).success(res => {
          this.dsId = id;
          this.dsName = res.name;
          this.dsKey = res.key;
          delete res.id;
          delete res.name;
          delete res.key;
          this.datasourceObj = res;
          this.toogleDialog(true)
        });
      }
    },
    doTest(){
      let value = this.editor.getValue();
      let json = {};
      try{
        json = JSON.parse(value)
      }catch(e){
        this.$magicAlert({
          content : 'JSON格式有误，请检查'
        })
        return;
      }
      bus.$emit('status', `测试数据源连接...`)
      request.send('datasource/test',value,{
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
      if(!this.dsName){
        this.$magicAlert({
          content : '数据源名称不能为空'
        })
      }else if(!this.dsKey){
        this.$magicAlert({
          content : '数据源key不能为空'
        })
      }else{
        let value = this.editor.getValue();
        let json = {};
        try{
          json = JSON.parse(value)
        }catch(e){
          this.$magicAlert({
            content : 'JSON格式有误，请检查'
          })
          return;
        }
        let saveObj = {
          ...json,
          id: this.dsId,
          key: this.dsKey,
          name: this.dsName
        }
        bus.$emit('status', `保存数据源「${this.dsName}」...`)
        request.send('datasource/save',JSON.stringify(saveObj),{
          method: 'post',
          headers: {
            'Content-Type': 'application/json'
          },
          transformRequest: []
        }).success(dsId => {
          bus.$emit('status', `数据源「${this.dsName}」保存成功`)
          this.showDialog = false;
          this.datasourceObj = {
            url: "",
            username: "",
            password: "",
            maxRows: "-1"
          };
          this.dsId = '';
          this.dsName = '';
          this.dsKey = '';
          this.initData();

        })
      }
    },
    toogleDialog(show,clear){
      this.showDialog = show;
      if(show){
        if(clear){
          this.datasourceObj = {
            url: "",
            username: "",
            password: "",
            maxRows: "-1"
          };
          this.dsId = '';
          this.dsName = '';
          this.dsKey = '';
        }
        bus.$emit('status', `准备编辑数据源`)
        if(!this.editor){
          this.editor = monaco.editor.create(this.$refs.editor, {
            minimap: {
              enabled: false
            },
            language: 'json',
            fixedOverflowWidgets: true,
            folding: true,
            wordWrap: 'on',
            lineDecorationsWidth: 20,
            theme: store.get('skin') || 'default',
            value: formatJson(this.datasourceObj) || '{\r\n\t\r\n}'
          })
        }else{
          bus.$emit('status', `编辑数据源「${this.dsName}」`)
          this.editor.setValue(formatJson(this.datasourceObj))
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
}
.ds-form label{
  margin-right: 5px;
}
.ds-form > div{
  width: 335px;
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
</style>
