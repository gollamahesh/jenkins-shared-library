#!groovy
def decidepipeline(Map configMap){
    application = configMap.get("application")
    //# here we are getting node js vm
    switch(application) {
        case 'nodeJSVM':
            echo "application is nodejs vm"
            nodeJSVMCI.call(configMap) 
            
        break
        case 'javaVM':
            javaVMCI(configMap)
        break
        default:
            error "unrecognized application"
        break
    }
    //echo "I need to take decision map we set"
}