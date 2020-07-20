CREATE TABLE magic_api_info (
    id varchar2(32) NOT NULL ,
    api_method varchar2(12) ,
    api_path varchar2(512) ,
    api_script clob ,
    api_parameter clob ,
    api_option clob ,
    api_name varchar2(255) ,
    api_group_name varchar2(255) ,
    api_group_prefix varchar2(255) ,
    api_output clob ,
    api_create_time number(20) ,
    api_update_time number(20)
);
COMMENT ON COLUMN magic_api_info.api_method IS '请求方法';
COMMENT ON COLUMN magic_api_info.api_path IS '请求路径';
COMMENT ON COLUMN magic_api_info.api_script IS '接口脚本';
COMMENT ON COLUMN magic_api_info.api_parameter IS '接口参数';
COMMENT ON COLUMN magic_api_info.api_option IS '接口选项';
COMMENT ON COLUMN magic_api_info.api_name IS '接口名称';
COMMENT ON COLUMN magic_api_info.api_group_name IS '接口分组';
COMMENT ON COLUMN magic_api_info.api_group_prefix IS '分组前缀';
COMMENT ON COLUMN magic_api_info.api_output IS '输出结果';
COMMENT ON COLUMN magic_api_info.api_create_time IS '创建时间';
COMMENT ON COLUMN magic_api_info.api_update_time IS '修改时间';
COMMENT ON TABLE magic_api_info IS 'MagicAPI接口信息';

CREATE TABLE magic_api_info_his (
    id varchar2(32) ,
    api_method varchar2(12) ,
    api_path varchar2(512) ,
    api_script clob ,
    api_parameter clob ,
    api_option clob ,
    api_name varchar2(255) ,
    api_group_name varchar2(255) ,
    api_group_prefix varchar2(255) ,
    api_output clob ,
    api_create_time number(20) ,
    api_update_time number(20)
);
COMMENT ON COLUMN magic_api_info_his.id IS 'api_id';
COMMENT ON COLUMN magic_api_info_his.api_method IS '请求方法';
COMMENT ON COLUMN magic_api_info_his.api_path IS '请求路径';
COMMENT ON COLUMN magic_api_info_his.api_script IS '接口脚本';
COMMENT ON COLUMN magic_api_info_his.api_parameter IS '接口参数';
COMMENT ON COLUMN magic_api_info_his.api_option IS '接口选项';
COMMENT ON COLUMN magic_api_info_his.api_name IS '接口名称';
COMMENT ON COLUMN magic_api_info_his.api_group_name IS '接口分组';
COMMENT ON COLUMN magic_api_info_his.api_group_prefix IS '分组前缀';
COMMENT ON COLUMN magic_api_info_his.api_output IS '输出结果';
COMMENT ON COLUMN magic_api_info_his.api_create_time IS '创建时间';
COMMENT ON COLUMN magic_api_info_his.api_update_time IS '修改时间';



INSERT INTO MAGIC_API_INFO VALUES ('033239e63a2a42b987567a37a2efdd32', 'GET', '/download', 'import response;
return response.download(''中文测试'',''str.txt'');', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"body" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '文件下载', '自定义结果', '/custom', NULL, '1595050133467', '1595065447606');
INSERT INTO MAGIC_API_INFO VALUES ('104219ceb2e34de38c1d4389cb0a094e', 'GET', '/json', 'import response;
return response.json({
    success : true,
    message : ''执行成功''
});', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"body" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '自定义json', '自定义结果', '/custom', '{
    "success": true,
    "message": "执行成功"
}', '1595065423867', '1595065447606');
INSERT INTO MAGIC_API_INFO VALUES ('180524e850124de7956d855bc94bcac9', 'GET', '/if', '/*
    if 测试
*/
if(a == 1){
    return 1;
}else if(a == 2){
    return 2;
}else{
    return 0;
}', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', 'if测试', '语法测试', '/test', NULL, '1593514724505', '1594736129503');
INSERT INTO MAGIC_API_INFO VALUES ('343ac4fb280941a6bb7ddc523b331fee', 'GET', '/page', 'import response;
var total = 5;  //模拟一共有多少条数据
var list = [1,2];   //模拟数据项
return response.page(total,list);', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"body" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '自定义分页', '自定义结果', '/custom', '{
    "code": 1,
    "message": "success",
    "data": {
        "total": 5,
        "list": [
            1,
            2
        ]
    },
    "timestamp": 1595065175380
}', '1595065176504', '1595065447606');
INSERT INTO MAGIC_API_INFO VALUES ('4cd4297dba064c888e571cffbb00d729', 'GET', '/assert', 'import assert;
assert.notNull(message, 0, ''message 不能为null'');
assert.regx(id,''\d+'', 0, ''id必须是数字'');
return ''ok'';', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456a"
	},
	"body" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '参数验证', '其它测试', '/other', '{
    "code": 0,
    "message": "id必须是数字",
    "data": null,
    "timestamp": 1595065764237
}', '1595065796210', '1595065959708');
INSERT INTO MAGIC_API_INFO VALUES ('6cf9a1ad659f4707be704528632778c5', 'GET', '/loop/map', '/*
    测试循环Map
*/
var map = {
    key1 : 1,
    key2 : 2,
    key3 : 3
};
var sum = 0;
var keys = '''';
for(key,value in map){
    sum = sum + value;
    keys = keys + key
}
return keys + ''-'' + sum;', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '测试循环Map', '语法测试', '/test', NULL, '1593515248339', '1594736134584');
INSERT INTO MAGIC_API_INFO VALUES ('7557a409802b4742b590122ef030b643', 'GET', '/expression', 'var ids = [''1'',''2'',''3''];

//具体执行的SQL请看打印的运行日志信息。

db.select(''select * from magic_api_info where id in (#{ids})'')  //对参数自动展开

var id = ''123'';
db.select("select * from magic_api_info where id = ''${id}'' ")  //拼接字符串的方式


//var name = ''123'';
db.select("select * from magic_api_info where id = ''123'' ?{name,and api_name = #{name}}")  //if 判断

return ''ok'';', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"body" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '参数测试', 'SQL测试', '/sql', NULL, '1595066679177', '1595251019503');
INSERT INTO MAGIC_API_INFO VALUES ('8621986dbb6945c6bd8a01dbcecfe9a5', 'GET', '/filter-map', 'var list = [{
    sex : 0,
    name : ''小明''
},{
    sex : 1,
    name : ''小花''
}]
// 利用map函数对list进行过滤，然后进行转换
return list.filter(item=>item.sex == 0).map((item)=>{
    sex : item.sex == 0 ? ''男'' : ''女'',
    name : item.name
});', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"body" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '过滤和转换', '结果转换', '/convert', '{
    "code": 1,
    "message": "success",
    "data": [
        {
            "sex": "男",
            "name": "小明"
        }
    ],
    "timestamp": 1595066270905
}', '1595066278706', '1595066278706');
INSERT INTO MAGIC_API_INFO VALUES ('9815c54b31f64a9cb3a068934df21c25', 'POST', '/var', '/*
    测试变量定义
*/
var int = 1;
var double = 2.0d; //2.0D
var long =  3L; // 3l
var float =  4f; // 4F
var byte =  5b; // 5B;
var short = 6s; //6S
var boolean = true; //true or false;
var nullValue = null; // null
var list = [1,2,3,4,5]; //定义list
var map = {
    k1 : 123,
    k2 : "456",
    k3 : 789L,
    k4 : {
        k5 : ''...''
    }
};  //定义map
var string1 = "str";    //定义字符串
var string2 = ''str2''; // \t \n \r \'' \" \\ 转义符是支持的，其它的不支持
//文本块，主要用于定义SQL
var string3 = """
    select
        *
    from table t1
    join table2 t2 on t2.xx = t1.xx
    where t1.id in (1,2,3,4,5,6)
"""

var lambda = e=>e+1;    //定义lambda', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '测试定义变量', '语法测试', '/test', NULL, '1593519576351', '1594736110904');
INSERT INTO MAGIC_API_INFO VALUES ('48095f19fa3a455296bf96b244a3c60c', 'GET', '/image', 'import ''java.awt.image.BufferedImage'' as BufferedImage;
import ''java.awt.Color'' as Color;
import ''java.awt.Font'' as Font;
import ''java.io.ByteArrayOutputStream'' as ByteArrayOutputStream;
import ''java.util.Random'' as Random;
import ''javax.imageio.ImageIO'' as ImageIO;
import response;
import log;

var width = 200;
var height = 69;
var image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
var graphics = image.getGraphics();
graphics.setColor(Color.WHITE);
graphics.fillRect(0,0,width,height);
graphics.setFont(new Font("微软雅黑", Font.BOLD, 40));
var letter = ''123456789abcdefghijklmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ'';
var random = new Random();
var randomColor = ()=>new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
var x = 10;
var code = '''';
for (i in range(0, 3)){ //验证码
    graphics.setColor(randomColor());
    var degree = random.nextInt() % 30;
    var ch = letter.charAt(random.nextInt(letter.length()));
    code = code + ch;
    graphics.rotate(degree * 3.1415926535 / 180, x, 45);
    graphics.drawString(ch + '''', x, 45);
    graphics.rotate(-degree * 3.1415926535 / 180, x, 45);
    x = x + 48;
}
log.info(''生成的验证码:{}'',code)
for (i in range(0, 6)) {    //干扰线
    graphics.setColor(randomColor());
    graphics.drawLine(random.nextInt(width), random.nextInt(height),random.nextInt(width), random.nextInt(height));
}

for(i in range(0, 30)){  //噪点
    graphics.setColor(randomColor());
    graphics.fillRect(random.nextInt(width), random.nextInt(height), 2,2);

}
graphics.dispose();
var baos = new ByteArrayOutputStream();
ImageIO.write(image,"png",baos);
baos.flush();
baos.close();
return response.image(baos.toByteArray(),''image/png'');
', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"body" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '生成验证码', '自定义结果', '/custom', '"iVBORw0KGgoAAAANSUhEUgAAAMgAAABFCAIAAACAFD7PAAAElklEQVR42u3bTYhOYRQH8GmspyykxkKzMNSYMikpITa2CitNFpQyNVMWbEbNwkYNFnaTYqGUBZHFLAhTiiJFyWYsiCxmMRshpXF0dXu6H889z/fH/Z/+y/veIb/Oc54zr4E1FMpBDUT4Z7o6up+CfxvAAq9s69T9L5SsYIm8IAyw0MDC18/ZVUpER+GPm58oaGC9grVz8SKl77AgzHr5gJVigRduhWhggJVLA1t3ZKGMo5/4bHIPBZL8wVo9OlskYAPjqBpaOldEjxRg+YBVYhLj+a8kdqmSF+eDpbBOZyIpua2Z8fcUwMoH1pnLLygir84JbOndnMRZSa1RFWApw1Ii4hQWZ2AqHxBhMWf8Ciyxpg+MUSrOmLbyq+tfz+vD0iPS+ClzWyIpJiz5w2282mwVsCqM+EcnYFnoPbHBUlpSSJpW52jVH2dGsOw2LbvnYCnm0fwDimq7kjcwiS2ls69tRAOsHGBpLClswTK5dfYI1tS+f4kNlsZ01VkEq20C64SlRC1dYX2Hpf2ziqZVn8AkM1avLoaZH4UVOtZh1Wf8tg1W35YOYWAFaVoWVUlGeCavFK38GZyghIQV52noB1Zx5HU2sOQ8NcYhrCQuhhbH9k5YFUCNvDIg1Wkrf1gFIOuq6rYkB1+KXwXjqJLwyg1Wm6GwsMpGlRAvJVh1W72A1ZbOV02OHKfYtbWWwndZ5YDSg+XuNNSzZQKLsyyNVhiHjvwBV7Bim99N+pbqgchZljYKixmW6jMWYKU7ZtniZetXh/E0MObtT/JYj2C5s6UEi7NuCM6LuVMArAXmBOZiWVpm7ONEGc47w/LSOAp7DcuRLRGWCEhMWgvSeGesipLvU7cpATcO4gOblrdQDG21AbJyGrqrh+O3zG11XhvtwOIoqcBy/X91TGDVbXEAufv2n0VSclWqS9FgsCRWwsJqe4zZhPyM8J4blUVVTFtGsOpc+E+6g9UGqP6pV9uWKZ73Dp4blaGqtnfagSUXw0n5ntcLlyhWYDE7kORMdAfLjy3ORGWdVESwxPfoweJ0INV5X+OquPfJLkoMTYvTqCSq9DZbTmBp2zIH1DgGqfqwsndQguXOlvbVj79n9wpLw5Y5IMP53W5JTsN7c98ormExGxVzHRURLD6vRkyLpzdQlABFBUtiqwIrbKPSgLX18HpKgBnLyk6hgOV56eCtaTklpaEqVVjBi4npwtnNFD+2ImlUGrBUT8OcYTErUVi7f61QTFSZzFiuYK08/UxZQwk1+HI7xTMs8/fofa0PsALAcm1Le6LSW2UFXpCi/JyGFkl10tFeuwNWSrbsNipDW4CVCSx3pDR4cV61Y+ANBbDiheW0UWnYYr4HsKK25ZOU4X6BfxTOf7hByQjW8+H/SQGW50ZlvdKDdWzkJMWOMPfU9GwlTSrM8H7nyiFKYFgeqanCSr1RJQzL97lp7IxvKw9SWDd4amkVWDcWr1FybVT8AizTq0AnrOxJbRz6TQGsGPcOgIUCLByFsAVYPmv08TQFsAALsAALpyEKsAALsGAr0ro7PEMBLMACLJRKvT14goKjEJU5rL+mrI6dI4qKvwAAAABJRU5ErkJggg=="', '1595051666872', '1595066035326');
INSERT INTO MAGIC_API_INFO VALUES ('523c2ae2b50a4465a0d8de83e5436da9', 'GET', '/map', 'var list = [{
    sex : 0,
    name : ''小明'',
    age : 19
},{
    sex : 1,
    name : ''小花'',
    age : 18
}];

var getAge = (age)=>{
    return age > 18 ? ''成人'' : ''未成年''
}
// 利用map函数对list进行过滤
return list.map((item)=>{
    age : getAge(item.age),
    sex : item.sex == 0 ? ''男'' : ''女'',
    name : item.name
});', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"body" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '字段转换', '结果转换', '/convert', '{
    "code": 1,
    "message": "success",
    "data": [
        {
            "sex": "男",
            "name": "小明",
            "age": "成人"
        },
        {
            "sex": "女",
            "name": "小花",
            "age": "未成年"
        }
    ],
    "timestamp": 1595066113546
}', '1595066116077', '1595066227461');
INSERT INTO MAGIC_API_INFO VALUES ('986acacbb7a84ea58c2ceff7e74e04b5', 'GET', '/log', 'import log;
// 切换到"运行日志"查看日志信息
log.info(''info日志:{}'',message);
log.warn(''warn日志'');
try{
    return 1 / 0;
}catch(e){
    log.error(''error日志'',e);
}
return ''ok'';', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"body" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '测试日志', '其它测试', '/other', '{
    "code": 1,
    "message": "success",
    "data": "ok",
    "timestamp": 1595065936684
}', '1595065953402', '1595065959708');
INSERT INTO MAGIC_API_INFO VALUES ('998b865018a146aab9abd8f4ffdc8359', 'GET', '/cache', 'return db.cache(''test'').select(''select api_name,api_method,api_path from magic_api_info'');', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"body" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '缓存测试', 'SQL测试', '/sql', NULL, '1595064750136', '1595064750136');
INSERT INTO MAGIC_API_INFO VALUES ('a4ebe5b3b0a143e797e0027e37b089d3', 'GET', '/page', 'return db.page(''select api_name from magic_api_info'')', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"body" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '分页测试', 'SQL测试', '/sql', '{
    "code": 1,
    "message": "success",
    "data": {
        "total": 12,
        "list": [
            {
                "apiName": "文件下载"
            },
            {
                "apiName": "if测试"
            },
            {
                "apiName": "生成验证码"
            },
            {
                "apiName": "测试循环Map"
            },
            {
                "apiName": "测试定义变量"
            },
            {
                "apiName": "缓存测试"
            },
            {
                "apiName": "操作符测试"
            },
            {
                "apiName": "测试循环List"
            },
            {
                "apiName": "测试lambda"
            },
            {
                "apiName": "测试创建对象"
            }
        ]
    },
    "timestamp": 1595064916305
}', '1595064929177', '1595064929177');
INSERT INTO MAGIC_API_INFO VALUES ('b7df52ff308e481abceda07d7d3ef62c', 'GET', '/binary', '/*
    各种操作符测试
*/
import ''java.lang.System'' as system;
var a = 1;
var b = 2;
system.out.println("a = " + a + ", b = " + b);
var c = a + b;
system.out.println("a + b = " + c);
c = a - b;
system.out.println("a - b = " + c);
c = a * b;
system.out.println("a * b = " + c);
c = a / b;
system.out.println("a / b = " + c);
c = a % b;
system.out.println("a % b = " + c);
c = a > b;
system.out.println("a > b = " + c);
c = a >= b;
system.out.println("a >= b = " + c);
c = a == b;
system.out.println("a == b = " + c);
c = a < b;
system.out.println("a < b = " + c);
c = a <= b;
system.out.println("a <= b = " + c);
c = a != b;
system.out.println("a != b = " + c);', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '操作符测试', '语法测试', '/test', NULL, '1593514691506', '1594903771663');
INSERT INTO MAGIC_API_INFO VALUES ('cd3c9e4c09fc44fdb82c0f1b783f59af', 'GET', '/loop/list', '/*
    测试循环List
*/
var list = [1,2,3,4,5];
var sum = 0;
for(val in list){
    sum = sum + val;
}
return sum;', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '测试循环List', '语法测试', '/test', '{
    "code": 1,
    "message": "success",
    "data": 15,
    "timestamp": 1594915393436
}', '1593515155753', '1594915394105');
INSERT INTO MAGIC_API_INFO VALUES ('d338de01930f4149b4ea85c9f1f88387', 'GET', '/lambda', '/*
    测试Lambda
*/
var lambda1 = e => e + 1; //单参数单行代码，省略括号,省略{}
var lambda2 = (e) => e +1; //单参数单行代码，不省略括号，省略{} 作用同上
var lambda4 = e => {e + 1};//单参数无返回值，不能省略{}
var lambda5 = e => {return e + 1};//单参数有返回值，省略括号,不省略{}
var lambda6 = (e) => {return e + 1};//单参数有返回值，不省略括号,不省略{}，作用同上
var lambda7 = (a,b) => a + b; //多参数单行代码，省略{}
var lambda7 = (a,b) => {return a + b}; //多参数单行代码，有返回值，作用同上
var lambda8 = (a,b) =>{ //多参数多行代码， 无法省略括号和{}
    a = a + 1;
    return a + b;
}
var v1 = lambda1(1);    //返回2
var v2 = lambda2(v1);    //返回3
return lambda8(v1,lambda7(v1,v2)); //返回8
', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '测试lambda', '语法测试', '/test', '{
    "code": 1,
    "message": "success",
    "data": 8,
    "timestamp": 1594915477773
}', '1593518831250', '1594915478585');
INSERT INTO MAGIC_API_INFO VALUES ('dafa5ca6daf9419995e9a4a01eea1e57', 'GET', '/filter', 'var list = [{
    sex : 0,
    name : ''小明''
},{
    sex : 1,
    name : ''小花''
}]
// 利用map函数对list进行过滤
return list.filter((item)=>item.sex == 0);', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"body" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', 'List过滤', '结果转换', '/convert', '{
    "code": 1,
    "message": "success",
    "data": [
        {
            "sex": 0,
            "name": "小明"
        }
    ],
    "timestamp": 1595066209866
}', '1595066211481', '1595066227461');
INSERT INTO MAGIC_API_INFO VALUES ('dccb42bc1d974d99b0ebd9a12d42c47b', 'GET', '/new', 'import ''java.util.Date'' as Date;
import ''java.text.SimpleDateFormat'' as SimpleDateFormat;
var now = new Date();
var df = new SimpleDateFormat(''yyyy-MM-dd'');
return df.format(now);', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '测试创建对象', '语法测试', '/test', '{
    "code": 1,
    "message": "success",
    "data": "2020-07-18",
    "timestamp": 1595040221517
}', '1593525594254', '1595040241898');
INSERT INTO MAGIC_API_INFO VALUES ('ee45724999ad400c927f5a267f6b8676', 'GET', '/test/for', '/*
    测试循环
*/
var sum = 0;
for(val in range(0,100)){   //包括0 包括100
    if(val > 90){
        break;  //跳出循环
    }
    if(val % 3 == 0){
        continue;   //进入下一次循环
    }
    sum = sum + val;
}
return sum;', '{
	"request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '测试for循环', '语法测试', '/test', NULL, '1593515005267', '1594735986968');
INSERT INTO MAGIC_API_INFO VALUES ('ff2135698c6e4d1bad0db59195dfe706', 'GET', '/select', 'var sql = """
    select
        ''${message}'' as user_name,
                                      #{id} as user_id
                                   """;
                                   return db.select(sql);', '{
                                       "request" : {
		"message" : "Hello MagicAPI!"
	},
	"path" : {
		"id" : "123456"
	},
	"header" : {
		"token" : "tokenValue"
	},
	"cookie" : {
		"cookieName" : "cookieValue"
	},
	"session" : {
		"userId" : "123"
	}
}', '{
}', '测试执行SQL', 'SQL测试', '/sql', NULL, '1593005960511', '1594990967188');