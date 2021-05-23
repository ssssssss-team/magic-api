<template>
  <magic-dialog v-show="true" :moveable="false" :shade="true" :showClose="false" title="登录">
    <template #content>
      <label>用户名：</label>
      <magic-input :onEnter="doLogin" :value.sync="username"/>
      <div style="height: 2px"/>
      <label>密码：</label>
      <magic-input :onEnter="doLogin" :value.sync="password" type="password"/>
    </template>
    <template #buttons>
      <button class="ma-button active" @click="doLogin">登录</button>
    </template>
  </magic-dialog>
</template>

<script>
import MagicInput from '@/components/common/magic-input'
import MagicDialog from '@/components/common/modal/magic-dialog'
import request from '@/api/request.js'
import contants from '@/scripts/contants.js'
import store from '@/scripts/store.js'

export default {
  name: 'MagicLogin',
  props: {
    onLogin: Function,
  },
  components: {
    MagicInput,
    MagicDialog,
  },
  data() {
    return {
      username: '',
      password: '',
    }
  },
  methods: {
    doLogin() {
      request.send('/login', {
        username: this.username,
        password: this.password
      }).success((res, response) => {
        if (res) {
          contants.HEADER_MAGIC_TOKEN_VALUE = response.headers[contants.HEADER_MAGIC_TOKEN];
          store.set(contants.HEADER_MAGIC_TOKEN, contants.HEADER_MAGIC_TOKEN_VALUE);
          this.onLogin();
        } else {
          this.$magicAlert({
            title: '登录',
            content: '登录失败,用户名或密码不正确'
          })
        }
      })
    },
  },
}
</script>
<style scoped>
label {
  width: 80px;
  text-align: right;
  display: inline-block;
}
</style>