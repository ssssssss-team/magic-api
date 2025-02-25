<template>
  <magic-dialog ref="dialog" v-model="value" :title="title" align="right" @onClose="closeHandle">
    <template #content>
      <div v-html="content"/>
    </template>
    <template #buttons>
      <button class="ma-button active" @click="okHandle">{{ ok }}</button>
      <button class="ma-button" @click="cancelHandle">{{ cancel }}</button>
    </template>
  </magic-dialog>
</template>
<script>
import MagicDialog from './magic-dialog.vue'

export default {
  name: 'MagicConfirm',
  components: {
    MagicDialog
  },
  props: {
    title: {
      type: String,
      default: '提示'
    },
    content: String,
    ok: {
      type: String,
      default: '是'
    },
    cancel: {
      type: String,
      default: '否'
    },
    onOk: {
      type: Function,
      require: false
    },
    onCancel: {
      type: Function,
      require: false
    },
    onClose: {
      type: Function,
      require: false
    },
    value: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    close() {
      this.$refs.dialog.hide()
    },
    okHandle() {
      this.close()
      if (typeof this.onOk === 'function') {
        this.onOk()
      }
      this.closeHandle()
    },
    cancelHandle() {
      this.close()
      if (typeof this.onCancel === 'function') {
        this.onCancel()
      }
    },
    closeHandle() {
      if (typeof this.onClose === 'function') {
        this.onClose()
      }
    }
  }
}
</script>
