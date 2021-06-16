<template>
  <div v-if="item.level > 0">
    <span v-for="index in item.level - 1" :key="index">
      <img :src="imgLine" v-if="indentLevel[index] != 1"/>
      <img :src="imgIndent" style="width: 16px;height: 16px;" v-else/>
    </span>
    <img :src="index + 1 == dataLength ? imgEnd : img"/>
  </div>
</template>

<script>

  export default {
    name: 'MagicJsonTreeFormat',
    props: {
      item: {
        type: Object,
        required: true
      },
      index: Number,
      dataLength: Number,
      indentLevel: Array
    },
    data() {
      return {
        imgEnd: require('../../assets/images/elbow-end.gif'),
        imgLine: require('../../assets/images/elbow-line.gif'),
        img: require('../../assets/images/elbow.gif'),
        imgIndent: require('../../assets/images/s.gif')
      }
    },
  }
</script>
<style>
</style>
