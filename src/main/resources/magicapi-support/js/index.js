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
    var editorLayout = function(){
        editor&&editor.layout();
        requestEditor&&requestEditor.layout();
        outputEditor&&outputEditor.layout();
    }
    var resetEditor = function(){
        editor&&editor.setValue('return message;');
        requestEditor&&requestEditor.setValue('{\r\n\t"message" : "Hello MagicAPI!"\r\n}');
        outputEditor&&outputEditor.setValue('');
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

    var $tbody = $('#debug-tbody');
    var debugDecorations;
    var debugIn = function(id,data){
        debugSessionId = id;
        for(var i =0,len = data.variables.length;i<len;i++){
            var item = data.variables[i];
            var $tr = $('<tr/>');
            $tr.append($('<td/>').html(item.name))
            $tr.append($('<td/>').html(JSON.stringify(item.value)))
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
        outputEditor.setValue(JSON.stringify(json,null,4))
    }
    // 窗口改变大小时，刷新编辑器
    $(window).resize(editorLayout);
    // 渲染接口列表
    var renderApiList = function(emptyString){
        var $ul = $(".layui-left .api-list").html('');
        var empty = true;
        if(apiList&&apiList.length > 0){
            for(var i=0,len = apiList.length;i<len;i++){
                var info = apiList[i];
                if(info.show!==false){
                    var $li = $('<li/>').attr('data-id',info.id);
                    $li.append($('<label/>').html(info.name));
                    $li.append('(' + info.path + ')');
                    $ul.append($li);
                    empty = false;
                }
            }
        }
        if(empty){
            $ul.html($('<li/>').addClass('empty').html(emptyString));
        }
    }
    var resizeX = $(".layout-resizer-x")[0];
    resizeX.onmousedown = function(e){
        var box = $("body")[0];
        document.onmousemove = function(e){
            var move = (e.clientX / box.clientWidth);
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
    }).on('blur','#request-parameter',function(){
        var value = this.value;
    }).on('click','.api-list li[data-id]',function(){
        var id = $(this).data('id');
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
                editor.setValue(info.script);

            }
        })
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
        _ajax({
            url : 'save',
            data : {
                script : editor.getValue(),
                path : path,
                method : method,
                id : apiId,
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
        if(value){
            renderApiList('搜不到相关API...')
        }else{
            renderApiList('您还没有创建接口..');
        }
    })
    _ajax({
        url : 'list',
        success : function(list){
            apiList = list;
            renderApiList('您还没有创建接口..');
        }
    })
});