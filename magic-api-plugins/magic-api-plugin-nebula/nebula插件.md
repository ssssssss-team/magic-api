---
title: nebula插件
date: 2023-08-16 09:16:55
---

### 引入依赖

```xml

<dependency>
    <groupId>org.ssssssss</groupId>
    <artifactId>magic-api-plugin-nebula</artifactId>
    <version>magic-api-lastest-version</version>
</dependency>
```

### 配置

```yml
nebula:
  hostAddress: ${NEBULA_HOSTADDRESS:localhost:9669}
  userName: ${NEBULA_USERNAME:root}
  password: ${NEBULA_PASSWORD:nebula}

```

### 使用

```js
import nebula;
var ngsl = 
    """"
        USE db_name;MATCH p_=(p:`assignee`)-[*3]-(p2:`transferor`) where  id(p2) == "阿里巴巴"  or id(p)== "阿里巴巴" RETURN p_ limit 1000'
    """
var resultJson = nebula.executeJson(ngsl)
nebula.convert(resultJson)


nebula.executeNebulaModel(ngsl)

其他支持的方法不太常用, 这里不再一一列举, 可参考源码
org.ssssssss.magicapi.nebula.NebulaModule
```

#### 返回的数据格式为:
```
    该结构的数据可被很多前端组件库支持进行可视化展示
```
如: [angv G6](http://antv-2018.alipay.com/zh-cn/g6/3.x/demo/index.html)


```json
{
    "code": 0,
    "message": "success",
    "data": {
        "nodes": [
            {
                "edgeSize": 1,
                "assignee.name": "中航纽赫融资租赁（上海）有限公司",
                "type": "vertex",
                "assignee.addr": "上海市中国（上海）自由贸易试验区正定路530号A5库区集中辅助区三层318室",
                "assignee.legal_person": "周勇",
                "registrant.addr": "上海市浦东新区南泉路1261号",
                "registrant.name": "中航国际租赁有限公司",
                "id": "中航纽赫融资租赁（上海）有限公司",
                "assignee.type": "企业"
            },
            {
                "edgeSize": 15,
                "type": "vertex",
                "transferor.name": "陕西海富融资租赁有限公司",
                "transferor.legal_person": "刘子瑜",
                "transferor.type": "企业",
                "transferor.addr": "陕西省西安市西安经济技术开发区未央路170号赛高城市广场2号楼企业总部大厦26层05单元",
                "registrant.addr": "广东省深圳市前海深港合作区南山街道梦海大厦5035号前海华润金融中心T5写字楼1808",
                "registrant.name": "深圳前海盈峰商业保理有限公司",
                "id": "陕西海富融资租赁有限公司"
            }, ...
        ],
        "edges": [
            {
                "dst": "陕西海富融资租赁有限公司",
                "src": "中航纽赫融资租赁（上海）有限公司",
                "source": "中航纽赫融资租赁（上海）有限公司",
                "label": "trans_with",
                "type": "edge",
                "target": "陕西海富融资租赁有限公司",
                "name": "trans_with",
                "ranking": 0,
                "value": 0
            },...
        ]
    },
    "timestamp": 1692149280167,
    "requestTime": 1692149280143,
    "executeTime": 24
}
```