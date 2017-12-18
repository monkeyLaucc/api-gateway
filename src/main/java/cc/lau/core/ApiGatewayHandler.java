package cc.lau.core;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by cc on 17/12/15
 */
@Component
public class ApiGatewayHandler implements InitializingBean, ApplicationContextAware {

    private static final String METHOD = "method";
    private static final String PARAMS = "params";


    ApiStore apiStore;
    final ParameterNameDiscoverer parameterNameDiscoverer;

    public ApiGatewayHandler() {
        parameterNameDiscoverer = new LocalVariableTableParameterNameDiscoverer();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        apiStore = new ApiStore(applicationContext);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        apiStore.loadApiFromSpringBeans();
    }


    /**
     * 处理所有转发来的请求，并返回处理结果
     *
     * @param request
     * @param response
     */
    public void handle(HttpServletRequest request, HttpServletResponse response) {
        //系统参数验证
        String params = request.getParameter(PARAMS);
        Object result = null;
        ApiStore.APIRunnable apiRunnable = null;
        try {
            apiRunnable = sysParamsValidate(request);
            Object[] args = buildParams(apiRunnable, params, request);
            result = apiRunnable.run(args);
        } catch (ApiException e) {
            e.printStackTrace();
            result = handleException(e);
        } catch (Exception e) {
            e.printStackTrace();
            result = handleException(e);
        }
        //返回处理结果
        returnResult(result, response);
    }

    /**
     * 统一处理异常
     *
     * @param throwable
     * @return
     */
    public Object handleException(Throwable throwable) {
        Map<String, Object> map = new HashMap<>();
            map.put("code", "50000");
            map.put("msg", throwable.getMessage());
            return map;
    }


    /**
     * 验证请求参数
     *
     * @param request
     * @return
     */
    private ApiStore.APIRunnable sysParamsValidate(HttpServletRequest request) {
        String apiName = request.getParameter(METHOD);
        String json = request.getParameter(PARAMS);

        ApiStore.APIRunnable apiRunnable;
        if (apiName == null || apiName.trim().equals("")) {
            throw new ApiException("调用失败：参数'method'为空");
        } else if (json == null) {
            throw new ApiException("调用失败：参数'params'为空");
        } else if ((apiRunnable = apiStore.findApiRunnable(apiName)) == null) {
            throw new ApiException("调用失败：指定API不存在，API:" + apiName);
        }
        return apiRunnable;
    }

    /**
     * 构建方法的请求参数
     *
     * @param run
     * @param paramJson
     * @param request
     * @return
     */
    private Object[] buildParams(ApiStore.APIRunnable run, String paramJson, HttpServletRequest request) {
        Map<String, Object> map = null;
        map = JsonTool.toMap(paramJson);
        if (map == null) {
            map = new HashMap<>();
        }
        Method method = run.getTargetMethod();
        List<String> paramNames = Arrays.asList(parameterNameDiscoverer.getParameterNames(method));//获取方法的参数名
        Class<?>[] paramTypes = method.getParameterTypes();//获取方法参数类型

        //验证调用接口方法的参数是否存在
        for (Map.Entry<String, Object> m : map.entrySet()) {
            if (!paramNames.contains(m.getKey())) {
                throw new ApiException("调用失败：接口不存在'" + m.getKey() + "'参数");
            }
        }

        //获取调用接口方法的参数
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i].isAssignableFrom(HttpServletRequest.class)) {
                args[i] = request;
            } else if (map.containsKey(paramNames.get(i))) {
                args[i] = convertJsonToBean(map.get(paramNames.get(i)), paramTypes[i]);
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    /**
     * 转换为方法参数对象
     *
     * @param val
     * @param targetClass
     * @param <T>
     * @return
     */
    private <T> Object convertJsonToBean(Object val, Class<T> targetClass) {
        Object result = null;
        if (val == null) {
            return null;
        } else if (Integer.class.equals(targetClass)) {
            result = Integer.parseInt(val.toString());
        } else if (Long.class.equals(targetClass)) {
            result = Long.parseLong(val.toString());
        } else if (Date.class.equals(targetClass)) {
            if (val.toString().matches("[0-9]+")) {
                result = new Date(Long.parseLong(val.toString()));
            } else {
                throw new IllegalArgumentException("日期必须是长整型的时间戳");
            }
        } else if (String.class.equals(targetClass)) {
            if (val instanceof String) {
                result = val;
            } else {
                throw new IllegalArgumentException("转换目标类型必须是字符串");
            }
        } else {
            //对象类型的转换
            result = JsonTool.toObject(targetClass,JsonTool.toJson(val));
            //json转对象失败
            if(result == null){
                try {
                    throw new IllegalArgumentException("参数对象转换失败："+JsonTool.toJson(val)+"无法转换成:"+targetClass.getName()+"对象，正确的json格式为："+JsonTool.toJson(targetClass.newInstance()));
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            //校验参数对象
            ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<Object>> violations = validator.validate(result);
            ConstraintViolationImpl constraintViolation;
            if (violations.size() > 0) {
                constraintViolation = (ConstraintViolationImpl) violations.iterator().next();
                String className = StringUtils.uncapitalize(constraintViolation.getRootBeanClass().getSimpleName());
                String fieldName = constraintViolation.getPropertyPath().toString();
                String msg = String.join(".", className, fieldName);
                throw new IllegalArgumentException(msg+"校验不通过");
            }
        }
        return result;
    }

    /**
     * 返回结果
     *
     * @param result
     * @param response
     */
    private void returnResult(Object result, HttpServletResponse response) {
        String json = JsonTool.toJson(result);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html/json;charset=utf-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        if (json != null) {
            try {
                response.getWriter().write(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 获取api文档
     * @return
     */
    public List<ApiStore.APIDocument> getApiDocument(){
        return apiStore.getApiDocument();
    }
}
