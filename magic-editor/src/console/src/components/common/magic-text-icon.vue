<template>
  <svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="36" height="12" font-size="10" font-weight="bold">
    <text x="16" y="8" :fill="color" style="dominant-baseline: middle;text-anchor:middle;" >{{ text }}</text>
  </svg>
</template>
<script>
export default {
  name: 'MagicTextIcon',
  props: {
    value: String
  },
  data(){
    return {
      colors: {
        GET: '#249C47',
        POST: '#FFB400',
        DELETE: ['DEL', '#EB2013'],
        PUT: '#097BED',
        'function': ['Fn', '#9012FE']
      }
    }
  },
  computed: {
    text: function() {
      let color = this.colors[this.value]
      let text = this.value
      if(color !== undefined && Array.isArray(color)) {
        text = color[0]
      }
      return text
    },
    color: function() {
      let color = this.colors[this.value]
      if(color !== undefined && Array.isArray(color)) {
        return color[1]
      }
      return color || 'var(--icon-color)'
    }
  }
}
</script>
<style scoped>

</style>