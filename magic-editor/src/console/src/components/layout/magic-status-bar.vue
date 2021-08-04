<template>
  <div class="ma-status-container">
    <div>{{ message }}</div>
    <div class="ma-icons">
      <span v-if="config.header.repo !== false" title="Gitee"
            @click="open('https://gitee.com/ssssssss-team/magic-api')">
        <i class="ma-icon ma-icon-gitee"></i>
      </span>
      <span v-if="config.header.repo !== false" title="Github"
            @click="open('https://github.com/ssssssss-team/magic-api')">
        <i class="ma-icon ma-icon-git"></i>
      </span>
      <span v-if="config.header.qqGroup !== false" title="加入QQ群"
            @click="open('https://qm.qq.com/cgi-bin/qm/qr?k=Q6dLmVS8cHwoaaP18A3tteK_o0244e6B&jump_from=webapi')">
        <i class="ma-icon ma-icon-qq"></i>
      </span>
      <span v-if="config.header.document !== false" title="帮助文档"
            @click="open('https://ssssssss.org')">
        <i class="ma-icon ma-icon-help"></i>
      </span>
      <span v-if="user && user.id && user.username" @click="logout" :title="user.username"><i class="ma-icon ma-icon-logout"/></span>
    </div>
  </div>
</template>

<script>
import bus from '@/scripts/bus.js'
import request from '@/api/request.js'
import contants from '@/scripts/contants.js'
import store from '@/scripts/store.js'

export default {
  name: 'MagicStatusBar',
  props: {
    config: {
      type: Object
    }
  },
  data() {
    return {
      user: null,
      message: ''
    }
  },
  mounted() {
    bus.$on('status', (message) => this.message = message)
    bus.$on('login',() => {
      bus.$emit('status', '获取当前登录用户信息')
      request.send('/user').success(user => this.user = user)
    })
  },
  methods: {
    open(url) {
      window.open(url)
    },
    logout(){
      bus.$emit('status', '准备注销登录')
      this.$magicConfirm({
        title: '注销登录',
        content: `是否要注销登录「${this.user.username}」`,
        onOk: () => {
          request.send('/logout').success(() => {
            this.user = null;
            contants.HEADER_MAGIC_TOKEN_VALUE = 'unauthorization';
            store.remove(contants.HEADER_MAGIC_TOKEN);
            bus.$emit('logout')
            bus.$emit('status', '成功注销登录')
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.ma-status-container {
  position: absolute;
  bottom: 0px;
  height: 24px;
  width: 100%;
  border-top: 1px solid var(--footer-border-color);
  padding-left: 20px;
  line-height: 24px;
  background: var(--background);
  flex: none;
  text-align: left;
  color: var(--color);
  display: flex;
}
.ma-status-container > div {
  flex: 1;
}
.ma-status-container .ma-icons{
  flex: none;
  color: var(--header-default-color);
}
.ma-status-container .ma-icons span{
  cursor: pointer;
  padding: 0 4px;
  height: 20px;
  line-height: 20px;
  display: inline-block;
  vertical-align: middle;
  border-radius: 2px;
  text-align: center;
}
.ma-status-container .ma-icons span:hover{
  background: var(--button-hover-background);
}
</style>
