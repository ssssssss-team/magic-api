<p align="center">
    <img src="https://www.ssssssss.org/images/logo-magic-api.png" width="256">
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
    <a target="_blank" href=https://qm.qq.com/cgi-bin/qm/qr?k=Q6dLmVS8cHwoaaP18A3tteK_o0244e6B&jump_from=webapi"><img src="https://img.shields.io/badge/QQ群-739235910-blue"></a>
</p>

[特性](#特性) | [快速开始](#快速开始) | [文档/演示](#文档演示) | [示例项目](#示例项目) | <a target="_blank" href="http://ssssssss.org/changelog.html">更新日志</a> | [项目截图](#项目截图)

# 简介

magic-api 是一个基于Java的接口快速开发框架，编写接口将通过magic-api提供的UI界面完成，自动映射为HTTP接口，无需定义Controller、Service、Dao、Mapper、XML、VO等Java对象即可完成常见的HTTP API接口开发


【已有上千家中小型公司使用，上万名开发者用于接口配置开发。上百名开发者参与提交了功能建议，接近20多名贡献者参与。已被gitee长期推荐。从首个版本开始不断优化升级，目前版本稳定，开发者交流群活跃。参与交流QQ群③739235910】

# 特性
- 支持MySQL、MariaDB、Oracle、DB2、PostgreSQL、SQLServer 等支持jdbc规范的数据库
- 支持非关系型数据库Redis、Mongodb
- 支持集群部署、接口自动同步。
- 支持分页查询以及自定义分页查询
- 支持多数据源配置，支持在线配置数据源
- 支持SQL缓存，以及自定义SQL缓存
- 支持自定义JSON结果、自定义分页结果
- 支持对接口权限配置、拦截器等功能
- 支持运行时动态修改数据源
- 支持Swagger接口文档生成
- 基于[magic-script](https://gitee.com/ssssssss-team/magic-script)脚本引擎，动态编译，无需重启，实时发布
- 支持Linq式查询，关联、转换更简单
- 支持数据库事务、SQL支持拼接，占位符，判断等语法
- 支持文件上传、下载、输出图片
- 支持脚本历史版本对比与恢复
- 支持脚本代码自动提示、参数提示、悬浮提示、错误提示
- 支持导入Spring中的Bean、Java中的类
- 支持在线调试
- 支持自定义工具类、自定义模块包、自定义类型扩展、自定义方言、自定义列名转换等自定义操作

# 快速开始

## maven引入
```xml
<!-- 以spring-boot-starter的方式引用 -->
<dependency>
	<groupId>org.ssssssss</groupId>
    <artifactId>magic-api-spring-boot-starter</artifactId>
    <version>2.0.1</version>
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

# 示例项目

- [magic-api-example](https://gitee.com/ssssssss-team/magic-api-example)

# 项目截图
| ![整体截图](https://images.gitee.com/uploads/images/2021/0711/105714_c1cacf2c_297689.png "整体截图") | ![代码提示](https://images.gitee.com/uploads/images/2021/0711/110448_11b6626b_297689.gif "代码提示") |
|---|---|
| ![DEBUG](https://images.gitee.com/uploads/images/2021/0711/110515_755f178a_297689.gif "DEBUG") | ![参数提示](https://images.gitee.com/uploads/images/2021/0711/110322_9dd6d149_297689.gif "参数提示") |
| ![远程推送](https://images.gitee.com/uploads/images/2021/0711/105803_b53e0d7e_297689.png "远程推送") | ![历史记录](https://images.gitee.com/uploads/images/2021/0711/105910_f2440ea4_297689.png "历史记录") |
| ![数据源](https://images.gitee.com/uploads/images/2021/0711/105846_7ec51a50_297689.png "数据源") | ![全局搜索](https://images.gitee.com/uploads/images/2021/0711/105823_ac18ada7_297689.png "全局搜索") |
