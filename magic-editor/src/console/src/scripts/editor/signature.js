import JavaClass from './java-class.js'
import tokenizer from '@/scripts/parsing/tokenizer.js'
import {TokenStream, TokenType} from '../parsing/index.js'
import {Parser} from '@/scripts/parsing/parser.js'
import RequestParameter from './request-parameter.js'

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
        let char = value.charAt(value.length - 1);
        if (char !== '(') {
            return;
        }
        let input = value.substring(0, value.lastIndexOf('('));
        try {
            let tokens = tokenizer(input);
            let tokenLen = tokens.length;
            if (tokenLen === 0 || tokens[tokenLen - 1].getTokenType() !== TokenType.Identifier) {
                return;
            }
            let token = tokens.pop();
            tokens.pop();
            let parser = new Parser(new TokenStream(tokens));
            var clazz = await parser.completion(RequestParameter.environmentFunction());
            let methods = JavaClass.findMethods(clazz);
            if (methods) {
                var name = token.getText();
                var signatures = [];
                for (var i = 0, len = methods.length; i < len; i++) {
                    var method = methods[i];
                    if (method.name === name) {
                        var document = [];
                        for (var j = (method.extension ? 1 : 0); j < method.parameters.length; j++) {
                            var param = method.parameters[j];
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
                    }
                }
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
            //console.log(e);
        }
    }
}
export default SignatureHelpProvider