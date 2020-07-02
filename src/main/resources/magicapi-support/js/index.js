var $ = layui.$;
var apiList = [];
var apiId;
var debugSessionId;
var _ajax = function(options){
    $.ajax({
        url : options.url,
        async : options.async,
        type : 'post',
        dataType : 'json',
        contentType : options.contentType,
        data : options.data,
        success : function(json){
            if(json.code == 1){
                options&&options.success(json.data,json);
            }else if(options.exception){
                options.exception(json.code,json.message,json);
            }else{
                layui.layer.alert(json.message);
            }
        },
        error : function(){
            layui.layer.alert('ajax请求失败');
        }
    })
}
$(function(){
    layui.form.render();
    var editor;
    var requestEditor;
    var outputEditor;
    var optionEditor;
    var editorLayout = function(){
        editor&&editor.layout();
        requestEditor&&requestEditor.layout();
        optionEditor && optionEditor.layout();
        outputEditor&&outputEditor.layout();
    }
    var defaultRequestValue = '{\r\n\t"request" : {\r\n\t\t"message" : "Hello MagicAPI!"\r\n\t},\r\n\t"path" : {\r\n\t\t"id" : "123456"\r\n\t},\r\n\t"header" : {\r\n\t\t"token" : "tokenValue"\r\n\t},\r\n\t"cookie" : {\r\n\t\t"cookieName" : "cookieValue"\r\n\t},\r\n\t"session" : {\r\n\t\t"userId" : "123"\r\n\t}\r\n}';
    var resetEditor = function(){
        resetGroup('未分组')
        editor&&editor.setValue('return message;');
        requestEditor && requestEditor.setValue(defaultRequestValue);
        outputEditor&&outputEditor.setValue('');
        optionEditor && optionEditor.setValue('{\r\n}');
    }
    var addBreakPoint = function(line){
        if(editor){
            var model = editor.getModel();
            model.deltaDecorations([],[{
                range : new monaco.Range(line, 1, line, 1),
                options: {
                    isWholeLine: true,
                    linesDecorationsClassName: 'breakpoints',
                    className : 'breakpoint-line',
                }
            }])
        }
    }
    var removeBreakPoint = function(line){
        if(editor) {
            var model = editor.getModel();
            var decorations = [];
            if (line !== undefined) {
                decorations = model.getLineDecorations(line);
            } else {
                decorations = model.getAllDecorations();
            }
            var ids = [];
            for (var i=0,len =decorations.length;i<len;i++) {
                if (decorations[i].options.linesDecorationsClassName === 'breakpoints') {
                    ids.push(decorations[i].id)
                }
            }
            model.deltaDecorations(ids, [])
        }
    }
    var hasBreakPoint = function(line){
        var decorations = editor.getLineDecorations(line);
        for (var i=0,len =decorations.length;i<len;i++) {
            if (decorations[i].options.linesDecorationsClassName === 'breakpoints') {
                return true;
            }
        }
    }

    var loadAPI = function () {
        _ajax({
            url : 'list',
            success : function(list){
                apiList = list;
                renderApiList();
            }
        })
    }
    var deleteAPIGroup = function(groupName,ids){
        _ajax({
            url : 'group/delete',
            data : {
                apiIds : ids.join(','),
                groupName : groupName
            },
            success : function(){
                loadAPI();
            }
        })
    }

    var deleteAPI = function(id){
        _ajax({
            url : 'delete',
            data : {
                id : id
            },
            success : function(){
                loadAPI();
            }
        })
    }

    var resetGroup = function(selValue){
        var $dom = $("select[name='group']").next().find("input");
        $dom.unbind("blur");
        if(!$dom.attr('bind-event')){
            $dom.attr('bind-event','true');
            $dom.css({
                "cursor" : "default"
            })
            $dom.focus(function () {
                window.setTimeout(function(){
                    var sourceSelect =  $dom.next();
                    var selectText = $(sourceSelect).find(".layui-anim-upbit .layui-this");
                    var selectVal = $(sourceSelect).find("input").val();
                    if (selectVal !== selectText.text()) {
                        $(selectText).removeClass("layui-this");
                    }
                },100)
            })
        }
        if(selValue !== undefined){
            $dom.val(selValue);
        }
        return $dom;
    }
    _ajax({
        url: 'classes',
        success: function (data) {
            Parser.scriptClass = data || {};
        }
    })
    resetGroup();
    // 初始化编辑器
    require(['vs/editor/editor.main'], function() {
        requestEditor = monaco.editor.create(document.getElementById('request-parameter'), {
            minimap : {
                enabled : false
            },
            language: 'json',
            folding:false,
            fixedOverflowWidgets :true,
            theme : 'json'
        });
        optionEditor = monaco.editor.create(document.getElementById('option-parameter'), {
            minimap: {
                enabled: false
            },
            language: 'json',
            folding: false,
            fixedOverflowWidgets: true,
            theme: 'json'
        });
        outputEditor = monaco.editor.create(document.getElementById('output-result'), {
            minimap : {
                enabled : false
            },
            language: 'json',
            folding:false,
            readOnly : true,
            fixedOverflowWidgets :true,
            theme : 'json'
        });
        editor = monaco.editor.create(document.getElementById('editor'), {
            minimap : {
                enabled : false
            },
            language: 'magicscript',
            fixedOverflowWidgets :true,
            theme : 'magicscript'
        });
        resetEditor();
        editor.onMouseDown(function(e){
            if (e.target.detail && e.target.detail.offsetX && e.target.detail.offsetX >= 0 && e.target.detail.offsetX <= 60) {
                var line = e.target.position.lineNumber;
                if (editor.getModel().getLineContent(line).trim() === '') {
                    return
                }
                if(hasBreakPoint(line)){
                    removeBreakPoint(line);
                }else{
                    addBreakPoint(line);
                }
            }
        });
    });

    var formatJson = function (val, defaultVal) {
        return (val ? JSON.stringify(val, null, 4) : defaultVal) || '';
    }

    var $tbody = $('#debug-tbody');
    var debugDecorations;
    var debugIn = function(id,data){
        debugSessionId = id;
        for(var i =0,len = data.variables.length;i<len;i++){
            var item = data.variables[i];
            var $tr = $('<tr/>');
            $tr.append($('<td/>').html(item.name))
            $tr.append($('<td/>').html(item.value))
            $tr.append($('<td/>').html(item.type))
            $tbody.append($tr);
        }
        debugDecorations = [editor&&editor.deltaDecorations([],[{
            range :  new monaco.Range(data.range[0],1,data.range[0],1),
            options: {
                isWholeLine: true,
                inlineClassName : 'debug-line',
                className : 'debug-line',
            }
        }])];
    }

    var convertResult = function(code,message,json){
        debugSessionId = null;
        $tbody.html('');
        debugDecorations&&editor&&editor.deltaDecorations(debugDecorations,[]);
        debugDecorations = null;
        if(code === -1000){
            layui.element.tabChange('output-container', 'output');
            if(json.data){
                var data = json.data;
                delete json.data;
                var decorations = editor&&editor.deltaDecorations([],[{
                    range :  new monaco.Range(data[0],data[2],data[1],data[3] + 1),
                    options : {
                        hoverMessage : {
                            value : message
                        },
                        inlineClassName : 'squiggly-error',
                    }
                }])
                setTimeout(function(){
                    editor&&editor.deltaDecorations(decorations,[])
                },10000)
            }
        }else if(code === 1000){ // debug断点
            layui.element.tabChange('output-container', 'debug');
            debugIn(message,json.data);
            return;
        }
        layui.element.tabChange('output-container', 'output');
        outputEditor.setValue(formatJson(json))
    }
    // 窗口改变大小时，刷新编辑器
    $(window).resize(editorLayout);
    var $tree;
    // 渲染接口列表
    var renderApiList = function(){
        var $ul = $(".layui-left .api-list").html('');
        var empty = true;
        var root = [];
        var groups = {
            "未分组" : {
                id : '未分组',
                children : [],
                spread : true,
                title : '未分组'
            }
        };
        if(apiList&&apiList.length > 0){
            for(var i=0,len = apiList.length;i<len;i++){
                var info = apiList[i];
                info.groupName = info.groupName || '未分组';
                if(!groups[info.groupName]){
                    groups[info.groupName] = {
                        id : info.groupName,
                        children : [],
                        spread : true,
                        title : info.groupName
                    }
                }
                if(info.show!==false){
                    groups[info.groupName].children.push({
                        id : info.id,
                        groupName : info.groupName,
                        name : info.name,
                        title : '<label style="padding-right: 4px;color:#000">' + info.name + "</label>" + info.path,
                        path : info.path
                    });
                }
            }
        }
        for(var key in groups){
            root.push(groups[key]);
        }
        if($tree){
            layui.tree.reload('api-list',{
                data : root
            })
        }else{
            $tree = layui.tree.render({
                elem : '#api-list',
                id : 'api-list',
                data : root,
                edit : ['del'],
                onlyIconControl : true,
                operate : function(obj){
                    var data = obj.data;
                    if(data.children){
                        var ids = [];
                        for(var i=0,len=data.children.length;i<len;i++){
                            ids.push(data.children[i].id);
                            if(data.children[i].id == apiId){
                                apiId = null;
                                resetEditor();
                            }
                        }
                        deleteAPIGroup(data.id,ids);
                    }else{
                        deleteAPI(data.id)
                    }
                    if(data.id == apiId){
                        apiId = null;
                        resetEditor();
                    }
                },
                click : function(obj){
                    if(!obj.data.children){
                        var id = obj.data.id;
                        _ajax({
                            url : 'get',
                            data : {
                                id : id
                            },
                            success : function(info){
                                apiId = id;
                                $('input[name=name]').val(info.name);
                                $('input[name=path]').val(info.path);
                                $('select[name=method]').val(info.method);
                                layui.form.render();
                                resetEditor();
                                resetGroup().val(info.groupName || '未分组');
                                editor && editor.setValue(info.script);
                                requestEditor && requestEditor.setValue(info.parameter || defaultRequestValue);
                                optionEditor && optionEditor.setValue(info.option || '{\r\n}');

                            }
                        })
                    }
                }
            })
        }
    }
    var resizeX = $(".layout-resizer-x")[0];
    resizeX.onmousedown = function(e){
        var box = $(".layui-right-container")[0];
        var body = $('body')[0];
        var mx = body.clientWidth - box.clientWidth;
        document.onmousemove = function(e){
            var move = ((e.clientX - mx) / box.clientWidth);
            if((1 - move) * box.clientWidth < 300 || move * box.clientWidth  < 500){
                return;
            }
            move = (1 - move) * 100;
            $(".editor-container").css('right',move + '%');
            $(".layui-right").css('width',move + '%');
            resizeX.style.right = move + '%';
            editorLayout();
        }
        document.onmouseup = function(evt){
            document.onmousemove = null;
            document.onmouseup = null;
            resizeX.releaseCapture && resizeX.releaseCapture();
        }
        resizeX.setCapture && resizeX.setCapture();
        return false;
    }
    var resizeY = $(".layout-resizer-y")[0];
    resizeY.onmousedown = function(e){
        var box = $(".layui-right")[0].getClientRects()[0];
        document.onmousemove = function(e){
            var move = ((e.clientY - box.y) / box.height);
            if((1 - move) * box.height < 150 || move * box.height  < 150){
                return;
            }
            move = move * 100;
            $(".output-container").css('top',move + '%');
            $(".request-container").css('height',move + '%');
            resizeY.style.top = move + '%';
            editorLayout();
        }
        document.onmouseup = function(evt){
            document.onmousemove = null;
            document.onmouseup = null;
            resizeY.releaseCapture && resizeY.releaseCapture();
        }
        resizeY.setCapture && resizeY.setCapture();
        return false;
    }
    layui.element.on('tab', function () {
        editorLayout();
    });
    $('body').on('keydown',function(e){
        if(e.keyCode == 119){ //F8
            if(debugSessionId){
                _ajax({
                    url : 'continue',
                    data : {
                        id : debugSessionId
                    },
                    success : function(data,json){
                        convertResult(json.code,json.message,json);
                    },
                    exception : function(code,message,json){
                        convertResult(code,message,json);
                    }
                })
            }
            e.preventDefault();
        }
    }).on('click','.btn-create',function(){
        layui.layer.confirm('新建接口会清空当前编辑器，是否继续？',{
            title : "创建接口"
        },function(index){
            layui.layer.close(index);
            $('input[name=name]').val('');
            $('input[name=path]').val('');
            $('select[name=method]').val('GET');
            layui.form.render();
            apiId = null;
            resetEditor();
        })
    }).on('click','.btn-test',function(){
        var request = requestEditor.getValue();
        try{
            request = JSON.parse(request);
            if(typeof request != 'object'){
                layui.layer.alert('请求参数有误！');
                return;
            }
        }catch(e){
            layui.layer.alert('请求参数有误！');
            return;
        }
        request.script = editor.getValue();
        var decorations = editor.getModel().getAllDecorations();
        var breakpoints = [];
        for (var i=0,len =decorations.length;i<len;i++) {
            if (decorations[i].options.linesDecorationsClassName === 'breakpoints') {
                breakpoints.push(decorations[i].range.startLineNumber);
            }
        }
        request.breakpoints = breakpoints;
        _ajax({
            url : 'test',
            data : JSON.stringify(request),
            contentType : 'application/json;charset=utf-8',
            success : function(data,json){
                convertResult(json.code,json.message,json);
            },
            exception : function(code,message,json){
                convertResult(code,message,json);
            }
        })
    }).on('click','.btn-save',function(){
        var name = $('input[name=name]').val();
        var path = $('input[name=path]').val();
        var method = $('select[name=method]').val();
        var groupName = resetGroup().val() || '未分组';
        _ajax({
            url : 'save',
            data : {
                script : editor.getValue(),
                path : path,
                method : method,
                id : apiId,
                groupName : groupName,
                parameter: requestEditor.getValue(),
                option: optionEditor.getValue(),
                name : name
            },
            async : false,
            success : function(id){
                if(apiId){
                    for(var i=0,len = apiList.length;i<len;i++){
                        if(apiList[i].id == apiId){
                            apiList[i].name = name;
                            apiList[i].path = path;
                            apiList[i].method = method;
                            apiList[i].groupName = groupName;
                            break;
                        }
                    }
                }else{
                    apiId = id;
                    apiList.unshift({
                        id : id,
                        name : name,
                        path : path,
                        method : method,
                        groupName : groupName || '未分组',
                        show : true
                    })
                }
                renderApiList();
                layui.layer.msg('保存成功');
            }
        })
    }).on('keyup','.layui-left .api-search input',function(){
        var value = this.value;
        for(var i=0,len = apiList.length;i<len;i++){
            var info = apiList[i];
            info.show = value ? info.path.indexOf(value) > -1 || info.name.indexOf(value) > -1 : true;
        }
        renderApiList()
    })
    loadAPI();
});