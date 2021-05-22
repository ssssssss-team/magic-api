# magic-editor

`magic-editor`是 [magic-api](https://gitee.com/ssssssss-team/magic-api) 配套的前端，目前有两种引入方式。

## 一、以jar的方式引入

在 [magic-api-spring-boot-starter](https://gitee.com/ssssssss-team/magic-api) 中已经是自动引入了，所以无需配置

按照 [magic-api-spring-boot-starter](https://gitee.com/ssssssss-team/magic-api) 的集成方式，启动之后访问`http://localhost:9999/magic/web`即可

## 二、以Vue的组件的方式引入

### 安装组件
`npm install --save magic-editor` 或 `npm i --save magic-editor`

### 使用组件
```html
<template>
  <div id="app">
    <!-- 引入magic-editor组件 -->
    <magic-editor :config="config"/>
  </div>
</template>

<script>
// 引入组件
import MagicEditor from 'magic-editor'
// 引入样式
import 'magic-editor/dist/magic-editor.css'

export default {
  name: 'App',
  components: {
	MagicEditor
  },
  data(){
    return {
      config:{
        baseURL: 'http://localhost:9999/magic/web',    //配置后台服务
        serverURL: 'http://localhost:9999/'    //配置接口实际路径
        // 其它配置请参考文档
      }
    }
  }
}
</script>

<style>
html,body,#app {
  width: 100%;
  height:100%;
  margin:0;
  padding:0
}
</style>
```
需要的注意的是，这种引入方式，后台同样需要配置`magic-api.web`，但可以不需要`magic-editor`的`jar`，可以在pom中排除掉
```xml
<dependency>
    <groupId>org.ssssssss</groupId>
    <artifactId>magic-api-spring-boot-starter</artifactId>
    <version>lastest-version</version><!-- 自行替换为最新的版本号 -->
    <exclusions>
        <exclusion>
            <groupId>org.ssssssss</groupId>
            <artifactId>magic-editor</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```
