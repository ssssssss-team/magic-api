<p align="center">
    <img src="https://images.gitee.com/uploads/images/2020/0516/214347_61254f60_297689.png" width="600">
</p>
<p align="center">
    <a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html"><img src="https://img.shields.io/badge/JDK-1.8+-green.svg" /></a>
    <a href="https://search.maven.org/search?q=g:org.ssssssss">
        <img alt="maven" src="https://img.shields.io/maven-central/v/org.ssssssss/magic-api.svg?style=flat-square">
    </a>
    <a target="_blank" href="https://www.ssssssss.org"><img src="https://img.shields.io/badge/Docs-latest-blue.svg"/></a>
    <a target="_blank" href="https://github.com/ssssssss-team/magic-api-spring-boot-starter/releases"><img src="https://img.shields.io/github/v/release/ssssssss-team/magic-api-spring-boot-starter?logo=github"></a>
    <a target="_blank" href='https://gitee.com/ssssssss-team/magic-api-spring-boot-starter'><img src="https://gitee.com/ssssssss-team/magic-api-spring-boot-starter/badge/star.svg?theme=white" /></a>
    <a target="_blank" href='https://github.com/ssssssss-team/magic-api-spring-boot-starter'><img src="https://img.shields.io/github/stars/ssssssss-team/magic-api-spring-boot-starter.svg?style=social"/></a>
    <a target="_blank" href="LICENSE"><img src="https://img.shields.io/:license-MIT-blue.svg"></a>
    <a target="_blank" href="https://shang.qq.com/wpa/qunwpa?idkey=10faa4cf9743e0aa379a72f2ad12a9e576c81462742143c8f3391b52e8c3ed8d"><img src="https://img.shields.io/badge/Join-QQGroup-blue"></a>
</p>

[特性](#特性) | [快速开始](#快速开始) |  <a target="_blank" href="http://ssssssss.org">文档</a> | <a target="_blank" href="http://ssssssss.org/changelog.html">更新日志</a> | [其它开源](#其它开源项目)

# 特性
-  以XML为基础，自动映射HTTP接口
-  支持MySQL、MariaDB、Oracle、DB2、PostgreSQL、SQLServer 等多种数据库
-  支持参数自动校验以及自定义参数校验
-  支持分页查询以及自定义分页查询
-  支持XML中调用java方法
-  支持执行多条sql语句
-  支持多数据源
-  支持主键自动生成，可自定义配置主键生成策略
-  自动热更新
-  支持缓存
-  ~~支持单表自动映射CRUD~~
-  ~~支持调用存储过程~~

# 快速开始

## maven引入
```xml
<!-- 以spring-boot-starter的方式引用 -->
<dependency>
	<groupId>org.ssssssss</groupId>
	<artifactId>magic-api-spring-boot-starter</artifactId>
    <version>0.1.1</version>
</dependency>
```
## 修改application.properties

```properties
server.port=9999
#配置magic-api的xml所在位置
magic-api.xml-locations: classpath*:magic-api/**/*.xml
#以下配置需跟实际情况修改
spring.datasource.url=jdbc:mysql://localhost/test
spring.datasource.username=root
spring.datasource.password=123456789
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
```

## 创建XML

在`src/main/resources/magic-api/`下建立`user.xml`文件
```xml
<?xml version="1.0" encoding="utf-8" ?>
<magic request-mapping="/user" 
        xmlns="http://ssssssss.org/schema"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://ssssssss.org/schema http://ssssssss.org/schema/magic-0.1.xsd">
    <!-- 访问地址/user/list,访问方法get,并开启分页 -->
    <select-list request-mapping="/list" request-method="get" page="true">
        select username,password from sys_user
    </select-list>
</magic>
```

## 测试
访问`http://localhost:9999/user/list`

结果如下：
```json
{
	"code": 1,
	"message": "success",
	"data": {
		"total": 2,
		"list": [{
			"password": "123456",
			"username": "admin"
		}, {
			"password": "1234567",
			"username": "1234567"
		}]
	},
	"timestamp": 1588586539249
}
```

# 其它开源项目
- [magic-api](https://gitee.com/ssssssss-team/magic-api)
- [spider-flow，新一代爬虫平台，以图形化方式定义爬虫流程，不写代码即可完成爬虫](https://gitee.com/ssssssss-team/spider-flow)