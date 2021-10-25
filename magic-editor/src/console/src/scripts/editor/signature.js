import JavaClass from './java-class.js'
import tokenizer from '@/scripts/parsing/tokenizer.js'
import {TokenStream, TokenType} from '../parsing/index.js'
import {Parser} from '@/scripts/parsing/parser.js'

const SignatureHelpProvider = {
    signatureHelpRetriggerCharacters: ['(', ','],
    signatureHelpTriggerCharacters: ['(', ','],
    provideSignatureHelp: async (model, position, token, context) => {
        if (context.activeSignatureHelp) {
            let helper = context.activeSignatureHelp;
            helper.activeSignature += 1;
            if (helper.activeSignature === helper.signatures.length) {
                helper.activeSignature = 0;
            }
            return {
                dispose: function () {
                },
                value: helper
            }
        }
        let value = model.getValueInRange({
            startLineNumber: 1,
            startColumn: 1,
            endLineNumber: position.lineNumber,
            endColumn: position.column
        });
        try {
            let tokens = tokenizer(value);
            let parser = new Parser(new TokenStream(tokens));
            const { best, env} = await parser.parseBest(value.length - 1);
            if(best && best.constructor.name === 'MethodCall'){
                let target = best.target
                let className = await target.getTarget().getJavaType(env);
                let methodName = target.member.getText()
                let methods = JavaClass.findMethods(await JavaClass.loadClass(className));
                let signatures = []
                methods.filter(it => it.name === methodName).forEach(method => {
                    let document = [];
                    for (let j = (method.extension ? 1 : 0); j < method.parameters.length; j++) {
                        let param = method.parameters[j];
                        document.push('- ' + param.name + '：' + (param.comment || param.type));
                    }
                    signatures.push({
                        label: method.fullName,
                        documentation: {
                            value: method.comment
                        },
                        parameters: [{
                            label: 'param1',
                            documentation: {
                                value: document.join('\r\n')
                            }
                        }]
                    });
                })
                if (signatures.length > 0) {
                    return {
                        dispose: function () {
                        },
                        value: {
                            activeParameter: 0,
                            activeSignature: 0,
                            signatures: signatures
                        }
                    }
                }
            }
        } catch (e) {
            // console.log(e);
        }
    }
}
export default SignatureHelpProvider