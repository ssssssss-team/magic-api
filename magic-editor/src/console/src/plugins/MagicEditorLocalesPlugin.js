'use strict'

var webpackVersion = require('webpack/package.json').version;
var local = null;

function compilerHook(compilation) {
    if (webpackVersion < '4') {
        compilation.plugin('succeed-module', compilationHook);
    } else {
        compilation.hooks.succeedModule.tap('MonacoEditorLocalesPlugin', compilationHook);
    }
}

function compilationHook(wpModule) {
    if (!wpModule.resource || !wpModule.resource.indexOf || wpModule.resource.replace(/\\+/g, "/").indexOf("esm/vs/nls.js") < 0) {
        return;
    }

    var langStr = local.getSelectLangStr();

    var endl = "\r\n";
    var code = wpModule._source._value;
    code = code.replace("export function localize", "       function _ocalize");

    code += endl + "function localize(data, message) {";
    code += endl + "	if(typeof(message) === 'string'){";
    code += endl + "		var idx = localize.mapLangIdx[message] || -1;";
    code += endl + "		var nlsLang = localize.mapNlsLang[localize.selectLang] || {};";
    code += endl + "";
    code += endl + "		if(idx in nlsLang){";
    code += endl + "			message = nlsLang[idx];";
    code += endl + "		}";
    if (local.options.logUnmatched) {
        code += endl + "		else{";
        code += endl + "			console.info('unknown lang:' + message);";
        code += endl + "		}";
    }
    code += endl + "	}";
    code += endl + "";
    code += endl + "	var args = [];";
    code += endl + "	for(var i = 0; i < arguments.length; ++i){";
    code += endl + "		args.push(arguments[i]);";
    code += endl + "	}";
    code += endl + "	args[1] = message;";
    code += endl + "	return _ocalize.apply(this, args);";
    code += endl + "}";
    code += endl + "localize.selectLang = " + local.getSelectLangStr() + ";";
    if (langStr.indexOf('(') >= 0) {
        code += endl + "try{ localize.selectLang = eval(localize.selectLang); }catch(ex){}";
    }
    code += endl + "localize.mapLangIdx = " + JSON.stringify(local.mapLangIdx) + ";";
    code += endl + "localize.mapNlsLang = " + JSON.stringify(local.lang) + ";";
    // code += endl + "var mapSelfLang = " + JSON.stringify(local.options.mapLanguages) + ";";
    code += endl + "";

    // wpModule._source = new OriginalSource(code, wpModule.identifier());
    wpModule._source._value = code;
}

// const { OriginalSource } = require("webpack-sources");
function MonacoEditorLocalesPlugin(options) {
    this.options = {
        /**
         * support languages list, .eg ["de"]
         * embed language base on monaco-editor@0.14.6
         * all available embed languages: de,es,fr,it,ja,ko,ru,zh-cn,zh-tw
         * just add what you need to reduce the size
         */
        languages: options.languages || [],
        /**
         * default language name, .eg "de"
         * use function string to set dynamic, .eg "$.cookie('language')"
         */
        defaultLanguage: options.defaultLanguage || options.languages[0] || "",
        /**
         * log on console if unmatched
         */
        logUnmatched: options.logUnmatched || false,
        /**
         * self languages map, .eg {"zh-cn": {"Find": "查找", "Search": "搜索"}, "de":{}, ... }
         */
        mapLanguages: options.mapLanguages || {},
    };

    this.langIdx = 0;
    this.mapLangIdx = {};
    this.lang = {};
    this.mapEmbedLangName = {};
    this.mapEmbedLangNameSelf = {};

    this.init();
    this.initLang();
}

module.exports = MonacoEditorLocalesPlugin;

MonacoEditorLocalesPlugin.prototype.apply = function (compiler) {
    local = this;

    if (webpackVersion < '4') {
        compiler.plugin("compilation", compilerHook);
    } else {
        compiler.hooks.compilation.tap('MonacoEditorLocalesPlugin', compilerHook);
    }
}

MonacoEditorLocalesPlugin.prototype.initLang = function () {
    var arr = this.options.languages;

    for (var i = 0; i < arr.length; ++i) {
        if (!(arr[i] in this.mapEmbedLangName)) {
            return;
        }

        var obj = this.mapEmbedLangName[arr[i]];
        if (!obj) {
            continue;
        }

        var rstObj = this.lang[arr[i]] || (this.lang[arr[i]] = {});
        this.initOneLang(rstObj, this.mapEmbedLangName["en"], obj);


        var objSelf = this.mapEmbedLangNameSelf[arr[i]];
        if (!objSelf) {
            continue;
        }
        this.initSelfOneLang(rstObj, objSelf);

        objSelf = this.options.mapLanguages[arr[i]];
        if (!objSelf) {
            continue;
        }
        this.initSelfOneLang(rstObj, objSelf);
    }
}

MonacoEditorLocalesPlugin.prototype.initOneLang = function (rstObj, en, langObj) {
    for (var key in en) {
        if (en && langObj && typeof (en[key]) === "string" && typeof (langObj[key]) === "string") {
            var idx = this.getLangIdx(en[key]);
            rstObj[idx] = langObj[key];
        } else if (en && langObj && en[key] && langObj[key] && typeof (en[key]) === "object" && typeof (langObj[key]) === "object") {
            this.initOneLang(rstObj, en[key], langObj[key]);
        }
    }
}

MonacoEditorLocalesPlugin.prototype.initSelfOneLang = function (rstObj, obj) {
    for (var key in obj) {
        var idx = this.getLangIdx(key);
        rstObj[idx] = obj[key];
    }
}

MonacoEditorLocalesPlugin.prototype.getSelectLangStr = function () {
    var str = this.options.defaultLanguage;
    str = str.replace(/'/g, "\\'");
    str = str.replace(/\r\n/g, "\\r\\n");
    str = str.replace(/\n/g, "\\n");
    return "'" + str + "'";
}

MonacoEditorLocalesPlugin.prototype.getLangIdx = function (en) {
    if (en in this.mapLangIdx) {
        return this.mapLangIdx[en];
    }

    var idx = this.langIdx;
    this.mapLangIdx[en] = idx;
    this.langIdx++;
    return idx;
}

MonacoEditorLocalesPlugin.prototype.init = function () {
    this.mapLangIdx = {};
    this.lang = {};

    this.mapEmbedLangName = {
        "en": require("./editor.main.nls.en"),
        "zh-cn": require("./editor.main.nls.zh-cn")
    };

    //{"de":{"enLang":"deLang", ...}, es:{}, ...}
    this.mapEmbedLangNameSelf = {
        "zh-cn": {}
    }
}