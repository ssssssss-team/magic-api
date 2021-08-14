<template>
  <div style="width: 100%">
    <input type="file" style="display: none" ref="file" @change="onFileSelected" :accept="accept" :multiple="multiple">
    <magic-input icon="upload" :readonly="true" style="width: 100%" placeholder="未选择文件" :onClick="choseFile" :value="filename"/>
  </div>
</template>

<script>
import MagicInput from './magic-input'
export default {
  name: 'MagicFile',
  components: { MagicInput },
  props: {
    placeholder: {
      type: String,
      default: '',
    },
    accept: {
      type: String,
      default: null
    },
    multiple: {
      type: Boolean,
      default: false
    },
    value: {
      type: [FileList, String]
    },
    width: {
      type: String
    }
  },
  data(){
    return {
      filename: null
    }
  },
  methods: {
    getFiles(){
      return this.$refs.file.files;
    },
    getFile(){
      return this.$refs.file.files[0];
    },
    onFileSelected(){
      if (this.$refs.file.files[0]) {
        this.filename = Array.from(this.$refs.file.files).map(it => it.name).join(',');
      }
      this.$emit('update:value', this.$refs.file.files)
    },
    choseFile(){
      this.$refs.file.click();
    }
  }
}
</script>
