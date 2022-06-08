export default function (bus, constants, $i, Message, request) {
    return {
        // svg text
        getIcon: item => ['Vue', '#41B883'],
        // 任务名称
        name: $i('component.name'),
        // 脚本语言
        language: 'html',
        // 默认脚本
        defaultScript: `<template>

</template>
<script setup>

</script>
<style scoped>

</style>`,
        // 是否允许执行测试
        runnable: false,
        // 是否需要填写路径
        requirePath: true,
        // 合并
        merge: item => item
    }
}
