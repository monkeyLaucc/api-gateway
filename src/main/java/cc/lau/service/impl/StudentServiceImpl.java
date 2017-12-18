package cc.lau.service.impl;

import cc.lau.core.ApiMapping;
import cc.lau.core.ApiStore;
import cc.lau.entity.Student;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class StudentServiceImpl {

    @ApiMapping(value = "test",description = "测试测试")
    public Student add(Student student, Integer id){
        if(student == null){
            throw new IllegalArgumentException("student 不能为空");
        }
        return student;
    }

    @ApiMapping(value = "list",description = "获取学生列表")
    public List<Student> list(){
        return Arrays.asList(new Student("aa"),new Student("bb"),new Student("cc"),new Student("dd"));
    }


}
