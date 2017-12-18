package cc.lau.core;

import lombok.Data;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ApiDocument {

    private static List<ApiRecord> apiRecords = new ArrayList<>();

    private ApplicationContext applicationContext;

    public ApiDocument(ApplicationContext applicationContext) {
        Assert.notNull(applicationContext);
        this.applicationContext = applicationContext;
    }

    public void loadApiFromSpringBean() {
        String[] names = applicationContext.getBeanDefinitionNames();
        Class<?> type;

        for (String name : names) {
            type = applicationContext.getType(name);
            Api api = type.getAnnotation(Api.class);
            if (api != null) {
                String apiCategory = api.value();
                for (Method m : type.getDeclaredMethods()) {
                    ApiMapping apiMapping = m.getAnnotation(ApiMapping.class);
                    if (apiMapping != null) {
                        ApiRecord apiRecord = new ApiRecord();
                        apiRecord.apiCategory = apiCategory;
                        apiRecord.apiName = apiMapping.value();
                        apiRecord.apiDescription = apiMapping.description();
                        apiRecord.targetName = name;
                        apiRecord.targetMethod = m;
                        apiRecords.add(apiRecord);
                    }
                }
            }
        }
    }

    public List<ApiRecord> getApiRecords() {
        return apiRecords;
    }




    @Data
    public class ApiRecord {
        private String apiCategory;
        private String apiName;
        private String apiDescription;
        private String targetName;
        private Method targetMethod;

    }

}
