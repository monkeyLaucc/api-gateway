package cc.lau.core;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

public class ApiDocumentHandler implements InitializingBean, ApplicationContextAware {

    private ApiDocument apiDocument;
    private final ParameterNameDiscoverer parameterNameDiscoverer;
    public ApiDocumentHandler() {
        parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        apiDocument = new ApiDocument(applicationContext);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        apiDocument.loadApiFromSpringBean();
    }




}
