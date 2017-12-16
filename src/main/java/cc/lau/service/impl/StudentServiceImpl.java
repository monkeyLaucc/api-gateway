package cc.lau.service.impl;

import cc.lau.core.ApiMapping;
import cc.lau.entity.Student;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class StudentServiceImpl {

    @ApiMapping("test")
    public Student add(Student student, Integer id){
        return student;
    }

    @ApiMapping("list")
    public List<Student> list(){
        return Arrays.asList(new Student("aa"),new Student("bb"),new Student("cc"),new Student("dd"));
    }

}
