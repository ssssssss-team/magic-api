<template>
  <div :class="{ inputable, border}" class="ma-select not-select" @click.stop="showList" ref="container">
    <span v-if="!inputable">{{ showText }}</span>
    <input v-if="inputable" ref="input" :value="showText" autocomplete="off" type="text" @input="(e) => triggerSelect(e.target.value)"/>
    <ul v-show="visible" :style="{ width, marginTop}" ref="selectList">
      <li v-for="item in options" :key="'ma_select_' + item.value" @click.stop="triggerSelect(item.value)">
        {{ item.text }}
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'MagicSelect',
  props: {
    value: String,
    defaultValue: String,
    options: Array,
    border: {
      type: Boolean,
      default: true
    },
    inputable: {
      type: Boolean,
      default: false,
    },
    select: Function
  },
  data() {
    return {
      marginTop: '-2px',
      width: 'auto',
      visible: false,
    }
  },
  mounted() {
    this.$root.$el.addEventListener('click', () => (this.visible = false));
  },
  methods: {
    showList(){
      this.visible = true;
      this.$nextTick(()=>{
        this.width = this.$refs.container.clientWidth + 'px';
        let height = this.$refs.selectList.offsetHeight;
        let top = this.$refs.selectList.offsetTop;
        if (top + height + 20 > this.$root.$el.offsetTop + this.$root.$el.offsetHeight) {
          this.marginTop = -(height + this.$refs.container.offsetHeight) + 'px'
        }
      })
    },
    triggerSelect(value) {
      this.$emit('update:value', value);
      this.visible = false;
      if (this.select) {
        this.select(value);
      }
    },
  },
  computed: {
    showText: {
      cache: false,
      get() {
        let options = this.options || []
        let showText = this.value || this.defaultValue;
        options.forEach((item) => {
          if (item.value === this.value || item.value === showText) {
            showText = item.text
          }
        })
        return showText
      },
    },
  },
}
</script>
<style scoped>
.ma-select {
  position: relative;
  display: inline-block;
  background: var(--select-background);
  height: 22px;
  line-height: 22px;
  width: 80px;
  font-size: 12px;
}

.ma-select.border {
  border: 1px solid var(--input-border-color);
}

.ma-select.inputable {
  background: var(--select-inputable-background);
  border-color: var(--select-inputable-border);
}

.ma-select input {
  background: none;
  border: none;
  height: 22px;
  line-height: 22px;
  border-radius: 0;
  outline: 0;
  padding-left: 5px;
  width: 100%;
  color: var(--color)
}

.ma-select span {
  height: 22px;
  line-height: 22px;
  border-radius: 0;
  outline: 0;
  padding-left: 5px;
}

.ma-select:hover:not(.inputable) {
  background: var(--select-hover-background);
}

.ma-select::after {
  content: '\efa2';
  font-family: 'magic-iconfont';
  position: absolute;
  right: 5px;
  top: 0px;
}

ul {
  display: block;
  position: fixed;
  z-index: 10;
  background: var(--select-option-background);
  border: 1px solid var(--select-border-color);
  margin-top: -2px;
  padding: 0px;
  box-sizing: content-box;
  margin-left: -1px;
}

ul li {
  padding: 0 5px;
  text-align: left;
  width: 100% !important;
}

ul li:hover {
  background: var(--select-option-hover-background);
  color: var(--select-option-hover-color);
}
</style>