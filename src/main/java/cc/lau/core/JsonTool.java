package cc.lau.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * json处理工具类
 */
public class JsonTool {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    /**
     * toJson
     *
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * json 2 object
     *
     * @param clazz
     * @param jsonStr
     * @param <T>
     * @return
     */
    public static <T> T toObject(Class<T> clazz, String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, clazz);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * json to Map
     *
     * @param jsonStr jsonStr
     * @return Map
     */
    public static Map toMap(String jsonStr) {
        try {
            return objectMapper.readValue(jsonStr, Map.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * json to Map
     *
     * @param jsonStr jsonStr
     * @return Map
     */
    public static Map toMap(String jsonStr, Class<?>... classMeta) {
        return (Map) fromJson(jsonStr, constructParametricType(Map.class, classMeta));
    }


    /**
     * json to List
     *
     * @param jsonStr   jsonStr
     * @param classMeta 需要转换的类型
     * @param <T>
     * @return
     */
    public static <T> List<T> toList(String jsonStr, Class<T> classMeta) {
        return (List<T>) fromJson(jsonStr, constructParametricType(List.class, classMeta));
    }

    /**
     * json to ArrayList
     *
     * @param jsonStr   jsonStr
     * @param classMeta 需要转换的类型
     * @param <T>
     * @return
     */
    public static <T> ArrayList<T> toArrayList(String jsonStr, Class<T> classMeta) {
        return (ArrayList<T>) fromJson(jsonStr, constructParametricType(ArrayList.class, classMeta));
    }

    /**
     * 構造泛型的Type如List<MyBean>, 则调用constructParametricType(ArrayList.class,MyBean.class)
     */
    private static JavaType constructParametricType(Class<?> parametrized, Class<?>... parameterClasses) {
        return objectMapper.getTypeFactory().constructParametricType(parametrized, parameterClasses);
    }

    /**
     * 将jsonString 按照指定类型转换
     *
     * @param jsonString
     * @param javaType
     * @param <T>
     * @return
     */
    private static <T> T fromJson(String jsonString, JavaType javaType) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            return (T) objectMapper.readValue(jsonString, javaType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T convertValue(Object value, Class<T> clazz) {
        if (StringUtils.isEmpty(value.toString())) {
            return null;
        }
        try {
            if (value instanceof String) {
                value = objectMapper.readTree((String) value);
                return objectMapper.convertValue(value, clazz);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        return null;
    }
}

