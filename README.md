HTT...
<p align="center">
    <img src="https://www.ssssssss.org/images/logo-magic-api.png" width="256">
</p>
<p align="center">
    <a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html"><img src="https://img.shields.io/badge/ JDK-1.8+-green.svg" /></a>
    <a href="https://search.maven.org/search?q=g:org.ssssssss%20AND%20a:magic-api">
        <img alt="maven" src="https://img.shields.io/maven-central/v/org.ssssssss/magic-api.svg?style=flat-square">
    </a>
    <a target="_blank" href="https://www.ssssssss.org"><img src="https://img.shields.io/badge/Docs-latest-blue.svg"/></ a>
    <a target="_blank" href="https://github.com/ssssssss-team/magic-api/releases"><img src="https://img.shields.io/github/v/release/ ssssssss-team/magic-api?logo=github"></a>
    <a target="_blank" href='https://gitee.com/ssssssss-team/magic-api'><img src="https://gitee.com/ssssssss-team/magic-api/badge/ star.svg?theme=white" /></a>
    <a target="_blank" href='https://github.com/ssssssss-team/magic-api'><img src="https://img.shields.io/github/stars/ssssssss-team/ magic-api.svg?style=social"/></a>
    <a target="_blank" href="LICENSE"><img src="https://img.shields.io/:license-MIT-blue.svg"></a>
    <a target="_blank" href=https://qm.qq.com/cgi-bin/qm/qr?k=Q6dLmVS8cHwoaaP18A3tteK_o0244e6B&jump_from=webapi"><img src="https://img.shields.io/badge /QQ group-739235910-blue"></a>
</p>

[features] (#features) | [quickstart] (#quickstart) | [documentation/demo] (#documentation demo) | [example project] (#example project) | <a target="_blank" href="http ://ssssssss.org/changelog.html">Changelog</a> | [project screenshot](#project screenshot)

# Introduction

magic-api is a Java-based interface rapid development framework, the writing interface will be completed through the UI interface provided by magic-api, automatically mapped to HTTP interface, no need to define Java objects such as Controller, Service, Dao, Mapper, XML, VO Complete common HTTP API interface development


[It has been used by thousands of small and medium-sized companies, and tens of thousands of developers have used it for interface configuration development. Hundreds of developers participated in submitting feature suggestions, and nearly 20 contributors participated. It has been recommended by gitee for a long time. Since the first version, it has been continuously optimized and upgraded. The current version is stable and the developer exchange group is active. Participate in the communication QQ group ③739235910]

# Features
- Support MySQL, MariaDB, Oracle, DB2, PostgreSQL, SQLServer and other databases that support jdbc specification
- Support non-relational database Redis, Mongodb
- Supports cluster deployment and automatic interface synchronization.
- Support paging query and custom paging query
- Support multiple data source configuration, support online configuration data source
- Support SQL cache, and custom SQL cache
- Support custom JSON results, custom pagination results
- Support for interface permission configuration, interceptor and other functions
- Support dynamic modification of data sources at runtime
- Support Swagger interface document generation
- Based on [magic-script](https://gitee.com/ssssssss-team/magic-script) script engine, dynamic compilation, no need to restart, real-time release
- Support Linq-style query, making association and conversion easier
- Support database transactions, SQL support splicing, placeholders, judgment and other syntax
- Support file upload, download, output picture
- Support script historical version comparison and restoration
- Support script code automatic prompt, parameter prompt, floating prompt, error prompt
- Support importing Beans in Spring and classes in Java
- Support online debugging
- Supports custom operations such as custom tool classes, custom module packages, custom type extensions, custom dialects, and custom column name conversions

# quick start

## maven import
```xml
<!-- Referenced by spring-boot-starter -->
<dependency>
	<groupId>org.ssssssss</groupId>
    <artifactId>magic-api-spring-boot-starter</artifactId>
    <version>2.0.1</version>
</dependency>
```
## Modify application.properties

```properties
server.port=9999
#Configure web page entry
magic-api.web=/magic/web
#Configuration file storage location. When starting with classpath, it is read-only mode
magic-api.resource.location=/data/magic-api
```

## Online Editing
Visit `http://localhost:9999/magic/web` to operate

# Documentation/Demo

- Document address: [https://ssssssss.org](https://ssssssss.org)
- Online demo: [https://magic-api.ssssssss.org](https://magic-api.ssssssss.org)

# example project

- [magic-api-example](https://gitee.com/ssssssss-team/magic-api-example)

# project screenshot
| ![Overall Screenshot](https://images.gitee.com/uploads/images/2021/0711/105714_c1cacf2c_297689.png "Overall Screenshot") | ![Code Tips](https://images.gitee.com/ uploads/images/2021/0711/110448_11b6626b_297689.gif "Code Hint") |
|---|---|
| ![DEBUG](https://images.gitee.com/uploads/images/2021/0711/110515_755f178a_297689.gif "DEBUG") | ![Parameter prompt](https://images.gitee.com/uploads/ images/2021/0711/110322_9dd6d149_297689.gif "Parameter Prompt") |
| ![Remote Push](https://images.gitee.com/uploads/images/2021/0711/105803_b53e0d7e_297689.png "Remote Push") | ![History](https://images.gitee.com/ uploads/images/2021/0711/105910_f2440ea4_297689.png "History") |
| ![Data Source](https://images.gitee.com/uploads/images/2021/0711/105846_7ec51a50_297689.png "Data Source") | ![Global Search](https://images.gitee.com/ uploads/images/2021/0711/105823_ac18ada7_297689.png "Global Search") |
