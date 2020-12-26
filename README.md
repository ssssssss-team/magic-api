<p align="center">
    <img src="https://images.gitee.com/uploads/images/2020/0516/214347_61254f60_297689.png" width="600">
</p>
<p align="center">
    <a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html"><img src="https://img.shields.io/badge/JDK-1.8+-green.svg" /></a>
    <a href="https://search.maven.org/search?q=g:org.ssssssss%20AND%20a:magic-api">
        <img alt="maven" src="https://img.shields.io/maven-central/v/org.ssssssss/magic-api.svg?style=flat-square">
    </a>
    <a target="_blank" href="https://www.ssssssss.org"><img src="https://img.shields.io/badge/Docs-latest-blue.svg"/></a>
    <a target="_blank" href="https://github.com/ssssssss-team/magic-api/releases"><img src="https://img.shields.io/github/v/release/ssssssss-team/magic-api?logo=github"></a>
    <a target="_blank" href='https://gitee.com/ssssssss-team/magic-api'><img src="https://gitee.com/ssssssss-team/magic-api/badge/star.svg?theme=white" /></a>
    <a target="_blank" href='https://github.com/ssssssss-team/magic-api'><img src="https://img.shields.io/github/stars/ssssssss-team/magic-api.svg?style=social"/></a>
    <a target="_blank" href="LICENSE"><img src="https://img.shields.io/:license-MIT-blue.svg"></a>
    <a target="_blank" href="https://shang.qq.com/wpa/qunwpa?idkey=10faa4cf9743e0aa379a72f2ad12a9e576c81462742143c8f3391b52e8c3ed8d"><img src="https://img.shields.io/badge/QQ群-720832964-blue"></a>
</p>

[特性](#特性) | [快速开始](#快速开始) | [文档/演示](#文档演示) | <a target="_blank" href="http://ssssssss.org/changelog.html">更新日志</a> | [项目截图](#项目截图) | [其它开源](#其它开源项目)

# 简介
magic-api 是一个基于Java的接口快速开发框架，编写接口将通过magic-api提供的UI界面完成，自动映射为HTTP接口，无需定义Controller、Service、Dao、Mapper、XML、VO等Java对象即可完成常见的HTTP API接口开发

# 特性
- 支持MySQL、MariaDB、Oracle、DB2、PostgreSQL、SQLServer 等多支持jdbc规范的数据库
- 支持非关系型数据库Redis、Mongodb
- 支持分页查询以及自定义分页查询
- 支持多数据源配置，支持运行时动态添加数据源
- 支持SQL缓存，以及自定义SQL缓存
- 支持SQL拦截、自定义分页方言、自定义列名转换
- 支持自定义JSON结果、自定义分页结果
- 支持对接口权限配置、拦截器等功能
- 支持运行时动态修改数据源
- 支持Swagger接口文档生成
- 基于[magic-script](https://gitee.com/ssssssss-team/magic-script)脚本引擎，动态编译，无需重启，实时发布
- 支持Linq式查询，关联、转换更简单
- 支持数据库事务、SQL支持拼接，占位符，判断等语法
- 支持文件上传、下载、输出图片
- 支持脚本历史版本对比与恢复
- 支持脚本代码自动提示、错误提示、参数提示、语法错误提示
- 支持导入Spring中的Bean、Java中的类
- 支持在线调试脚本引擎
- 支持自定义工具类、自定义模块包、自定义类型扩展、自定义函数等

# 快速开始

## maven引入
```xml
<!-- 以spring-boot-starter的方式引用 -->
<dependency>
	<groupId>org.ssssssss</groupId>
	<artifactId>magic-api-spring-boot-starter</artifactId>
    <version>0.5.5</version>
</dependency>
```
## 修改application.properties

```properties
server.port=9999
#配置web页面入口
magic-api.web=/magic/web
#以下配置需跟实际情况修改
spring.datasource.url=jdbc:mysql://localhost/test
spring.datasource.username=root
spring.datasource.password=123456789
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
```

## 执行建表语句

执行本仓库下[db/magic-api.sql](./db/magic-api.sql)建表语句

## 在线编辑
访问`http://localhost:9999/magic/web`进行操作

# 文档/演示

- 文档地址：[https://ssssssss.org](https://ssssssss.org)
- 在线演示：[http://140.143.210.90:9999/magic/web](http://140.143.210.90:9999/magic/web)

# 项目截图

## 整体截图
![整体截图](https://images.gitee.com/uploads/images/2020/1220/143206_a056da00_297689.png "full.png")
## 切换主题
![切换皮肤](https://images.gitee.com/uploads/images/2020/1220/143305_955ad124_297689.png "skin.png")
## 代码提示
![代码提示](https://images.gitee.com/uploads/images/2020/1220/143322_966caf97_297689.gif "completion.gif")
## DEBUG
![DEBUG](https://images.gitee.com/uploads/images/2020/1220/143340_b2d83a49_297689.gif "debug.gif")
## 历史记录
![历史记录](https://images.gitee.com/uploads/images/2020/1220/143412_d8414820_297689.png "version.png")

# 其它开源项目
- [magic-api-spring-boot-starter](https://gitee.com/ssssssss-team/magic-api-spring-boot-starter)
- [magic-script，基于Java实现的脚本引擎](https://gitee.com/ssssssss-team/magic-script)
- [magic-editor，本项目的前端UI](https://gitee.com/ssssssss-team/magic-editor)
- [spider-flow，新一代爬虫平台，以图形化方式定义爬虫流程，不写代码即可完成爬虫](https://gitee.com/ssssssss-team/spider-flow)