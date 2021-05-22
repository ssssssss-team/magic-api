<template>
  <div class="ma-checkbox">
    <input :id="cboId" ref="checkbox" type="checkbox" @change="onChange" :checked="value"/>
    <label :for="cboId"/>
  </div>
</template>

<script>
export default {
  name: 'MagicCheckbox',
  props: {
    value: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      cboId: new Date().getTime() + '' + Math.floor(Math.random() * 1000)
    }
  },
  mounted() {
  },
  methods: {
    onChange() {
      this.$emit('update:value', this.$refs.checkbox.checked);
    }
  },
}
</script>
<style scoped>
.ma-checkbox {
  width: 100%;
  height: 100%;
  text-align: center;
}

.ma-checkbox input {
  display: none;
}

.ma-checkbox input + label {
  position: relative;
  color: #c9c9c9;
  font-size: 12px;
  height: 24px;
  line-height: 24px;
  user-select: none;
  display: inline;
}

.ma-checkbox input + label::after {
  display: inline-block;
  background-color: var(--checkbox-background);
  border: 1px solid var(--checkbox-border);
  content: '';
  width: 16px;
  height: 16px;
  line-height: 16px;
  position: absolute;
  top: 2px;
  left: -10px;
  text-align: center;
  font-size: 12px;
  color: var(--checkbox-text-color);
}

.ma-checkbox input:checked + label::after {
  content: "\2714";
  background-color: var(--checkbox-selected-background);
  border-color: var(--checkbox-selected-border);
}
</style>