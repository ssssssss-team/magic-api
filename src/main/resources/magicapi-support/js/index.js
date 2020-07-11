var MagicEditor = {
    init : function(){
        var skin = this.getValue('skin');
        if(skin){
            $('body').addClass('skin-' + skin);
        }
        this.apiId = null;
        this.apiList = [];
        this.debugSessionId = null;
        this.defaultRequestValue = '{\r\n\t"request" : {\r\n\t\t"message" : "Hello MagicAPI!"\r\n\t},\r\n\t"path" : {\r\n\t\t"id" : "123456"\r\n\t},\r\n\t"header" : {\r\n\t\t"token" : "tokenValue"\r\n\t},\r\n\t"cookie" : {\r\n\t\t"cookieName" : "cookieValue"\r\n\t},\r\n\t"session" : {\r\n\t\t"userId" : "123"\r\n\t}\r\n}';
        this.initMTA();
        this.loadAPI();
        this.initShortKey();
        this.initSkin();
        this.initLeftToobarContainer();
        this.initBottomContainer();
        this.initSelect();
        this.initContextMenu();
        this.initScriptEditor();
        this.resetEditor();
        this.checkUpdate();
    },
    initSkin : function(){
        var skinSelector = $('.skin-selector');
        $('.button-skin').on('click',function(){
            skinSelector.toggle();
            return false;
        });
        var $body = $('body');
        $body.on('click',function(){
            skinSelector.hide();
        })
        var _this = this;
        skinSelector.on('click','li',function(){
            skinSelector.hide();
            $(this).siblings().each(function(){
                $body.removeClass('skin-' + $(this).text())
            })
            _this.setSkin($(this).text());
        })
    },
    resetEditor : function(){
        $('input[name=group]').val('未分组');
        $('input[name=method]').val('GET');
        $('input[name=name]').val('');
        $('input[name=path]').val('');
        this.apiId = null;
        this.scriptEditor&&this.scriptEditor.setValue('return message;');
        this.requestEditor && this.requestEditor.setValue(this.defaultRequestValue);
        this.resultEditor&&this.resultEditor.setValue('');
        this.optionsEditor && this.optionsEditor.setValue('{\r\n}');
    },
    addBreakPoint : function(line){
        var model = this.scriptEditor.getModel();
        model.deltaDecorations([],[{
            range : new monaco.Range(line, 1, line, 1),
            options: {
                isWholeLine: true,
                linesDecorationsClassName: 'breakpoints',
                className : 'breakpoint-line',
            }
        }])
    },
    removeBreakPoint : function(line){
        var model = this.scriptEditor.getModel();
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
    },
    hasBreakPoint : function(line){
        var decorations = this.scriptEditor.getLineDecorations(line);
        for (var i=0,len =decorations.length;i<len;i++) {
            if (decorations[i].options.linesDecorationsClassName === 'breakpoints') {
                return true;
            }
        }
    },
    renderApiList : function(){
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
        var apiList = this.apiList;
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
        var $dom = $('.api-list-container').html('');
        for(var key in groups){
            var $item = $('<div/>').addClass('group-item')
                .addClass('opened')
                .append($('<div/>').addClass('group-header').append('<i class="iconfont icon-arrow-bottom"></i><i class="iconfont icon-list"></i>').append(key));
            var $ul = $('<ul/>').addClass('group-list');
            for(var i =0,len = groups[key].children.length;i<len;i++){
                var info = groups[key].children[i];
                $ul.append($('<li/>').attr('data-id',info.id).append('<i class="iconfont icon-script"></i>')
                    .append('<label>'+info.name+'</label>')
                    .append('<span>('+info.path+')</span>'));
            }
            $item.append($ul);
            $dom.append($item);
        }
    },
    loadAPI : function(id){
        var _this = this;
        if(id){
            this.ajax({
                url : 'get',
                data : {
                    id : id
                },
                success : function(info){
                    _this.resetEditor();
                    _this.apiId = id;
                    $('.button-delete').removeClass('disabled');
                    $('input[name=name]').val(info.name);
                    $('input[name=path]').val(info.path);
                    MagicEditor.setStatusBar('编辑接口：' + info.name + '(' + info.path + ')')
                    $('select[name=method]').val(info.method);
                    $('select[name=group]').val(info.groupName || '未分组');
                    $('.button-run,.button-delete').removeClass('disabled');
                    _this.scriptEditor && _this.scriptEditor.setValue(info.script);
                    _this.requestEditor && _this.requestEditor.setValue(info.parameter || _this.defaultRequestValue);
                    _this.optionsEditor && _this.optionsEditor.setValue(info.option || '{\r\n}');

                }
            })
        }else{
            this.ajax({
                url : 'list',
                success : function(list){
                    _this.apiList = list;
                    _this.renderApiList();
                }
            })
        }
    },
    createNew : function(){
        MagicEditor.createDialog({
            title : '新建接口',
            content : '新建接口会清空当前编辑器，是否继续？',
            buttons : [{
                name : '继续',
                click : function(){
                    $('.group-item .group-list li.selected').removeClass('selected');
                    MagicEditor.resetEditor();
                    $('.button-delete').addClass('disabled');
                    MagicEditor.setStatusBar('创建接口');
                }
            },{
                name : '取消'
            }]
        })
    },
    setStatusBar : function(value){
        $('.footer-container').html(value);
    },
    initMTA : function(){
        window._mtac = {};
        var mta = document.createElement("script");
        mta.src = "//pingjs.qq.com/h5/stats.js?v2.0.4";
        mta.setAttribute("name", "MTAH5");
        mta.setAttribute("sid", "500724136");
        mta.setAttribute("cid", "500724141");
        var s = document.getElementsByTagName("script")[0];
        s.parentNode.insertBefore(mta, s);
        this.report('visit');
    },
    deleteGroup : function($header){
        var groupName = $header.text();
        MagicEditor.createDialog({
            title : '删除接口分组',
            content : '是否要删除接口分组「'+groupName + '」',
            buttons : [{
                name : '删除',
                click : function(){
                    MagicEditor.report('group_delete');
                    var ids = [];
                    $header.next().find('li').each(function(){
                        ids.push($(this).data('id'));
                    });
                    MagicEditor.setStatusBar('准备删除接口分组「'+groupName + '」');
                    MagicEditor.ajax({
                        url : 'group/delete',
                        data : {
                            apiIds : ids.join(','),
                            groupName : groupName
                        },
                        success : function(){
                            MagicEditor.setStatusBar('接口分组「'+groupName + '」已删除');
                            MagicEditor.loadAPI();  //重新加载
                        }
                    })
                }
            },{
                name : '取消'
            }]
        })
    },
    report : function(eventId){
        try{
            MtaH5.clickStat(eventId);
        }catch(ignored){}
    },
    deleteApi : function($li){
        var text = $li.text();
        MagicEditor.createDialog({
            title : '删除接口',
            content : '是否要删除接口「'+text + '」',
            buttons : [{
                name : '删除',
                click : function(){
                    MagicEditor.setStatusBar('准备删除接口');
                    MagicEditor.report('script_delete')
                    var apiId = $li.data('id');
                    MagicEditor.ajax({
                        url : 'delete',
                        data : {
                            id : apiId
                        },
                        success : function(){
                            if(MagicEditor.apiId == apiId){
                                MagicEditor.apiId = null;
                            }
                            MagicEditor.setStatusBar('接口「'+text + '」已删除');
                            MagicEditor.loadAPI();  //重新加载
                        }
                    })
                }
            },{
                name : '取消'
            }]
        })
    },
    ajax : function(options){
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
                }else{
                    var val = options.exception&&options.exception(json.code,json.message,json);
                    if(val !== false){
                        MagicEditor.createDialog({
                            title : 'Error',
                            content : json.message,
                            buttons : [{name : 'OK'}]
                        });
                    }
                }
            },
            error : function(){
                MagicEditor.setStatusBar('ajax请求失败');
                MagicEditor.createDialog({
                    title : '网络错误',
                    content : 'ajax请求失败',
                    buttons : [{
                        name : '关闭'
                    }]
                });
                options.error&&options.error();
            }
        })
    },
    copyApi : function(){
        MagicEditor.createDialog({
            title : '复制接口',
            content : '功能暂未实现！',
            buttons : [{name : '知道了'}]
        })
    },
    copyApiPath : function(){
        MagicEditor.createDialog({
            title : '复制接口路径',
            content : '功能暂未实现！',
            buttons : [{name : '知道了'}]
        })
    },
    resetDebugContent : function(){
        $('.bottom-item-body table tbody').html('<tr><td colspan="3" align="center">no message.</td></tr>');
    },
    doContinue : function(){
        if($('.button-continue').hasClass('disabled')){
            return;
        }
        if(this.debugSessionId){
            MagicEditor.resetDebugContent();
            $('.button-continue').addClass('disabled');
            var _this = this;
            this.ajax({
                url : 'continue',
                data : {
                    id : this.debugSessionId
                },
                success : function(data,json){
                    _this.convertResult(json.code,json.message,json);
                },
                exception : function(code,message,json){
                    return _this.convertResult(code,message,json);
                },
                error : function(){
                    $('.button-run').removeClass('disabled');
                }
            })
        }
    },
    doTest : function(){
        if($('.button-run').hasClass('disabled')){
            return;
        }
        var request = this.requestEditor.getValue();
        try{
            request = JSON.parse(request);
            if(typeof request != 'object'){
                MagicEditor.setStatusBar('请求参数有误！');
                this.createDialog({
                    title : '运行测试',
                    content : '请求参数有误！',
                    buttons : [{
                        name : '确定'
                    }]
                });
                return;
            }
        }catch(e){
            MagicEditor.setStatusBar('请求参数有误！');
            this.createDialog({
                title : '运行测试',
                content : '请求参数有误！',
                buttons : [{
                    name : '确定'
                }]
            });
            return;
        }
        MagicEditor.setStatusBar('开始测试...');
        MagicEditor.report('run');
        request.script = this.scriptEditor.getValue();
        var decorations = this.scriptEditor.getModel().getAllDecorations();
        var breakpoints = [];
        for (var i=0,len =decorations.length;i<len;i++) {
            if (decorations[i].options.linesDecorationsClassName === 'breakpoints') {
                breakpoints.push(decorations[i].range.startLineNumber);
            }
        }
        request.breakpoints = breakpoints;
        this.resetDebugContent();
        $('.button-run').addClass('disabled');
        $('.button-continue').addClass('disabled');
        var _this = this;
        this.ajax({
            url : 'test',
            data : JSON.stringify(request),
            contentType : 'application/json;charset=utf-8',
            success : function(data,json){
                _this.convertResult(json.code,json.message,json);
            },
            exception : function(code,message,json){
                return _this.convertResult(code,message,json);
            },
            error : function(){
                $('.button-run').removeClass('disabled');
            }
        })
    },
    doSave : function(){
        if($('.button-save').hasClass('disabled')){
            return;
        }
        $('.button-save').addClass('disabled');
        var name = $('input[name=name]').val();
        var path = $('input[name=path]').val();
        var method = $('input[name=method]').val();
        var groupName = $('input[name=group]').val();
        this.setStatusBar('准备保存接口：' + name + "(" + path + ")");
        var _this = this;
        this.ajax({
            url : 'save',
            data : {
                script : this.scriptEditor.getValue(),
                path : path,
                method : method,
                id : this.apiId,
                groupName : groupName,
                parameter: this.requestEditor.getValue(),
                option: this.optionsEditor.getValue(),
                name : name
            },
            async : false,
            exception : function(){
                $('.button-save').removeClass('disabled');
            },
            error : function(){
                $('.button-save').removeClass('disabled');
            },
            success : function(id){
                $('.button-save,.button-delete').removeClass('disabled');
                if(_this.apiId){
                    _this.report('script_save');
                    for(var i=0,len = _this.apiList.length;i<len;i++){
                        if(_this.apiList[i].id == _this.apiId){
                            _this.apiList[i].name = name;
                            _this.apiList[i].path = path;
                            _this.apiList[i].method = method;
                            _this.apiList[i].groupName = groupName;
                            break;
                        }
                    }
                }else{
                    _this.report('script_add');
                    _this.apiId = id;
                    _this.apiList.unshift({
                        id : id,
                        name : name,
                        path : path,
                        method : method,
                        groupName : groupName || '未分组',
                    })
                }
                _this.setStatusBar('保存成功！');
                _this.renderApiList();
            }
        })
    },
    convertResult : function(code,message,json){
        this.debugSessionId = null;
        this.resetDebugContent();
        this.debugDecorations&&this.scriptEditor&&this.scriptEditor.deltaDecorations(this.debugDecorations,[]);
        this.debugDecorations = null;
        var _this = this;
        if(code === -1000){
            MagicEditor.setStatusBar('脚本执行出错..');
            MagicEditor.report('script_error');
            $(".button-run").removeClass('disabled');
            $('.button-continue').addClass('disabled');
            this.navigateTo(2);
            if (json.body) {
                var line = json.body;
                var decorations = this.scriptEditor&&this.scriptEditor.deltaDecorations([],[{
                    range: new monaco.Range(line[0], line[2], line[1], line[3] + 1),
                    options : {
                        hoverMessage : {
                            value : message
                        },
                        inlineClassName : 'squiggly-error',
                    }
                }])
                setTimeout(function(){
                    _this.scriptEditor&&_this.scriptEditor.deltaDecorations(decorations,[])
                },10000)
            }
        }else if(code === 1000){ // debug断点
            $(".button-run").addClass('disabled');
            $('.button-continue').removeClass('disabled');
            this.navigateTo(3);
            this.debugIn(message, json.body);
            return false;
        }
        MagicEditor.setStatusBar('脚本执行完毕');
        $(".button-run").removeClass('disabled');
        $('.button-continue').addClass('disabled');
        this.navigateTo(2)
        this.resultEditor.setValue(this.formatJson(json.data))
    },
    debugIn : function(id,data){
        MagicEditor.setStatusBar('进入断点...');
        MagicEditor.report('debug_in');
        this.debugSessionId = id;
        if(data.variables.length > 0){
            var $tbody = $('.bottom-item-body table tbody').html('');
            for(var i =0,len = data.variables.length;i<len;i++){
                var item = data.variables[i];
                var $tr = $('<tr/>');
                $tr.append($('<td/>').html(item.name))
                $tr.append($('<td/>').html(item.value))
                $tr.append($('<td/>').html(item.type))
                $tbody.append($tr);
            }
        }else{
            this.resetDebugContent();
        }
        this.debugDecorations = [this.scriptEditor&&this.scriptEditor.deltaDecorations([],[{
            range :  new monaco.Range(data.range[0],1,data.range[0],1),
            options: {
                isWholeLine: true,
                inlineClassName : 'debug-line',
                className : 'debug-line',
            }
        }])];
    },
    // 初始化快捷键
    initShortKey : function(){
        var _this = this;
        $('body').on('keydown',function(e){
            if(e.keyCode == 119){ //F8
                _this.doContinue();
                e.preventDefault();
            }else if(e.keyCode == 81 && (e.metaKey || e.ctrlKey)){  //Ctrl + Q
                _this.doTest();
                e.preventDefault();
            }else if(e.keyCode == 83 && (e.metaKey || e.ctrlKey)){  //Ctrl + S
                _this.doSave();
                e.preventDefault();
            }else if(e.keyCode == 78 && e.altKey){  //Ctrl + O
                _this.createNew();
                e.preventDefault();
            }
        })
    },
    //检测更新
    checkUpdate : function(){
        var _this = this;
        var ignoreVersion = this.getValue('ignore-version');
        $.ajax({
            url : 'https://img.shields.io/maven-central/v/org.ssssssss/magic-api.json',
            dataType : 'json',
            success : function(data){
                if(data.value != 'v0.2.1' && ignoreVersion != data.value){
                    _this.createDialog({
                        title : '更新提示',
                        content : '检测到已有新版本'+data.value+'，是否更新？',
                        buttons : [{
                            name : '更新日志',
                            click : function(){
                                _this.setValue('ignore-version',data.value)
                                window.open('http://www.ssssssss.org/changelog.html')
                            }
                        },{
                            name : '残忍拒绝',
                            click : function(){
                                _this.setValue('ignore-version',data.value)
                            }
                        }]
                    })
                    MagicEditor.setStatusBar('版本检测完毕，最新版本为：' + data.value+',建议更新！！');
                }else{
                    MagicEditor.setStatusBar('版本检测完毕，当前已是最新版');
                }
            },
            error : function(){
                MagicEditor.setStatusBar('版本检测失败');
            }
        })
    },
    //初始化右键菜单
    initContextMenu : function(){
        var _this = this;
        $('.api-list-container').on('contextmenu','.group-header',function(e){
            _this.createContextMenu([{
                name : '新建接口',
                shortKey : 'Alt+N',
                click : _this.createNew
            },{
                name : '删除组',
                shortKey : '',
                click : _this.deleteGroup
            }],e.pageX,e.pageY,$(this));
            return false;
        }).on('contextmenu','.group-list li',function(e){
            var $li = $(this);
            _this.createContextMenu([{
                name : '复制接口',
                shortKey : 'Ctrl+C',
                click : _this.copyApi,
            },{
                name : '复制路径',
                shortKey : 'Ctrl+Shift+C',
                click : _this.copyApiPath
            },{
                name : '移动',
                shortKey : 'Ctrl+M',
                click : function(){
                    _this.createDialog({
                        title : '移动接口',
                        content : '功能暂未实现！',
                        buttons : [{name : '知道了'}]
                    })
                }
            },{
                name : '删除接口',
                shortKey : '',
                click : _this.deleteApi
            }],e.pageX,e.pageY,$li)
            return false;
        }).on('contextmenu',function(){
            return false;
        })
    },
    initSelect : function(){
        var _this = this;
        $('body').on('click','.select',function(){
            $('.select ul').hide();
            $(this).find('ul').show();
            return false;
        }).on('click','.select ul li',function(){
            var $this = $(this);
            $this.parent().hide().parent().find('input').val($this.text());
            $this.addClass('selected').siblings().removeClass('selected');
            return false;
        }).on('click',function(){
            $('.select ul').hide();
        }).on('click','.api-list-container ul li',function(){
            $('.api-list-container ul li.selected').removeClass('selected');
            _this.loadAPI($(this).addClass('selected').data('id'))
        }).on('click','.button-run',function(){
            _this.doTest();
        }).on('click','.button-delete',function(){
            if($(this).hasClass('disabled')){
                return;
            }
            if(_this.apiId){
                var $li = $('.group-list li[data-id='+_this.apiId+']');
                if($li.length > 0){
                    _this.deleteApi($li);
                }
            }
        }).on('click','.button-save',function(){
            _this.doSave();
        }).on('click','.button-continue',function(){
            _this.doContinue();
        }).on('click','.button-gitee',function(){
            MagicEditor.report('button-gitee');
            window.open('https://gitee.com/ssssssss-team/magic-api');
        }).on('click','.button-github',function(){
            MagicEditor.report('button-github');
            window.open('https://github.com/ssssssss-team/magic-api')
        }).on('click','.button-qq',function(){
            window.open('https://shang.qq.com/wpa/qunwpa?idkey=10faa4cf9743e0aa379a72f2ad12a9e576c81462742143c8f3391b52e8c3ed8d')
        }).on('click','.button-help',function(){
            MagicEditor.report('button-help');
            window.open('https://ssssssss.org')
        });
    },
    getValue : function(key){
        return localStorage&&localStorage.getItem(key) || '';
    },
    setValue : function(key,value){
        if(Array.isArray(value) || typeof value == 'object'){
            value = JSON.stringify(value);
        }
        localStorage&&localStorage.setItem(key,value) || '';
    },
    bindEditorShortKey : function(editor){
        // Alt + / 代码提示
        editor.addCommand(monaco.KeyMod.Alt | monaco.KeyCode.US_SLASH,function(){
            editor.trigger(null, 'editor.action.triggerSuggest', {})
        },'!findWidgetVisible && !inreferenceSearchEditor && !editorHasSelection');
        // Ctrl + Shift + U 转大写
        editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyMod.Shift | monaco.KeyCode.KEY_U,function(){
            editor.trigger(null, 'editor.action.transformToUppercase', {})
        });
        // Ctrl + Shift + X 转小写
        editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyMod.Shift | monaco.KeyCode.KEY_X,function(){
            editor.trigger(null, 'editor.action.transformToLowercase', {})
        });
        // Ctrl + Alt + L 代码格式化
        editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyMod.Alt | monaco.KeyCode.KEY_L,function(){
            editor.trigger(null, 'editor.action.formatDocument', {})
        },'editorHasDocumentFormattingProvider && editorTextFocus && !editorReadonly');
        // Ctrl + Alt + L 选中代码格式化
        editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyMod.Alt | monaco.KeyCode.KEY_L,function(){
            editor.trigger(null, 'editor.action.formatSelection', {})
        },'editorHasDocumentFormattingProvider && editorHasSelection && editorTextFocus && !editorReadonly');


    },
    // 初始化脚本编辑器
    initScriptEditor : function(){
        this.ajax({
            url: 'classes',
            async : false,
            success: function (data) {
                Parser.scriptClass = data || {};
            }
        })

        monaco.editor.defineTheme('default', {
            base: 'vs',
            inherit: true,
            rules: [
                { background: '#ffffff' },
                { token: 'keywords', foreground: '000080',fontStyle : 'bold'},
                { token: 'number', foreground: '0000FF' },
                { token: 'keyword', foreground: '000080',fontStyle : 'bold'},
                { token: 'string.sql', foreground: '000000'},
                { token: 'predefined.sql', foreground: '000000'},
                { token: 'operator.sql', foreground: '000080',fontStyle : 'bold'},
                { token: 'key', foreground: '660E7A' },
                { token: 'string.key.json', foreground: '660E7A' },
                { token: 'string.value.json', foreground: '008000' },
                { token: 'keyword.json', foreground: '0000FF' },
                { token: 'string', foreground: '008000',fontStyle : 'bold' },
                { token: 'string.invalid', foreground: '008000' ,background : 'FFCCCC'},
                { token: 'string.escape.invalid', foreground: '008000' ,background : 'FFCCCC'},
                { token: 'string.escape', foreground: '000080',fontStyle : 'bold'},
                { token: 'comment', foreground: '808080'},
                { token: 'comment.doc', foreground: '808080'},
                { token: 'string.escape', foreground: '000080'}
            ],
            colors: {
                'editor.foreground': '#000000',
                'editor.background': '#ffffff',
//		        'editor.lineHighlightBorder' : '#00000000',
                'editorLineNumber.foreground': '#999999',	//行号的颜色
                'editorGutter.background' : '#f0f0f0',	//行号背景色
                'editor.lineHighlightBackground' : '#FFFAE3',	//光标所在行的颜色
                'dropdown.background' : '#F2F2F2',	//右键菜单
                'dropdown.foreground' : '#000000',	//右键菜单文字颜色
                'list.activeSelectionBackground': '#1A7DC4',	//右键菜单悬浮背景色
                'list.activeSelectionForeground' : '#ffffff',	//右键菜单悬浮文字颜色
            }
        });

        monaco.editor.defineTheme('dark', {
            base: 'vs-dark',
            inherit: true,
            rules: [
                { foreground: 'A9B7C6' },
                { token: 'keywords', foreground: 'CC7832',fontStyle : 'bold'},
                { token: 'keyword', foreground: 'CC7832',fontStyle : 'bold'},
                { token: 'number', foreground: '6897BB' },
                { token: 'string', foreground: '6A8759',fontStyle : 'bold' },
                { token: 'string.sql', foreground: 'A9B7C6'},
                { token: 'predefined.sql', foreground: 'A9B7C6'},
                { token: 'key', foreground: '9876AA' },
                { token: 'string.key.json', foreground: '9876AA' },
                { token: 'string.value.json', foreground: '6A8759' },
                { token: 'keyword.json', foreground: '6897BB' },
                { token: 'operator.sql', foreground: 'CC7832',fontStyle : 'bold'},
                { token: 'string.invalid', foreground: '008000' ,background : 'FFCCCC'},
                { token: 'string.escape.invalid', foreground: '008000' ,background : 'FFCCCC'},
                { token: 'string.escape', foreground: '000080',fontStyle : 'bold'},
                { token: 'comment', foreground: '808080'},
                { token: 'comment.doc', foreground: '629755'},
                { token: 'string.escape', foreground: 'CC7832'}
            ],
            colors: {
                'editor.background': '#2B2B2B',
//		        'editor.lineHighlightBorder' : '#00000000',
                'editorLineNumber.foreground': '#999999',	//行号的颜色
                'editorGutter.background' : '#313335',	//行号背景色
                'editor.lineHighlightBackground' : '#323232',	//光标所在行的颜色
                'dropdown.background' : '#3C3F41',	//右键菜单
                'dropdown.foreground' : '#BBBBBB',	//右键菜单文字颜色
                'list.activeSelectionBackground': '#4B6EAF',	//右键菜单悬浮背景色
                'list.activeSelectionForeground' : '#FFFFFF',	//右键菜单悬浮文字颜色
            }
        });
        var theme = this.getValue('skin') || 'default';
        this.report('theme_' + theme);
        this.scriptEditor = monaco.editor.create($('.editor-container')[0], {
            minimap : {
                enabled : false
            },
            language: 'magicscript',
            folding : false,
            lineDecorationsWidth : 35,
            fixedOverflowWidgets :false,
            theme : theme
        })
        this.requestEditor = monaco.editor.create($('.request-editor')[0], {
            value: "{}",
            minimap : {
                enabled : false
            },
            language: 'json',
            folding : false,
            fixedOverflowWidgets :true,
            theme : theme
        })
        this.optionsEditor = monaco.editor.create($('.options-editor')[0], {
            value: "{}",
            minimap : {
                enabled : false
            },
            language: 'json',
            folding : false,
            fixedOverflowWidgets :true,
            theme : theme
        })
        this.resultEditor = monaco.editor.create($('.result-editor')[0], {
            value: "{}",
            minimap : {
                enabled : false
            },
            language: 'json',
            folding : false,
            readOnly : true,
            fixedOverflowWidgets : true,
            theme : theme
        })
        var _this = this;
        this.scriptEditor.onMouseDown(function(e){
            if (e.target.detail && e.target.detail.offsetX && e.target.detail.offsetX >= 0 && e.target.detail.offsetX <= 60) {
                var line = e.target.position.lineNumber;
                if (_this.scriptEditor.getModel().getLineContent(line).trim() === '') {
                    return
                }
                if(_this.hasBreakPoint(line)){
                    _this.removeBreakPoint(line);
                }else{
                    _this.addBreakPoint(line);
                }
            }
        });
        this.bindEditorShortKey(this.scriptEditor);
        this.bindEditorShortKey(this.requestEditor);
        this.bindEditorShortKey(this.optionsEditor);
    },
    navigateTo : function(index){
        var $parent = $('.bottom-container');
        var $dom = $parent.find('.bottom-content-container').show();
        $parent.find('.bottom-tab li').eq(index).addClass('selected').siblings().removeClass('selected');
        $dom.find('.bottom-content-item').eq(index).show().siblings('.bottom-content-item').hide();
        this.layout();
    },
    createDialog : function(options){
        options = options || {};
        var $dialog = $('<div/>').addClass('dialog');
        var $header = $('<div/>').addClass('dialog-header').addClass('not-select').append(options.title || '');
        var $close = $('<span/>').append('<i class="iconfont icon-close"></i>');
        $header.append($close);
        $close.on('click',function(){
            options.close&&options.close();
            $wrapper.remove();
        })
        $dialog.append($header);
        $dialog.append('<div class="dialog-content">' + options.content.replace(/\n/g,'<br>').replace(/ /g,'&nbsp;').replace(/\t/g,'&nbsp;&nbsp;&nbsp;&nbsp;') + '</div>');
        var buttons = options.buttons || [];
        var $buttons = $('<div/>').addClass('dialog-buttons').addClass('not-select');
        if(buttons.length > 1){
            $buttons.addClass('button-align-right');
        }
        for(var i=0,len = buttons.length;i<len;i++){
            var button = buttons[i];
            $buttons.append($('<button/>').html(button.name || '').addClass(button.className || '').addClass(i == 0 ? 'active' : ''));
        }
        $dialog.append($buttons);
        var $wrapper = $('<div/>').addClass('dialog-wrapper').append($dialog);
        $buttons.on('click','button',function(){
            var index = $(this).index();
            if(buttons[index].click&&buttons[index].click() === false){
                return;
            }
            options.close&&options.close();
            $wrapper.remove();
        })
        $('body').append($wrapper);
    },
    createContextMenu : function(menus,left,top,$dom){
        $('.context-menu').remove();
        var $ul = $('<ul/>').addClass('context-menu').addClass('not-select');
        for(var i=0,len = menus.length;i<len;i++){
            var menu = menus[i];
            $ul.append($('<li/>').append('<label>'+menu.name+'</label>').append('<span>'+(menu.shortKey || '')+'<span>'));
        }
        $ul.on('click','li',function(){
            var menu = menus[$(this).index()]
            menu&&menu.click&&menu.click($dom);
        });
        $ul.css({
            left : left + 'px',
            top : top + 'px'
        })
        $('body').append($ul).on('click',function(){
            $ul.remove();
        });
    },
    // 初始化左侧工具条
    initLeftToobarContainer : function(){
        var $apiContainr = $('.api-list-container');
        var value = this.getValue('left-toolbar-width');
        if(value && !isNaN(Number(value))){
            $apiContainr.width(value);
        }
        if('false' == this.getValue('left-toolbar-show')){
            $('.left-toolbar-container li').removeClass('selected');
            $apiContainr.hide();
        }
        var _this = this;
        $('.left-toolbar-container').on('click','li',function(){
            var $this = $(this);
            if($this.hasClass('selected')){	//当前是选中状态
                $this.removeClass('selected');
                _this.setValue('left-toolbar-show',false);
                $apiContainr.hide();
            }else{
                $this.addClass('selected');
                _this.setValue('left-toolbar-show',true);
                $apiContainr.show();
            }
            _this.layout();
        })
        var $middleContainer = $('.middle-container');
        // 调整宽度
        var resizer = $middleContainer.find('.resizer-x')[0];
        resizer.onmousedown = function(){
            var box = $apiContainr[0].getClientRects()[0];
            document.onmousemove = function(e){
                var move = e.clientX - 22;
                if(move > 150 && move < 700){
                    _this.layout();
                    _this.setValue('left-toolbar-width',move);
                    $apiContainr.width(move);
                }
            }
            document.onmouseup = function(evt){
                document.onmousemove = null;
                document.onmouseup = null;
                resizer.releaseCapture && resizer.releaseCapture();
            }
            resizer.setCapture && resizer.setCapture();
        }

        $('body').on('click','.group-header',function(){
            var $parent = $(this).parent();
            if($parent.hasClass('opened')){
                $parent.removeClass('opened');
                $(this).find('.icon-arrow-bottom').removeClass('icon-arrow-bottom').addClass('icon-arrow-right');
            }else{
                $parent.addClass('opened');
                $(this).find('.icon-arrow-right').removeClass('icon-arrow-right').addClass('icon-arrow-bottom');
            }
        })
    },
    formatJson : function (val, defaultVal) {
        return (val ? JSON.stringify(val, null, 4) : defaultVal) || '';
    },
    // 初始化底部
    initBottomContainer : function(){
        var $contentContainer = $('.bottom-container .bottom-content-container');
        var value = this.getValue('bottom-container-height');
        if(value && !isNaN(Number(value))){
            $contentContainer.height(value);
        }
        if('false' == this.getValue('bottom-tab-show')){
            $contentContainer.hide();	//隐藏全部
            $('.bottom-container .bottom-tab li').removeClass('selected');
        }else{
            var index = Number(this.getValue('bottom-tab-index'));
            if(!isNaN(index)){
                this.navigateTo(index);
            }
        }
        var _this = this;
        $('.bottom-container').on('click','.bottom-tab li',function(){
            var $this = $(this);
            if($this.hasClass('selected')){	//当前是选中状态
                $contentContainer.hide();	//隐藏全部
                $this.removeClass('selected')
                _this.setValue('bottom-tab-show',true);
            }else{
                $this.addClass('selected').siblings().removeClass('selected');	//选中选择项，取消其他选择项
                var index = $(this).index();
                _this.setValue('bottom-tab-index',index);
                _this.setValue('bottom-tab-show',true);
                $contentContainer.show().find('.bottom-content-item').hide().eq(index).show();
            }
            _this.layout();
        }).on('click','.button-minimize',function(){
            _this.setValue('bottom-tab-show',false);
            $contentContainer.hide();	//隐藏全部
            $('.bottom-tab li').removeClass('selected')
        });
        // 调整底部高度
        var resizer = $contentContainer.find('.resizer-y')[0];
        resizer.onmousedown = function(){
            var box = $contentContainer[0].getClientRects()[0];
            document.onmousemove = function(e){
                if(e.clientY > 150){
                    var move = box.height - (e.clientY - box.y);
                    if(move > 30){
                        _this.setValue('bottom-container-height',move);
                        _this.layout();
                        $contentContainer.height(move);
                    }
                }
            }
            document.onmouseup = function(evt){
                document.onmousemove = null;
                document.onmouseup = null;
                resizer.releaseCapture && resizer.releaseCapture();
            }
            resizer.setCapture && resizer.setCapture();
        }
        $('.bottom-container').on('click','.bottom-content-item:eq(0) .button-clear',function(){
            _this.requestEditor&&_this.requestEditor.setValue('{}');
        }).on('click','.bottom-content-item:eq(0) .button-format',function(){
            try{
                _this.requestEditor.setValue(_this.formatJson(JSON.parse(_this.requestEditor.getValue()),'{\r\n}'));
            }catch(e){}
        }).on('click','.bottom-content-item:eq(1) .button-clear',function(){
            _this.optionsEditor&&_this.optionsEditor.setValue('{}');
        }).on('click','.bottom-content-item:eq(1) .button-format',function(){
            try{
                _this.optionsEditor.setValue(_this.formatJson(JSON.parse(_this.optionsEditor.getValue()),'{\r\n}'));
            }catch(e){}
        }).on('click','.bottom-content-item:eq(2) .button-clear',function(){
            _this.resultEditor&&_this.resultEditor.setValue('{}');
        }).on('click','.bottom-content-item:eq(2) .button-format',function(){
            try{
                _this.resultEditor.setValue(_this.formatJson(JSON.parse(_this.resultEditor.getValue()),'{\r\n}'));
            }catch(e){}
        })
    },
    setSkin : function(skin){
        $('body').addClass('skin-' + skin);
        this.setValue('skin',skin);
        monaco.editor.setTheme(skin);
        MagicEditor.report('theme_' + skin);
        MagicEditor.setStatusBar('切换皮肤至：' + skin);
        //this.scriptEditor&&this.scriptEditor.setTheme(skin);
    },
    layout : function(){
        this.scriptEditor&&this.scriptEditor.layout();
        this.optionsEditor&&this.optionsEditor.layout();
        this.requestEditor&&this.requestEditor.layout();
        this.resultEditor&&this.resultEditor.layout();
    }
}
$(function(){
    require(['vs/editor/editor.main'],function(){
        MagicEditor.init();
        $('.loading-wrapper').remove();
    })
    $(window).resize(function(){
        MagicEditor.layout();
    });

});
