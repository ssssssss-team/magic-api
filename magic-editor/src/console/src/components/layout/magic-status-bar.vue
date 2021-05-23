<template>
  <div>
    {{ message }}
  </div>
</template>

<script>
import bus from '@/scripts/bus.js'

export default {
  name: 'MagicStatusBar',
  data() {
    return {
      message: ''
    }
  },
  mounted() {
    bus.$on('status', (message) => this.message = message)
  }
}
</script>

<style scoped>
div {
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
}
</style>
