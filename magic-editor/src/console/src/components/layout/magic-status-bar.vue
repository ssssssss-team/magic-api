<template>
  <div class="ma-status-container">
    <div>{{ message }}</div>
    <div class="ma-user-info" v-if="user && user.id && user.username" @click="logout"><i class="ma-icon ma-icon-logout"/>{{ user.username }}</div>
  </div>
</template>

<script>
import bus from '@/scripts/bus.js'
import request from '@/api/request.js'
import contants from '@/scripts/contants.js'
import store from '@/scripts/store.js'

export default {
  name: 'MagicStatusBar',
  data() {
    return {
      user: null,
      message: ''
    }
  },
  mounted() {
    bus.$on('status', (message) => this.message = message)
    bus.$on('login',() => {
      request.send('/user').success(user => this.user = user)
    })
  },
  methods: {
    logout(){
      this.$magicConfirm({
        title: '注销登录',
        content: `是否要注销登录`,
        onOk: () => {
          request.send('/logout').success(() => {
            this.user = null;
            contants.HEADER_MAGIC_TOKEN_VALUE = 'unauthorization';
            store.remove(contants.HEADER_MAGIC_TOKEN);
            bus.$emit('logout')
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
.ma-status-container .ma-user-info{
  flex: none;
  padding: 0 10px;
  cursor: pointer;
}
.ma-status-container .ma-user-info i{
  color: var(--icon-color);
  padding: 0 2px;
  vertical-align: bottom;
}
.ma-status-container .ma-user-info:hover{
  background: var(--hover-background);
}
</style>
