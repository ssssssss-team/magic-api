import 'org.ssssssss.magicapi.provider.GroupServiceProvider' as GroupServiceProvider
import 'org.ssssssss.magicapi.provider.ApiServiceProvider' as ApiServiceProvider
import 'org.ssssssss.magicapi.provider.FunctionServiceProvider' as FunctionServiceProvider
import 'org.ssssssss.magicapi.utils.IoUtils' as IoUtils
import 'org.ssssssss.magicapi.utils.JsonUtils' as JsonUtils
import 'java.io.File' as File;
var ds = db.camel();    //如果之前保存在其他库，这里可以修改为db.xxx.camel();
// 文件保存目录
var workspace = new File('E:\\magic\\');
var apiSql = """ select * from magic_api_info """;
var groupSql = """ select * from magic_group where deleted = '0' """;
var functionSql = """ select * from magic_function """;
// 替换key，去除前缀，将首字母小写。
var replaceKey = (it,src) => it.replaceKey(src,'').replaceKey(it => it.substring(0,1).toLowerCase() + it.substring(1));
// list转tree
var toTree = (list,parentId)=>list.filter(it => it.parentId == parentId).each(it => it.children = toTree(list,it.id))
// 查询分组列表
var groupList = ds.select(groupSql).map(it => replaceKey(it,"group"));
// 将接口分组转为tree
var apiTree = toTree(groupList.filter(it => it.type == '1'),'0');
// 将函数分组转为tree
var functionTree = toTree(groupList.filter(it => it.type == '2'),'0');
// 记录分组所在路径
var groupPath = {};
// 处理分组
var processGroup = (parentFile,list)=>{
    list.each(it => {
        var file = new File(parentFile,it.name);
        file.mkdirs();
        groupPath[it.id] = file.absolutePath;
        // 防止序列化children
        var children = it.remove('children');
        IoUtils.write(new File(file,'group.json'),JsonUtils.toJsonString(it));
        if(children){
            processGroup(file,children);
        }
    });
}
// 处理接口分组
processGroup(new File(workspace,'api'),apiTree);
// 处理函数分组
processGroup(new File(workspace,'function'),functionTree);
// 处理接口
ds.select(apiSql).map(it => replaceKey(it,'api')).each(it => IoUtils.write(new File(groupPath[it.groupId],it.name + '.ms'),ApiServiceProvider.serialize(it)));
// 处理函数
ds.select(functionSql).map(it => replaceKey(it,'function')).each(it => IoUtils.write(new File(groupPath[it.groupId],it.name + '.ms'),FunctionServiceProvider.serialize(it)));
return 'ok';