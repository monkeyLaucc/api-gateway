package cc.lau.core;

import cc.lau.entity.Student;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class testMap {
    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

        String json = "{\"student\":{\"id\":1,\"name\":\"cccc\"}}";
        Student student = JsonTool.convertValue(JsonTool.toMap(json), Student.class);
//        Student student = JsonTool.toObject(Student.class,json);
        System.out.println(student.toString());
    }
}
