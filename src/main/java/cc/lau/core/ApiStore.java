package cc.lau.core;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

public class ApiStore {
    private ApplicationContext applicationContext;


    private static HashMap<String, APIRunnable> apiMap = new HashMap<String, APIRunnable>();

    public ApiStore(ApplicationContext applicationContext) {
        Assert.notNull(applicationContext);
        this.applicationContext = applicationContext;
    }

    public void loadApiFromSpringBeans() {
        String[] names = applicationContext.getBeanDefinitionNames();
        Class<?> type;

        for (String name : names) {
            type = applicationContext.getType(name);
            for (Method m : type.getDeclaredMethods()) {
                ApiMapping apiMapping = m.getAnnotation(ApiMapping.class);
                if (apiMapping != null) {
                    addApiItem(apiMapping, name, m);
                }
            }
        }
    }

    private void addApiItem(ApiMapping apiMapping, String beanName, Method method) {
        APIRunnable apiRunnable = new APIRunnable();
        apiRunnable.apiName = apiMapping.value();
        apiRunnable.apiDescription = apiMapping.description();
        apiRunnable.targetName = beanName;
        apiRunnable.targetMethod = method;
        apiMap.put(apiMapping.value(), apiRunnable);
    }

    public APIRunnable findApiRunnable(String apiName) {
        return apiMap.get(apiName);
    }

    public List<APIRunnable> findApiRunnables(String apiName) {
        if (apiName == null) {
            throw new IllegalArgumentException("api name must not null");
        }
        List<APIRunnable> list = new ArrayList<>(20);
        for (APIRunnable api : apiMap.values()) {
            if (api.apiName.equals(apiName)) {
                list.add(api);
            }
        }
        return list;
    }


    public List<APIRunnable> getAll() {
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

    /**
     * 获取api文档
     *
     * @return
     */
    public List<APIDocument> getApiDocument() {
        ParameterNameDiscoverer parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
        List<APIDocument> list = new ArrayList<>();
        for (ApiStore.APIRunnable api : apiMap.values()) {
            String apiName = api.getApiName();
            String apiDesc = api.getApiDescription();
            Method method = api.getTargetMethod();
            String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
            Class<?>[] types = method.getParameterTypes();
            Map<String, String> mapItem = new HashMap<>();
            for (int i = 0; i < parameterNames.length; i++) {
                try {
                    String json;
                    if (Integer.class.equals(types[i])) {
                        json = null;
                    } else if (Long.class.equals(types[i])) {
                        json = null;
                    } else {
                        json = JsonTool.toJson(types[i].newInstance());
                    }
                    mapItem.put(parameterNames[i], json);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            APIDocument apiDocument = new APIDocument(apiName,apiDesc,JsonTool.toJson(mapItem));
            list.add(apiDocument);
        }
        return list;
    }


    @Data
    public class APIRunnable {

        String apiName;

        String apiDescription;

        String targetName;

        Object target;

        Method targetMethod;

        public Object run(Object... args) throws InvocationTargetException, IllegalAccessException {
            if (target == null) {
                target = applicationContext.getBean(targetName);
            }
            return targetMethod.invoke(target, args);
        }

        public Class<?>[] getParamTypes() {
            return targetMethod.getParameterTypes();
        }


    }

    @Data
    @NoArgsConstructor
    public class APIDocument{
        private String apiName;
        private String apiDescription;
        private String params;

        public APIDocument(String apiName, String apiDescription, String params) {
            this.apiName = apiName;
            this.apiDescription = apiDescription;
            this.params = params;
        }
    }
}
