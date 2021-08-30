<template>
  <div class="ma-structure-container">
    <span :class="type.substring(10).toLowerCase()" v-if="displayText">{{ data }}</span>
    <span class="boolean" v-else-if="!type">null</span>
    <magic-structure-array v-else-if="Array.isArray(jsonData)" :data="jsonData"/>
    <magic-structure-object v-else :data="jsonData"/>
  </div>
</template>

<script>
import MagicStructureArray from './magic-structure-array'
import MagicStructureObject from './magic-structure-object'
export default {
  name: 'MagicStructure',
  props: {
    data: String,
    type: String
  },
  data() {
    let displayText = (this.type||'').startsWith('java.lang');
    return {
      displayText,
      jsonData: displayText ? null : this.data&&JSON.parse(this.data)
    }
  },
  components: {
    MagicStructureObject,
    MagicStructureArray
  },
}
</script>
<style>
  .ma-structure-container {
    display: flex;
    flex-direction: row;
    height: 100%;
    font-size: 14px;
  }
  .ma-structure-container label{
    padding-right: 2px;
  }
  .ma-structure-container span{
    display: inline-block;
    color: var(--color);
  }
  .ma-structure-container .number{
    color: var(--text-number-color);
    font-weight: bold;
  }
  .ma-structure-container .string{
    color: var(--text-string-color);
    font-weight: bold;
  }
  .ma-structure-container .boolean{
    color: var(--text-boolean-color);
    font-weight: bold;
  }
  .ma-structure-container .property{
    color: var(--text-key-color);
    font-weight: bold;
  }
  .ma-structure-container .ma-icon{
    font-size: 12px;
    color: var(--color);
  }
</style>
