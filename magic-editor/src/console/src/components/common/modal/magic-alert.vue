<template>
  <magic-dialog v-if="value" ref="dialog" v-model="value" :title="title" @onClose="closeHandle">
    <template #content>
      {{ content }}
    </template>
    <template #buttons>
      <button class="ma-button active" @click="okHandle">{{ ok }}</button>
    </template>
  </magic-dialog>
</template>
<script>
import MagicDialog from './magic-dialog.vue'

export default {
  name: 'MagicAlert',
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
      default: 'OK'
    },
    onOk: {
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
      this.$refs.dialog.close()
    },
    okHandle() {
      this.close()
      if (typeof this.onOk === 'function') {
        this.onOk()
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
