<template>
  <div class="ma-structure-o">
    <label v-if="simple" @click.stop="expand = !expand"><i class="ma-icon" :class="{'ma-icon-collapse': expand, 'ma-icon-expand': !expand}"></i></label>
    <div v-if="simple">
      <span>{</span>
        <span v-for="(value,key,index) in data" :key="'root_' + key">
          <span v-if="index > 0">,</span>
          <span>{{key}}</span>
          <span>:</span>
          <span v-if="Array.isArray(value)" class="array">Array({{value.length}})</span>
          <span v-else-if="value && typeof value === 'object'" class="object">{...}</span>
          <span v-else-if="typeof value === 'string'" class="string">"{{value.replace(/"/g, "\"")}}"</span>
          <span v-else-if="typeof value === 'number'" class="number">{{value}}</span>
          <span v-else-if="typeof value === 'boolean'" class="boolean">{{value}}</span>
          <span v-else-if="value == null" class="boolean">null</span>
          <span v-else>{{value}}</span>
        </span>
      <span>}</span>
    </div>
    <div v-if="expand || simple === false" class="expand-o" :style="{'margin-left': '20px'}">
      <div v-for="(value,key) in data" :key="'root_line_' + key">
        <label v-if="value && typeof value === 'object'" @click.stop="doExpand(key)">
          <i class="ma-icon" :class="{'ma-icon-collapse': expandKeys[key] === true, 'ma-icon-expand': expandKeys[key] !== true}"></i>
        </label>
        <span class="property">{{key}}</span>
        <span class="colon">:</span>
        <span v-if="Array.isArray(value)" class="array">Array({{value.length}})</span>
        <span v-else-if="value && typeof value === 'object'" class="object">{...}</span>
        <span v-else-if="typeof value === 'string'" class="string">"{{value.replace(/"/g, "\"")}}"</span>
        <span v-else-if="typeof value === 'number'" class="number">{{value}}</span>
        <span v-else-if="typeof value === 'boolean'" class="boolean">{{value}}</span>
        <span v-else-if="value == null" class="boolean">null</span>
        <span v-else>{{value}}</span>
        <magic-structure-array v-if="value && Array.isArray(value) && expandKeys[key]" :data="value" :indent="indent + 1" :simple="false"/>
        <magic-structure-object v-else-if="value && typeof value === 'object' && expandKeys[key]" :data="value" :indent="indent + 1" :simple="false"/>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MagicStructureObject',
  props: {
    data: Object,
    indent: {
      type: Number,
      default: 0
    },
    simple: {
      type: Boolean,
      default: true
    }
  },
  components: { MagicStructureArray: ()=> import('./magic-structure-array.vue') },
  data(){
    return {
      expand: false,
      expandKeys: {}
    }
  },
  methods:{
    doExpand(key) {
      this.expandKeys[key] = this.expandKeys[key] === undefined ? true : !this.expandKeys[key];
      this.$forceUpdate()
    }
  }
}
</script>
<style scoped>
.ma-structure-o > div {
  display: inline-block;
}
.ma-structure-o .expand-o{
  display: block;
  font-style: normal;
}
.expand-o label{
  margin-left: -6px;
}
.ma-structure-o > div > span > span:last-child{
  padding-left: 5px;
}
.ma-structure-o > div > span:not(:first-child):not(:last-child):not(:nth-child(2)) > span:first-child{
  padding-right: 5px;
}
.colon{
  margin-right: 5px;
}
</style>
