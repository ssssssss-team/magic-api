<h1>图南快速API 编辑器</h1>

<h2>
编译项目工程需要JDK17 
发行版oracleJDK

1、其中magic-editor为前端界面,可以编译之后修改一些图标打包jar包再导入springboot项目中来使用
2、magic-api-plugins为插件，可以自定义插件,可修改插件具体的实现
3、jar package 引入方式:magic-editor 打包, 然后引入具体的项目即可
4、如果想要修改前端代码那么修改magic-editor工程即可


</h2>


## maven引入
```xml
<!-- 以spring-boot-starter的方式引用 -->
<dependency>
	<groupId>org.ssssssss</groupId>
    <artifactId>magic-api-spring-boot-starter</artifactId>
    <version>2.1.1</version>
</dependency>
```
## 修改application.properties

```properties
server.port=9999
#配置web页面入口
magic-api.web=/magic/web
#配置文件存储位置。当以classpath开头时，为只读模式
magic-api.resource.location=/data/magic-api
```

## 在线编辑
访问`http://localhost:9999/magic/web`进行操作

# 文档/演示

- 文档地址：[https://ssssssss.org](https://ssssssss.org)
- 在线演示：[https://magic-api.ssssssss.org](https://magic-api.ssssssss.org)

