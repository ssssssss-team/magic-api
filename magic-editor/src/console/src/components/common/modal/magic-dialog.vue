<template>
  <div v-show="value" :class="[{ moveable,shade },'ma-dialog-wrapper ' + className]">
    <div :style="{ position, top, left,width,height,'max-width': maxWidth }" class="ma-dialog">
      <div ref="title" class="ma-dialog-header not-select">
        {{ title }}
        <span v-if="showClose" @click="close"><i class="ma-icon ma-icon-close"/></span>
      </div>
      <div :style="{padding,'max-height': maxHeight,height: contentHeight, overflow: 'auto'}" class="ma-dialog-content">
        <template v-if="content">
          {{ content }}
        </template>
        <slot v-else name="content"></slot>
      </div>
      <div :class="{ 'button-align-right': align == 'right' }" class="ma-dialog-buttons not-select">
        <slot name="buttons"></slot>
      </div>
    </div>
  </div>
</template>
<script>
export default {
  name: 'MagicDialog',
  props: {
    title: String,
    className: {
      type: String,
      default: ''
    },
    showClose: {
      type: Boolean,
      default: true,
    },
    align: String,
    moveable: {
      type: Boolean,
      default: true,
    },
    content: {
      type: String,
      require: false,
    },
    onClose: {
      type: Function,
      require: false,
    },
    value: {
      type: Boolean,
      default: false,
    },
    width: {
      type: String,
      default: 'auto'
    },
    height: {
      type: String,
      default: 'auto'
    },
    maxWidth: {
      type: String
    },
    maxHeight: {
      type: String
    },
    contentHeight: {
      type: String
    },
    padding: {
      type: String,
      default: '5px 10px'
    },
    shade: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      position: 'relative',
      top: 'auto',
      left: 'auto',
    }
  },
  mounted() {
    this.$nextTick(() => {
      if (this.moveable) {
        let moving = false
        let rect
        let start
        let maxRect = this.getRootEl().getBoundingClientRect()
        this.$refs.title.addEventListener('mousedown', (e) => {
          start = e
          moving = true
          rect = this.$refs.title.parentNode.getBoundingClientRect()
          this.top = rect.top + 'px'
          this.left = rect.left + 'px'
          this.position = 'fixed'
        })
        this.getRootEl().addEventListener('mousemove', (e) => {
          if (moving) {
            let y = Math.min(Math.max(rect.top + (e.pageY - start.pageY), maxRect.y), maxRect.y + maxRect.height - rect.height)
            let x = Math.min(Math.max(rect.left + (e.pageX - start.pageX), maxRect.x), maxRect.x + maxRect.width - rect.width)
            this.top = y + 'px'
            this.left = x + 'px'
          }
        })
        this.$refs.title.addEventListener('mouseup', (e) => {
          moving = false
        })
      }
    })
  },
  methods: {
    show() {
      this.$emit('change', true)
      this.$emit('input', true)
    },
    hide() {
      this.$emit('change', false)
      this.$emit('input', false)
    },
    close() {
      this.$emit('onClose')
      if (typeof this.onClose === 'function') {
        this.onClose()
      }
      this.hide()
    },
    getRootEl() {
      return document.getElementsByClassName('ma-container')[0]
    },
    appendDom() {
      this.$nextTick(() => {
        const body = this.getRootEl()
        if (body.append) {
          body.append(this.$el)
        } else {
          body.appendChild(this.$el)
        }
      })
    },
  },
}
</script>
<style>
.ma-dialog-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 999999;
  text-align: center;
}

.ma-dialog-wrapper.shade {
  background: rgba(0, 0, 0, 0.6);
}

.ma-dialog-wrapper.moveable .ma-dialog .ma-dialog-header {
  cursor: move;
}

.ma-dialog-wrapper::before {
  content: '';
  display: inline-block;
  height: 100%;
  vertical-align: middle;
  margin-right: -0.25em;
}

.ma-dialog-wrapper .ma-dialog {
  background: var(--background);
  border: 1px solid var(--dialog-border-color);
  display: inline-block;
  vertical-align: middle;
  position: relative;
  min-width: 250px;
  box-shadow: 0px 0px 8px var(--dialog-shadow-color);
  max-width: 800px;
  color: var(--color);
}

.ma-dialog-wrapper .ma-dialog .ma-dialog-header {
  height: 30px;
  line-height: 30px;
  padding-left: 30px;
  padding-right: 75px;
  background-size: 22px 24px;
  background-position: 5px 4px;
  background-repeat: no-repeat;
  text-align: left;
}

.ma-dialog-wrapper .ma-dialog .ma-dialog-header span {
  display: inline-block;
  width: 40px;
  position: absolute;
  right: 0px;
  text-align: center;
  cursor: pointer;
  font-size: 12px;
  height: 30px;
}

.ma-dialog-wrapper .ma-dialog .ma-dialog-header span:hover:not(.disabled) {
  background: #e81123;
  color: var(--select-icon-background);
}

.ma-dialog-wrapper .ma-dialog .ma-dialog-content {
  text-align: left;
  word-break: break-word;
}

.ma-dialog-wrapper .ma-dialog .ma-dialog-buttons {
  padding: 5px 0;
}

.ma-dialog-wrapper .ma-dialog .ma-dialog-buttons.button-align-right {
  text-align: right;
  margin-right: 10px;
}

.ma-dialog-wrapper .ma-dialog .ma-dialog-buttons button:not(:last-child) {
  margin-right: 10px;
}


</style>
