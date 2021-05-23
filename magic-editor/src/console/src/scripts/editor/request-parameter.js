const RequestParameter = {
    environmentFunction: ()=>{},
    setEnvironment: callback => RequestParameter.environmentFunction = callback,
}
export default RequestParameter;