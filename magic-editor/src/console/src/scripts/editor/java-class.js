import request from '@/api/request.js'
import contants from '@/scripts/contants.js'

let scriptClass = {}
let extensions = {}
let importClass = []
let functions = []
let autoImportModule;
let autoImportClass;

const getWrapperClass = (target) => {
    if (target === 'int' || target === 'java.lang.Integer') {
        return 'java.lang.Integer';
    }
    if (target === 'string' || target === 'java.lang.String') {
        return 'java.lang.String';
    }
    if (target === 'double' || target === 'java.lang.Double') {
        return 'java.lang.Double';
    }
    if (target === 'float' || target === 'java.lang.Float') {
        return 'java.lang.Float';
    }
    if (target === 'byte' || target === 'java.lang.Byte') {
        return 'java.lang.Byte';
    }
    if (target === 'short' || target === 'java.lang.Short') {
        return 'java.lang.Short';
    }
    if (target === 'long' || target === 'java.lang.Long') {
        return 'java.lang.Long';
    }
    if (target.indexOf('[]') > -1) {
        return '[Ljava.lang.Object;';
    }
    return target || 'java.lang.Object';
}
const getSimpleClass = (target) => {
    let index = target.lastIndexOf('.')
    if (index > -1) {
        return target.substring(index + 1);
    }
    return target;
}
const matchTypes = (parameters, args, extension) => {
    if(parameters.length > 0 && parameters[parameters.length - 1].varArgs){
        return extension ? parameters.length - 1 <= args.length : parameters.length <= args.length;
    }else{
        return extension ? parameters.length - 1 === args.length : parameters.length === args.length;
    }
}
const initClasses = function () {
    return new Promise((resolve, reject) => {
        request.send('classes').success(data => {
            scriptClass = data.classes || {}
            extensions = data.extensions || {}
            functions = data.functions || []
            resolve()
        }).exception(res => {
            reject()
        }).error(res => {
            reject()
        })
    })
}
const initImportClass = () => {
    return new Promise((resolve, reject) => {
        request.execute({
            url: 'classes.txt',
            responseType: 'text'
        }).then(e => {
            importClass = e.data.split('\r\n')
            resolve()
        }).catch(res => {
            reject()
        })
    })
}

const padding = (num, n) => Array(n > (num + '').length ? (n - ('' + num).length - 1) : 0).join(0) + num;

const findEnums = (clazz) => {
    let enums = []
    if (clazz) {
        enums = clazz.enums || [];
        if (clazz.superClass) {
            enums = enums.concat(findEnums(clazz.superClass));
        }
    }
    return enums;
}
const processMethod = (method, begin, sort) => {
    method.insertText = method.name;
    if (method.parameters.length > begin) {
        let params = [];
        let params2 = [];
        for (let j = begin; j < method.parameters.length; j++) {
            params.push('${' + (j + 1 - begin) + ':' + method.parameters[j].name + '}');
            if (method.parameters[j].varArgs) {
                params2.push(getSimpleClass(method.parameters[j].type).replace('[]','') + " ... " + method.parameters[j].name);
            } else {
                params2.push(getSimpleClass(method.parameters[j].type) + " " + method.parameters[j].name);
            }
        }
        if (!method.comment) {
            method.comment = getSimpleClass(method.returnType) + ':' + method.name + '(' + params2.join(',') + ')';
        }
        method.sortText = padding(sort, 10) + method.name;
        method.fullName = method.name + '(' + params2.join(', ') + ')';
        method.insertText += '(' + params.join(',') + ')';
        method.signature = method.name + params2.join(',');
    } else {
        method.sortText = padding(sort, 10) + method.name;
        method.insertText += '()';
        method.fullName = method.name + '()';
        if (!method.comment) {
            method.comment = getSimpleClass(method.returnType) + ':' + method.name + '()';
        }
        method.signature = method.name;
    }
    return method;
}
let extensionAttribute = {};
const setExtensionAttribute = (clazz, value) => {
    extensionAttribute[clazz] = value;
}
const findAttributes = (clazz) => {
    let attributes = [];
    if (clazz) {
        attributes = clazz.attributes || [];
        if (clazz.superClass) {
            attributes = attributes.concat(findAttributes(clazz.superClass));
        }
        if (clazz.interfaces && clazz.interfaces.length > 0) {
            for (let i = 0, len = clazz.interfaces.length; i < len; i++) {
                attributes = attributes.concat(findAttributes(clazz.interfaces[i]));
            }
        }
        if (extensionAttribute[clazz.className]) {
            attributes = attributes.concat(extensionAttribute[clazz.className])
        }
    }
    return attributes;
}
const findMethods = (clazz, sort) => {
    sort = sort || 0;
    let methods = [];
    let _findMethod = (target, begin, sort) => {
        if (target && target.methods) {
            for (let i = 0, len = target.methods.length; i < len; i++) {
                let method = target.methods[i];
                method = processMethod(method, begin, sort);
                method.extension = begin === 1;
                methods.push(method);
            }
        }
    }
    if (typeof clazz === 'string') {
        clazz = scriptClass[clazz];
    }
    if (clazz) {
        _findMethod(clazz, 0, sort);
        if (clazz.superClass) {
            methods = methods.concat(findMethods(clazz.superClass, sort + 1));
        }
        if (clazz.interfaces && clazz.interfaces.length > 0) {
            for (let i = 0, len = clazz.interfaces.length; i < len; i++) {
                methods = methods.concat(findMethods(clazz.interfaces[i], sort + 100));
            }
        }
        clazz = extensions[clazz.className];
        if (clazz) {
            _findMethod(clazz, 1, sort + 10000);
        }
    }
    return methods;
}

const getExtension = (clazz) => {
    return extensions[clazz]
}

async function loadClass(className) {
    if (!className) {
        throw new Error('className is required');
    }
    let val = scriptClass[className];
    if (!val) {
        try {
            let res = await request.execute({url: '/class', data: {className}})
            let clazzs = res.data.data;
            clazzs.forEach(it => {
                scriptClass[it.className] = it;
            })
            val = scriptClass[className]
        } catch (e) {

        }
    } else {
        val = scriptClass[val.className] || val    // fix attribute
    }
    return val;
}

const findFunction = () => {
    return functions.map(method => processMethod(method, 0, 1));
}

const initAutoImport = () => {
    if (!autoImportModule && contants.config) {
        let config = contants.config;
        if (config.autoImportModuleList) {
            autoImportModule = {};
            config.autoImportModuleList.forEach(it => {
                autoImportModule[it] = it;
            })
        }
        let importPackages = ['java.util.', 'java.lang.'].concat((config.autoImportPackage || '').replace(/\\s/g, '').replace(/\*/g, '').split(','));
        autoImportClass = {};
        importClass.forEach(className => {
            importPackages.forEach(packageName => {
                if (className.indexOf(packageName) === 0 && className.indexOf(".", packageName.length) === -1) {
                    autoImportClass[className.substring(className.lastIndexOf('.') + 1)] = className;
                }
            })
        })
    }
}
const getAutoImportModule = () => {
    initAutoImport();
    return autoImportModule || {}
}
const getAutoImportClass = () => {
    initAutoImport();
    return autoImportClass || {}
}
const getImportClass = () => importClass;
let onlineFunctionFinder;
const setupOnlineFunction = (loader) => {
    onlineFunctionFinder = loader;
}
const getOnlineFunction = (path) => {
    return onlineFunctionFinder && onlineFunctionFinder(path);
}
const exportValue = {
    findEnums,
    findAttributes,
    findMethods,
    findFunction,
    loadClass,
    initClasses,
    initImportClass,
    getWrapperClass,
    matchTypes,
    getAutoImportModule,
    getAutoImportClass,
    getExtension,
    getImportClass,
    getOnlineFunction,
    setupOnlineFunction,
    setExtensionAttribute,
}
export default exportValue;
