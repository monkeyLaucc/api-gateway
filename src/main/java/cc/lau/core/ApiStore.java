package cc.lau.core;

import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ApiStore {
    private ApplicationContext applicationContext;


    private static HashMap<String,APIRunnable> apiMap = new HashMap<String, APIRunnable>();

    public ApiStore(ApplicationContext applicationContext){
        Assert.notNull(applicationContext);
        this.applicationContext = applicationContext;
    }

    public void loadApiFromSpringBeans(){
        String[] names = applicationContext.getBeanDefinitionNames();
        Class<?> type;

        for(String name : names){
            type = applicationContext.getType(name);
            for(Method m : type.getDeclaredMethods()){
                ApiMapping ApiMapping = m.getAnnotation(ApiMapping.class);
                if(ApiMapping != null){
                    addApiItem(ApiMapping,name,m);
                }
            }
        }

    }

    private void addApiItem(ApiMapping ApiMapping,String beanName,Method method){
        APIRunnable apiRunnable = new APIRunnable();
        apiRunnable.apiName = ApiMapping.value();
        apiRunnable.targetName = beanName;
        apiRunnable.targetMethod = method;
        apiMap.put(ApiMapping.value(),apiRunnable);
    }

    public APIRunnable findApiRunnable(String apiName){
        return apiMap.get(apiName);
    }

    public List<APIRunnable> findApiRunnables(String apiName){
        if(apiName == null){
            throw new IllegalArgumentException("api name must not null");
        }
        List<APIRunnable> list = new ArrayList<>(20);
        for(APIRunnable api : apiMap.values()){
            if(api.apiName.equals(apiName)){
                list.add(api);
            }
        }
        return list;
    }


    public List<APIRunnable> getAll(){
        List<APIRunnable> list = new ArrayList<>(20);
        list.addAll(apiMap.values());
        Collections.sort(list, new Comparator<APIRunnable>() {
            @Override
            public int compare(APIRunnable o1, APIRunnable o2) {
                return o1.getApiName().compareTo(o2.getApiName());
            }
        });
        return list;
    }



    @Data
    public class APIRunnable{

        String apiName;

        String targetName;

        Object target;

        Method targetMethod;

        public Object run(Object...args) throws InvocationTargetException, IllegalAccessException {
            if(target == null){
                target = applicationContext.getBean(targetName);
            }
            return targetMethod.invoke(target,args);
        }

        public Class<?> [] getParamTypes(){
            return targetMethod.getParameterTypes();
        }


    }
}
