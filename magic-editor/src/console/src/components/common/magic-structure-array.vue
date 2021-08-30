<template>
  <div class="ma-structure-a">
    <label v-if="simple" @click.stop="expand = !expand"><i class="ma-icon" :class="{'ma-icon-collapse': expand, 'ma-icon-expand': !expand}"></i></label>
    <div v-if="simple">
      <span class="array">Array({{data.length}})</span>
    </div>
    <div v-if="expand || simple === false" class="expand-a" :style="{'margin-left': '20px'}">
      <template v-if="chunk">
        <div v-for="(range,key) in chunkRange" :key="'root_array_' + key">
          <label @click.stop="doExpand(key)">
            <i class="ma-icon" :class="{'ma-icon-collapse': expandKeys[key] === true, 'ma-icon-expand': expandKeys[key] !== true}"></i>
          </label>
          <span class="array">[{{range[0]}} ... {{range[1]}}]</span>
          <magic-structure-array v-if="expandKeys[key]" :data="data.slice(range[0],range[1])" :fromIndex="range[0]" :indent="indent + 1" :simple="false"/>
        </div>
      </template>
      <template v-else>
        <div v-for="(value,key) in data" :key="'root_line_' + key">
          <label v-if="value && typeof value === 'object'" @click.stop="doExpand(key)">
            <i class="ma-icon" :class="{'ma-icon-collapse': expandKeys[key] === true, 'ma-icon-expand': expandKeys[key] !== true}"></i>
          </label>
          <span class="property">{{fromIndex + key}}</span>
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
      </template>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MagicStructureArray',
  props: {
    data: Array,
    indent: {
      type: Number,
      default: 0
    },
    simple: {
      type: Boolean,
      default: true
    },
    fromIndex: {
      type: Number,
      default: 0
    }
  },
  components: { MagicStructureObject: ()=> import('./magic-structure-object.vue') },
  data(){
    let len = this.data.length;
    let chunkSize = 100;
    let chunk = len > chunkSize;
    let ranges = [];
    for(let index =0; index < len; index+=chunkSize){
      ranges.push([index, Math.min(index + chunkSize, len)])
    }
    return {
      expand: false,
      expandKeys: {},
      chunk,
      chunkRange: ranges
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
.ma-structure-a > div {
  display: inline-block;
}
.ma-structure-a .expand-a{
  display: block;
  font-style: normal;
}
.expand-a label{
  margin-left: -6px;
}
.ma-structure-a > div > span > span:last-child{
  padding-left: 5px;
}
.ma-structure-a > div > span:not(:first-child):not(:last-child):not(:nth-child(2)) > span:first-child{
  padding-right: 5px;
}
.colon{
  margin-right: 5px;
}
</style>
